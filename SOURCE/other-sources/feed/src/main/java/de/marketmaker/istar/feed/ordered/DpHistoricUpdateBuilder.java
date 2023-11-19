/*
 * DpHistoricRecordBuilder.java
 *
 * Created on 03.01.2008 13:59:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.jcip.annotations.ThreadSafe;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.DateTimeProviderImpl;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.dp.web.DpPricesFormatter;

/**
 * A thread-safe record builder that collects records with ADF_Handelsdatum &lt; Today and stores
 * those records in a file. The file will be rotated whenever an external scheduler calls
 * {@link #rotateFile()}, which usually happens at midnight.<p>
 * The overall number of updates stored per day is limited to {@value #MAX_RECORD_COUNT} and per
 * market to {@value #MAX_RECORD_COUNT_PER_MARKET}. If theses numbers are exceeded, further
 * updates (for the market) will be ignored until {@link #rotateFile()} is invoked again.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ThreadSafe
@ManagedResource
public class DpHistoricUpdateBuilder
        implements InitializingBean, DisposableBean, OrderedUpdateBuilder {

    private static class UpdateLimit {
        private int numUpdates = 1;

        private final boolean blocked;

        UpdateLimit(boolean blocked) {
            this.blocked = blocked;
        }

        boolean isLimitReached() {
            return this.blocked || this.numUpdates++ > MAX_RECORD_COUNT_PER_MARKET;
        }

        boolean isBlocked() {
            return this.blocked;
        }

        void reset() {
            this.numUpdates = 0;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DateTimeProvider dateTimeProvider = DateTimeProviderImpl.INSTANCE;

    private ByteBuffer bb = ByteBuffer.allocate(1024 * 32);

    private File baseDirectory;

    private FileChannel channel;

    private RandomAccessFile raf;

    private Map<ByteString, UpdateLimit> limits = new HashMap<>();

    private File currentFile;

    private int recordCount = 0;

    private KafkaProducer<String, String> kafkaProducer;

    private String kafkaTopic;

    private DpPricesFormatter dpPricesFormatter;

    private int[] fieldIds;

    private SnapFieldIteratorFactory iteratorFactory;

    private static final int MAX_RECORD_COUNT = 3000000;

    private static final int MAX_RECORD_COUNT_PER_MARKET = MAX_RECORD_COUNT / 3;

    private static final String BIN_FILE_SUFFIX = ".dph";

    private static final String TMP_FILE_SUFFIX = ".tmp";

    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void setDateTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    public void setKafkaProducer(KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public void setDpPricesFormatter(DpPricesFormatter dpPricesFormatter) {
        this.dpPricesFormatter = dpPricesFormatter;
    }

    public void setFieldIds(int[] fieldIds) {
        this.fieldIds = fieldIds;
    }

    @ManagedOperation
    public synchronized void setMarketsToIgnore(String s) {
        this.limits.values().removeIf(UpdateLimit::isBlocked);
        final String[] markets = s.split(",");
        for (String market : markets) {
            this.limits.put(new ByteString(market.trim()), new UpdateLimit(true));
        }
    }

    @ManagedAttribute
    public synchronized String getMarketsToIgnore() {
        final List<String> names = new ArrayList<>();
        for (Map.Entry<ByteString, UpdateLimit> entry : limits.entrySet()) {
            if (entry.getValue().isBlocked()) {
                names.add(entry.getKey().toString());
            }
        }
        return names.toString();
    }

    public void afterPropertiesSet() throws Exception {
        if (this.baseDirectory == null || !this.baseDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid baseDirectory '" + this.baseDirectory + "'");
        }
        this.iteratorFactory = SnapFieldIteratorFactory.create(DpPricesFormatter.DEFAULT_ESCAPE_NULLS, this.fieldIds);

        initFile();
    }

    public void destroy() throws Exception {
        closeCurrent();
    }

    public synchronized void rotateFile() {
        closeCurrent();
        renameTmpFile();
        initFile();
    }

    private void renameTmpFile() {
        final File p = this.currentFile.getParentFile();
        final File f = getFile(p, p.getName(), false);
        if (!this.currentFile.renameTo(f)) {
            this.logger.error("<renameTmpFile> failed for " + f.getAbsolutePath());
        }
    }

    private void initFile() {
        initFile(this.dateTimeProvider.dayAsYyyyMmDd());
    }

    private synchronized void initFile(int yyyymmdd) {
        this.currentFile = getFile(yyyymmdd, true);
        if (this.recordCount > 0) {
            this.logger.info("<initFile> previous recordCount = " + this.recordCount);
        }
        this.recordCount = 0;

        try {
            if (!FileUtil.isDirWriteable(this.currentFile.getParentFile())) {
                throw new IOException("not writable: " + this.currentFile.getAbsolutePath());
            }

            this.raf = new RandomAccessFile(this.currentFile, "rw");
            if (this.raf.length() > 0) {
                this.raf.seek(this.raf.length());
            }
            this.channel = this.raf.getChannel();
        } catch (IOException e) {
            this.logger.error("<rotateFile> failed", e);
            this.channel = null;
        }
    }

    private void flushBuffer() {
        this.bb.flip();
        append(this.bb);
    }

    private void append(final ByteBuffer buffer) {
        try {
            this.channel.write(buffer);
        } catch (IOException e) {
            this.logger.error("<append> failed", e);
        } finally {
            buffer.clear();
        }
    }

    private File getFile(int yyyymmdd, boolean temp) {
        final File dir = new File(this.baseDirectory, Integer.toString(yyyymmdd));
        return getFile(dir, dir.getName(), temp);
    }

    private File getFile(File dir, final String yyyyymmdd, boolean temp) {
        return new File(dir, "DPH-" + yyyyymmdd + (temp ? TMP_FILE_SUFFIX : BIN_FILE_SUFFIX));
    }

    private synchronized void closeCurrent() {
        if (this.channel == null) {
            return;
        }
        flushBuffer();
        try {
            this.channel.close();
        } catch (IOException e) {
            this.logger.error("<closeCurrent> failed to close channel!", e);
        }
        try {
            this.raf.close();
        } catch (IOException e) {
            this.logger.error("<closeCurrent> failed to close raf!", e);
        }
        this.channel = null;

        resetLimits();
    }

    private void resetLimits() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<ByteString, UpdateLimit> entry : this.limits.entrySet()) {
            if (entry.getValue().isBlocked()) {
                continue;
            }
            if (entry.getValue().numUpdates > MAX_RECORD_COUNT_PER_MARKET) {
                this.logger.warn("<resetLimits> rejected "
                        + (entry.getValue().numUpdates - MAX_RECORD_COUNT_PER_MARKET)
                        + " updates for market " + entry.getKey());
            }
            if (entry.getValue().numUpdates > 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=>").append(entry.getValue().numUpdates);
            }
            entry.getValue().reset();
        }
        this.logger.info("<resetLimits> " + sb);
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (update.hasFlag(FeedUpdateFlags.FLAG_WITH_OLD_HANDELSDATUM)) {
            doProcess(data, update);
        }
    }

    private synchronized void doProcess(FeedData data, OrderedUpdate update) {

        if (isLimitReached(data)) {
            return;
        }

        if (this.kafkaProducer != null) {
            // Write to Kafka first
            this.kafkaProducer.send(
                    new ProducerRecord<>(
                            this.kafkaTopic,
                            update.getVendorkey().toString(),
                            convertToMessage(data, update)
                    ),
                    (metadata, exception) -> {
                        if (exception != null) {
                            this.logger.error("Failed pushing update to Kafka", exception);
                        } else {
                            this.logger.debug("<doProcess> Pushed update to Kafka: " + metadata.toString());
                        }
                    }
            );
        }

        // No file open -> discard
        if (this.channel == null) {
            return;
        }

        // Write to file or the buffer
        final ByteBuffer buffer = update.asMessageWithLength();
        if (this.bb.remaining() < buffer.remaining()) {
            flushBuffer();
            if (this.bb.remaining() < buffer.remaining()) {
                append(buffer);
                return;
            }
        }
        this.bb.put(buffer);

        if (++this.recordCount > MAX_RECORD_COUNT) {
            this.logger.error("<writeBuffer> max record count exceeded, stop recording");
            flushBuffer();
            closeCurrent();
        }
    }

    private String convertToMessage(FeedData data, OrderedUpdate update) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        dpPricesFormatter.write(pw, data, iteratorFactory.iterator(update), false, false, true, update.getTimestamp());
        return sw.toString();
    }

    private boolean isLimitReached(FeedData data) {
        final ByteString market = data.getMarket().getName();
        final UpdateLimit limit = this.limits.get(market);
        if (limit != null) {
            return limit.isLimitReached();
        }
        this.limits.put(market, new UpdateLimit(false));
        return false;
    }

    public Iterator<OrderedUpdate> read(File dir, String yyyymmdd) {
        final File file = getFile(dir, yyyymmdd, false);
        if (!file.exists()) {
            return Collections.emptyIterator();
        }
        try {
            return new DataIterator(new RandomAccessFile(file, "r"));
        } catch (IOException e) {
            this.logger.error("<read> failed for " + file.getAbsolutePath(), e);
            return Collections.emptyIterator();
        }
    }

    private class DataIterator implements Iterator<OrderedUpdate> {
        private final RandomAccessFile myRaf;

        private final FileChannel fc;

        // strictly internal, not exposed to external objects
        private final ByteBuffer buffer = (ByteBuffer) BufferFieldData.asBuffer(new byte[1024 * 32]).flip();

        // same content as buffer, but exposed to external objects which may change its position, limit etc.
        private final ByteBuffer pubBuffer = buffer.duplicate().order(buffer.order());

        private final OrderedUpdate next = new OrderedUpdate();

        private boolean eof = false;

        private boolean hasNext = false;

        private DataIterator(RandomAccessFile myRaf) throws IOException {
            this.myRaf = myRaf;
            this.fc = this.myRaf.getChannel();
            this.hasNext = readNext();
        }

        private boolean readNext() {
            if (!ensureLength(2)) {
                return false;
            }
            int length = (this.buffer.getShort() & 0xFFFF) - 2;
            if (!ensureLength(length)) {
                return false;
            }

            this.pubBuffer.clear().limit(this.buffer.position() + length).position(this.buffer.position());
            this.next.reset(this.pubBuffer);

            this.buffer.position(this.buffer.position() + length);
            return true;
        }

        public boolean hasNext() {
            return this.hasNext;
        }

        private boolean ensureLength(int n) {
            if (this.buffer.remaining() < n && !this.eof) {
                fillBuffer();
            }
            return this.buffer.remaining() >= n;
        }

        public OrderedUpdate next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            }
            this.hasNext = readNext();
            return this.next;
        }

        private void fillBuffer() {
            this.buffer.compact();
            try {
                int n = this.fc.read(this.buffer);
                this.eof |= (n == -1);
                this.buffer.flip();
            } catch (IOException e) {
                logger.error("<readNext> failed to read ", e);
                this.buffer.clear();
                this.eof = true;
            }
            if (this.eof) {
                if (!IoUtils.close(this.fc)) {
                    logger.error("<readNext> failed to close channel ");
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
