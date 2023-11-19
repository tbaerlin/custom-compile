/*
 * MulticastFeedRipper.java
 *
 * Created on 07.07.11 08:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mcrip;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.lmax.disruptor.EventHandler;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.spring.MidnightEvent;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;

/**
 * Dumps multicasted FeedRecords into files. This class uses 256 files per day and
 * partitions the FeedRecords based on the mini-hashcode contained in the multicast feed.
 * Searching data for a particular vwdcode can therefore be limited to a file that contains only
 * a 1/256th of the daily feed, which should be less than 1GB.<p>
 * In order to keep the files small, the content will be passed through a DeflatorOutputStream
 * before writing it to disk.<p>
 * File Structure: <tt>[ chunk length (int) , chunk length bytes of deflated data ]*</tt><p>
 * To decode the files, first read the length, then all the deflated bytes, inflate them and
 * use the result as input for a {@link de.marketmaker.istar.feed.ordered.MulticastFeedParser}.
 * See {@link RipReader} for an example.
 * <p>
 * Daily file rotation is expected to be triggered externally by publishing a {@link MidnightEvent}.
 * <p>
 * A MulticastFeedRipper needs to coordinate/synchronize three threads:<ul>
 * <li>the multicast receiver which invokes {@link #onEvent(java.nio.ByteBuffer)}</li>
 * <li>the {@link #zip}-Thread that zips full buffers and appends the result to files</li>
 * <li>the external threads that publish the {@link MidnightEvent} (scheduled) or
 * {@link #flushBuffers()} (jmx)</li>
 * </ul>
 * This is done by<ul>
 * <li>synchronizing on {@link #fileBuffers} whenever an element of that array is accessed and
 * for as long as that array element is used or replaced</li>
 * <li>using {@link #fullBuffers} to submit data to the zip-Thread</li>
 * <li>using {@link #flushIndex} to tell the zip-Thread to add items to {@link #fullBuffers}
 * itself, in order to perform the flush/midnight task as fast as possible w/o using twice as
 * many buffers as necessary.</li>
 * </ul>
 *
 * @author oflege
 */
@ManagedResource
public class MulticastFeedRipper implements InitializingBean, Lifecycle,
        EventHandler<ByteBuffer>, OrderedUpdateBuilder, ApplicationListener<MidnightEvent> {

    static final String SUFFIX = ".dat";

    private class FileBuffer implements Closeable {
        private final int id;

        private final LocalDate date;

        private final ByteBuffer buffer;

        private final File file;

        private final FileChannel channel;

        private FileBuffer(int id) {
            this(id, today, createByteBuffer(),
                    getFile(id), createChannel(getFile(id)));
        }

        private FileBuffer(int id, LocalDate date, ByteBuffer buffer, File file,
                FileChannel channel) {
            this.id = id;
            this.date = date;
            this.buffer = buffer;
            this.file = file;
            this.channel = channel;
        }

        FileBuffer copyWithNewBuffer() {
            if (this.date != today) {
                return new FileBuffer(this.id);
            }
            return new FileBuffer(this.id, this.date, createByteBuffer(), this.file, this.channel);
        }

        public void close() {
            IoUtils.close(this.channel);
            MulticastFeedRipper.this.logger.info("<close> " + this.file.getName());
        }

        private int remaining() {
            return this.buffer.remaining();
        }

        void put(ByteBuffer src) {
            this.buffer.put(src);
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    private static final int NUM_BUFFERS = 256;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile boolean stopped = false;

    private final Deflater def = new Deflater(Deflater.BEST_SPEED + 1);

    private final Thread zip = new Thread(MulticastFeedRipper.this::zip,
            getClass().getSimpleName() + "-zip");

    /**
     * holds a FileBuffer for each feed partition; array elements will be replaced whenever
     * a buffer is full or the buffers are flushed (e.g., at midnight). As that happens in
     * different threads, access to this object has to be synchronized
     */
    @GuardedBy("this.fileBuffers")
    private final FileBuffer[] fileBuffers = new FileBuffer[NUM_BUFFERS];

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private File baseDir;

    private final BlockingQueue<FileBuffer> fullBuffers
            = new LinkedBlockingQueue<>(NUM_BUFFERS * 3 / 2);

    private final AtomicLong bytesWritten = new AtomicLong();

    private volatile LocalDate today = new LocalDate();

    // the number of the next buffer to be flushed, or NUM_BUFFERS to indicate nothing to do
    private AtomicInteger flushIndex = new AtomicInteger(NUM_BUFFERS);

    private final BlockingQueue<ByteBuffer> bufferPool
            = new LinkedBlockingQueue<>(NUM_BUFFERS / 2);

    private final ConcurrentSkipListSet<ByteString> markets = new ConcurrentSkipListSet<>();

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @SuppressWarnings("FieldAccessNotGuarded")
    public void afterPropertiesSet() throws Exception {
        for (int i = 0; i < this.fileBuffers.length; i++) {
            this.fileBuffers[i] = new FileBuffer(i);
        }
    }

    private ByteBuffer createByteBuffer() {
        final ByteBuffer pooled = this.bufferPool.poll();
        if (pooled != null) {
            pooled.clear();
            return pooled;
        }
        if (this.zip.isAlive()) { // don't log during startup
            this.logger.info("<createByteBuffer>");
        }
        return ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public boolean isRunning() {
        return !this.stopped && this.zip.isAlive();
    }

    public void start() {
        this.zip.start();
    }

    public void stop() {
        this.logger.info("<stop> stopping...");
        this.stopped = true;
        join(this.zip);

        final ByteBuffer tmp = createWriteBuffer();
        //noinspection FieldAccessNotGuarded
        for (FileBuffer buffer : this.fileBuffers) {
            doZip(buffer, tmp);
            buffer.close();
        }

        this.def.end();
        this.logger.info("<stop> done.");
    }

    @ManagedOperation
    public boolean flushBuffers() {
        if (!this.stopped && this.flushIndex.compareAndSet(NUM_BUFFERS, 0)) {
            this.logger.info("<flushBuffers> ...");
            return true;
        }
        return false;
    }

    private File getFile(int i) {
        final File dir = new File(this.baseDir, createDirname(this.today));
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("could not create " + dir.getAbsolutePath());
            }
            this.logger.info("<getFile> created " + dir.getAbsolutePath());
        }

        return new File(dir, createFilename(this.today, i));
    }

    private FileChannel createChannel(File f) {
        final FileChannel result;
        try {
            result = new RandomAccessFile(f, "rw").getChannel();
            result.position(result.size());
            this.logger.info("<createChannel> " + f.getName());
            return result;
        } catch (IOException e) {
            this.logger.error("<createChannel> failed for " + f.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }

    static String createDirname(LocalDate ld) {
        return ld.toString("yyyyMMdd");
    }

    static String createFilename(LocalDate ld, int i) {
        return ld.toString("'mcdump_'yyyyMMdd-") + i + SUFFIX;
    }

    private void join(final Thread thread) {
        if (Thread.currentThread() == thread) {
            return;
        }
        try {
            thread.join(TimeUnit.SECONDS.toMillis(10));
        } catch (InterruptedException e) {
            this.logger.warn("<join> interrupted while joining " + thread.getName() + "?!");
        }
        if (thread.isAlive()) {
            this.logger.error("<stop> " + thread.getName() + " did not join?!");
        }
    }

    private void zip() {
        final ByteBuffer writeBuffer = createWriteBuffer();

        while (!this.stopped) {
            final FileBuffer toBeZipped;
            try {
                toBeZipped = getNextToZip();
            } catch (InterruptedException e) {
                this.logger.info("<zip> interrupted?!");
                continue;
            }
            if (toBeZipped != null) {
                try {
                    doZip(toBeZipped, writeBuffer);
                } finally {
                    this.bufferPool.offer(toBeZipped.buffer);
                    if (toBeZipped.date != this.today) {
                        toBeZipped.close();
                    }
                }
            }
        }
    }

    private FileBuffer getNextToZip() throws InterruptedException {
        // if fullBuffers were not empty, it could be full, today could just have been incremented,
        // a previous attempt to offer an item to fullBuffers could have failed and now
        // #submitForZipping is trying to put an item to fullBuffers while holding a lock on
        // fullBuffers. In that case, we must absolutely not try to synchronize on fileBuffers
        // (-> deadlock!), but instead poll fullBuffers to make the put succeed.
        // Extremely unlikely, but could happen.
        if (this.fullBuffers.isEmpty() && this.flushIndex.get() < NUM_BUFFERS) {
            int idx;
            synchronized (this.fileBuffers) {
                idx = this.flushIndex.getAndIncrement();
                submitForZipping(this.fileBuffers[idx]);
            }
            if (idx == (NUM_BUFFERS - 1)) {
                this.logger.info("<getNextToZip> submitted last buffer for zipping");
            }
        }
        return this.fullBuffers.poll(1, TimeUnit.SECONDS);
    }

    private ByteBuffer createWriteBuffer() {
        // we use FileChannel#write, which would copy the data into an internal direct buffer
        // before writing if the given buffer is not direct: return a direct buffer
        return ByteBuffer.allocateDirect(this.bufferSize).order(ByteOrder.LITTLE_ENDIAN);
    }

    private void doZip(FileBuffer toBeZipped, ByteBuffer writeBuffer) {
        final long then = System.nanoTime();

        final ByteBuffer buffer = toBeZipped.buffer;
        if (buffer.position() == 0) {
            return;
        }

        if (!deflate(buffer, writeBuffer)) {
            return;
        }
        final int length = writeBuffer.remaining() - 4;

        writeBuffer.putInt(0, length);

        final long now = System.nanoTime();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<zip> from " + buffer.position() + " => " + length
                    + " " + (length * 100 / buffer.position()) + "% in " + (now - then) + "ns");
        }

        try {
            while (writeBuffer.hasRemaining()) {
                toBeZipped.channel.write(writeBuffer);
            }
        } catch (IOException e) {
            this.logger.error("<zip> failed writing to "
                    + toBeZipped.file.getAbsolutePath(), e);
        }

        this.bytesWritten.addAndGet(length + 4);
    }

    private boolean deflate(ByteBuffer source, ByteBuffer target) {
        final DeflaterOutputStream dos
                = new DeflaterOutputStream(new ByteBufferOutputStream(target), this.def);

        try {
            this.def.reset();
            dos.write(source.array(), 0, source.position());
            dos.close();
        } catch (IOException e) {
            this.logger.error("<deflate> failed", e);
            return false;
        }

        target.flip();
        return true;
    }

    /**
     * allows to use this instance as an in-process feed dumper
     */
    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (markets.contains(data.getMarket().getName())) {
            onEvent(update.asMessageWithLength());
        }
    }

    @Override
    public void onEvent(ByteBuffer event, long sequence, boolean endOfBatch) throws Exception {
        onEvent(event);
    }

    private void onEvent(ByteBuffer buffer) {
        final int bufferEnd = buffer.limit();
        while (buffer.hasRemaining()) {
            final int p = buffer.position();
            final int recordEnd = p + (buffer.getShort(p) & 0xFFFF);
            try {
                buffer.limit(recordEnd);
                ripNext(buffer);
                buffer.limit(bufferEnd).position(recordEnd);
            } catch (Throwable t) {
                this.logger.error("<onEvent> failed " + buffer + ", p=" + p
                        + ", rE=" + recordEnd, t);
                saveBuffer(Arrays.copyOfRange(buffer.array(), 0, bufferEnd));
                return;
            }
        }
    }

    private void saveBuffer(final byte[] bytes) {
        final File out = new File(this.baseDir, "error-" + System.currentTimeMillis() + ".mcr");
        try {
            FileCopyUtils.copy(bytes, out);
            this.logger.info("<saveBuffer> in " + out.getAbsolutePath());
        } catch (IOException e) {
            this.logger.error("<saveBuffer> failed for " + out.getAbsolutePath(), e);
        }
    }

    private void ripNext(ByteBuffer buffer) throws IOException, InterruptedException {
        final int hash = buffer.get(buffer.position() + 2) & 0xFF;
        synchronized (this.fileBuffers) {
            getBuffer(hash, buffer.remaining()).put(buffer);
        }
    }

    private FileBuffer getBuffer(int hash, int minRemaining) {
        assert Thread.holdsLock(this.fileBuffers);

        FileBuffer fb = this.fileBuffers[hash];
        if (fb.remaining() >= minRemaining) {
            return fb;
        }
        return submitForZipping(fb);
    }

    private FileBuffer submitForZipping(FileBuffer data) {
        assert Thread.holdsLock(this.fileBuffers);

        final FileBuffer result = data.copyWithNewBuffer();
        this.fileBuffers[data.id] = result;

        if (this.fullBuffers.offer(data)) {
            return result;
        }

        if (result.date == data.date) {
            this.logger.error("<submitForZipping> rejected, losing "
                    + data.buffer.position() + " bytes for id " + data.id);
            this.bufferPool.offer(data.buffer);
        }
        else {
            // data.date != today is used to decide to close data.file, so we have to submit
            // it to make sure the file will be closed - we may have to wait a while which
            // may cause other problems...
            this.logger.warn("<submitForZipping> waiting to put " + data.id + "...");
            try {
                this.fullBuffers.put(data);
                this.logger.warn("<submitForZipping> put " + data.id);
            } catch (InterruptedException e) {
                this.logger.error("<submitForZipping> put interrupted?!");
            }
        }
        return result;
    }

    @Override
    public void onApplicationEvent(MidnightEvent event) {
        // NOT ... = new LocalDate(), as the cron trigger might fire a few ms too early so
        // that today would end up unchanged; using plusDays(1) early does no harm.
        this.today = this.today.plusDays(1);
        this.flushIndex.set(0);
        this.logger.info("<midnight> " + this.today);
    }

    @ManagedAttribute
    public String getMarkets() {
        return String.valueOf(this.markets);
    }

    @ManagedOperation
    public String setMarkets(String s) {
        final Set<ByteString> marketNames = toMarketNames(s);
        if (s.startsWith("+")) {
            this.markets.addAll(marketNames);
        }
        else if (s.startsWith("-")) {
            this.markets.removeAll(marketNames);
        }
        else {
            this.markets.clear();
            this.markets.addAll(marketNames);
        }
        return getMarkets();
    }

    private Set<ByteString> toMarketNames(String s) {
        if (!StringUtils.hasText(s)) {
            return Collections.emptySet();
        }
        String[] names = s.substring(s.startsWith("+") || s.startsWith("-") ? 1 : 0).split(",");
        final HashSet<ByteString> result = new HashSet<>();
        for (String name : names) {
            if (StringUtils.hasText(name)) {
                result.add(new ByteString(name.trim()));
            }
        }
        return result;
    }

    private static class ByteBufferOutputStream extends OutputStream {
        private final ByteBuffer target;

        public ByteBufferOutputStream(ByteBuffer target) {
            this.target = target;
            target.clear();
            target.position(4);
        }

        public void write(int b) throws IOException {
            target.put((byte) b);
        }

        public void write(byte[] b) throws IOException {
            target.put(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            target.put(b, off, len);
        }
    }
}
