/*
 * DpWriter.java
 *
 * Created on 26.02.2004 09:53:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.AsciiBytes;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataMerger;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.snap.SnapData;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.encodeTimestamp;

/**
 * Responsible for writing the contents of a DpFile to some file. Not thread-safe,
 * because it reuses the same buffer for all files.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NotThreadSafe
@ManagedResource
public class OrderedDpWriter implements InitializingBean, DpFile.Writer {
    private static class SnapWithTimestamp {
        private int timestamp;

        private byte[] snap;

        private void reset(byte[] snap, int timestamp) {
            this.snap = snap;
            this.timestamp = timestamp;
        }

        private boolean isValid(long checkTimestamp) {
            return (this.snap != null) && (this.timestamp & 0xFFFFFFFFL) >= checkTimestamp;
        }
    }


    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    private static final byte LF = (byte) '\n';

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FieldDataMerger fieldDataMerger = new FieldDataMerger();

    /**
     * DpFile contents are first written into this buffer which is then written
     * to the output file.
     */
    private ByteBuffer bb;

    private File defaultOutputDirectory = null;

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * Used for formatting binary data.
     */
    private final AsciiBytes bytes = new AsciiBytes();

    private DpFile dpFile;

    protected static final int FLUSH_IF_LESS_REMAINING = 1024;

    private FeedDataRepository repository;

    private volatile int numRecordsInBatch = 4000;

    private long sleepMillisAfterBatch = 200;

    @ManagedAttribute
    public void setNumRecordsInBatch(int numRecordsInBatch) {
        this.numRecordsInBatch = numRecordsInBatch;
    }

    @ManagedAttribute
    public int getNumRecordsInBatch() {
        return numRecordsInBatch;
    }

    public void setSleepMillisAfterBatch(long sleepMillisAfterBatch) {
        this.sleepMillisAfterBatch = sleepMillisAfterBatch;
    }

    @ManagedAttribute
    public long getSleepMillisAfterBatch() {
        return sleepMillisAfterBatch;
    }

    @Required
    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    public void afterPropertiesSet() throws Exception {
        this.bb = ByteBuffer.allocate(bufferSize);
        this.logger.info("<initialize> bufferSize = " + bufferSize);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setDefaultOutputDirectory(File defaultOutputDirectory) {
        this.defaultOutputDirectory = defaultOutputDirectory;
        this.logger.info("<setDefaultOutputDirectory> " + defaultOutputDirectory.getAbsolutePath());
    }

    public File write(DpFile dpFile) throws Exception {
        this.dpFile = dpFile;

        File f = dpFile.getOutputFile();
        if (!f.isAbsolute() && this.defaultOutputDirectory != null) {
            f = new File(this.defaultOutputDirectory, f.getPath());
        }

        final File tmpFile = new File(f.getParent(), f.getName() + ".tmp");
        ensureOutDirectory(tmpFile.getParentFile());
        FileUtil.deleteIfExists(tmpFile);

        final long checkTimestamp = getCheckTimestamp(dpFile);

        final KeySource keySource = getKeySource();

        TimeTaker tt = new TimeTaker();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tmpFile, "rw");
            final FileChannel channel = raf.getChannel();

            this.bb.clear();
            writeHeader(channel);
            writeData(channel, checkTimestamp, keySource);
            writeFooter(channel);
            IoUtils.close(channel);
            sleep();

            FileUtil.deleteIfExists(f);
            if (!tmpFile.renameTo(f)) {
                this.logger.warn("<write> failed to rename " + tmpFile.getAbsolutePath()
                        + " to " + f.getAbsolutePath());
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<write> " + f.getName() + " took " + tt);
            }
            return f;
        } catch (InterruptedException | IOException e) {
            FileUtil.deleteIfExists(f);
            throw new Exception("<write> failed for " + tmpFile.getAbsolutePath(), e);
        } finally {
            IoUtils.close(raf);
            keySource.close();
        }
    }

    long getCheckTimestamp(DpFile dpFile) {
        final int lastRunTimestamp = dpFile.getLastRunTimestamp();
        if (lastRunTimestamp == -1) {
            return 0L;
        }
        dpFile.setLastRunTimestamp(encodeTimestamp(System.currentTimeMillis()));
        return lastRunTimestamp & 0xFFFFFFFFL;
    }

    private void ensureOutDirectory(final File dir) throws IOException {
        if (dir.isDirectory()) {
            return;
        }
        if (dir.isFile()) {
            throw new IOException("not a directory:" + dir.getAbsolutePath());
        }
        if (!dir.mkdirs()) {
            throw new IOException("failed to create directory: " + dir.getAbsolutePath());
        }
        this.logger.info("<ensureOutDirectory> created: " + dir.getAbsolutePath());
    }

    private KeySource getKeySource() throws IOException {
        if (dpFile.getMarkets() != null) {
            return new MarketKeySource(this.repository, dpFile.getMarkets(), getTypeMapping(dpFile));
        }
        return new FileKeySource(this.repository, dpFile.getKeyFile());
    }

    private MarketKeySource.TypeMapping getTypeMapping(DpFile dpFile) {
        MarketKeySource.TypeMapping result = dpFile.getTypeMapping();
        return (result != null) ? result : MarketKeySource.TypeMapping.NONE;
    }

    private void doWrite(final WritableByteChannel channel) throws IOException {
        this.bb.flip();
        channel.write(this.bb);
        this.bb.clear();
    }

    private void writeHeader(WritableByteChannel channel) throws IOException {
        writeLine(channel, dpFile.getHeader());
    }

    private void writeFooter(WritableByteChannel channel) throws IOException {
        writeLine(channel, dpFile.getEndTag());
    }

    private void writeLine(WritableByteChannel channel, byte[] line) throws IOException {
        if (line != null) {
            this.bb.put(line).put(LF);
            doWrite(channel);
        }
    }

    private void writeData(WritableByteChannel channel, long checkTimestamp, KeySource keySource)
            throws IOException, InterruptedException {
        final DpField[] fields = dpFile.getFields();
        final BufferFieldData fd = new BufferFieldData();
        final SnapWithTimestamp snapWithTimestamp = new SnapWithTimestamp();

        final int batchSize = this.numRecordsInBatch;
        int n = 0;
        int size = 0;

        while (keySource.hasNext()) {
            if (Thread.interrupted()) {
                throw new InterruptedException("<writeData> Writing thread was interrupted, stopping writing...");
            }

            final FeedData data = keySource.nextFeedData();
            if (data == null) {
                continue;
            }
            final ByteString alias = keySource.getAlias();

            final int start = this.bb.position();
            synchronized (data) {
                OrderedFeedData ofd = (OrderedFeedData) data;
                initSnap(ofd, dpFile, snapWithTimestamp);
                if (!snapWithTimestamp.isValid(checkTimestamp)) {
                    continue;
                }

                ++size;

                fd.reset(snapWithTimestamp.snap);
                alias.writeTo(this.bb, ByteString.LENGTH_ENCODING_NONE);

                try {
                    appendSnap(fd, ofd.getCreatedTimestamp(), snapWithTimestamp.timestamp, fields);
                } catch (Exception e) {
                    this.logger.warn("<writeData> append failed for " + data.getVwdcode(), e);
                    this.bb.position(start);
                    continue;
                }
            }
            this.bb.put(LF);
            doWrite(channel);

            if (++n == batchSize) {
                sleep();
                n = 0;
            }
        }

        if (this.bb.position() != 0) {
            doWrite(channel);
        }

        dpFile.setSize(size); // add most current size so that it will be logged correctly
    }

    private void initSnap(OrderedFeedData data, DpFile dpFile, SnapWithTimestamp swt) {
        final OrderedSnapData rtSnap = data.getSnapData(true);
        if (!dpFile.isRealtime()) {
            final OrderedSnapData ntSnap = data.getSnapData(false);
            if (ntSnap != null && ntSnap.isInitialized()) {
                byte[] tmp = (rtSnap != null && rtSnap.isInitialized())
                        ? merge(rtSnap, ntSnap) : ntSnap.getData(false);
                swt.reset(tmp, ntSnap.getLastUpdateTimestamp());
                return;
            }
        }
        if (rtSnap != null && rtSnap.isInitialized()) {
            swt.reset(rtSnap.getData(false), rtSnap.getLastUpdateTimestamp());
        }
        else {
            swt.reset(null, 0);
        }
    }

    private byte[] merge(SnapData rt, SnapData nt) {
        final byte[] rtCopy = rt.getData(true);
        final byte[] merged = this.fieldDataMerger.merge(new BufferFieldData(rtCopy),
                new BufferFieldData(nt.getData(false)));
        return (merged != null) ? merged : rtCopy;
    }

    private void sleep() throws IOException {
        try {
            TimeUnit.MILLISECONDS.sleep(this.sleepMillisAfterBatch);
        } catch (InterruptedException e) {
            this.logger.warn("<writeData> interrupted");
            Thread.currentThread().interrupt();
            throw new IOException("incomplete due to interrupt");
        }
    }

    private void appendSnap(final BufferFieldData fd, int createdTimestamp, int lastUpdateTimestamp,
            DpField[] fields) {
        final byte fs = (byte) dpFile.getFieldSeparator();
        int n = 0;
        while (n < fields.length && fields[n].getOrderId() < 0) {
            this.bb.put(fs);
            if (fields[n].getOrderId() == DpField.OID_TIMEOFARR) {
                bytes.setSecondOfDay(DateTimeProvider.Timestamp.decodeTime(lastUpdateTimestamp));
                bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
            }
            else if (fields[n].getOrderId() == DpField.OID_DATEOFARR) {
                int value = DateTimeProvider.Timestamp.decodeDate(lastUpdateTimestamp);
                if (this.dpFile.isIsoDateFormat()) {
                    this.bytes.setIsoYyyyMmDd(value);
                }
                else {
                    this.bytes.setYyyyMmDd(value);
                }
                bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
            }
            else if (fields[n].getOrderId() == DpField.OID_CREATED) {
                if (createdTimestamp > 0) {
                    bytes.setIsoYyyyMmDd(DateTimeProvider.Timestamp.decodeDate(createdTimestamp));
                    bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
                    bb.put((byte) 'T');
                    bytes.setSecondOfDay(DateTimeProvider.Timestamp.decodeTime(createdTimestamp));
                    bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
                }
            }
            else if (fields[n].getOrderId() == DpField.OID_UPDATED) {
                bytes.setIsoYyyyMmDd(DateTimeProvider.Timestamp.decodeDate(lastUpdateTimestamp));
                bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
                bb.put((byte) 'T');
                bytes.setSecondOfDay(DateTimeProvider.Timestamp.decodeTime(lastUpdateTimestamp));
                bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
            }
            else {
                throw new IllegalStateException();
            }
            n++;
        }
        if (n == fields.length) {
            return;
        }

        int nextId = fields[n].getOrderId();

        for (int oid = fd.readNext(); oid != 0; oid = fd.readNext()) {
            while (oid > nextId) {
                this.bb.put(fs);
                if (++n == fields.length) {
                    return;
                }
                nextId = fields[n].getOrderId();
            }
            if (oid == nextId) {
                this.bb.put(fs);
                formatValue(fd, fields[n]);
                bb.put(bytes.getData(), bytes.getOffset(), bytes.getLength());
                if (++n == fields.length) {
                    return;
                }
                nextId = fields[n].getOrderId();
            }
            else {
                fd.skipCurrent();
            }
        }
        while (n++ < fields.length) {
            this.bb.put(fs);
        }
    }

    private void setDate(int value) {
        if (this.dpFile.isIsoDateFormat()) {
            bytes.setIsoDate(value);
        }
        else {
            bytes.setDate(value);
        }
    }

    private void formatValue(BufferFieldData fd, DpField dpField) {
        switch (dpField.getType()) {
            case DATE:
                setDate(fd.getInt());
                break;
            case TIME:
                bytes.setTime(fd.getInt());
                break;
            case UINT:
            case USHORT:
                bytes.setNumber(fd.getUnsignedInt());
                break;
            case PRICE:
                bytes.setPrice(fd.getInt(), fd.getByte());
                break;
            case STRING:
                final byte[] b = fd.getBytes();
                this.bytes.setString(b, 0, getLength(b));
                break;
            default:
                throw new IllegalArgumentException("unknown field type: this should never happen");
        }
    }

    private static int getLength(byte[] b) {
        int n = 0;
        while (n < b.length && b[n] != 0) {
            n++;
        }
        return n;
    }
}
