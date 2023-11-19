/*
 * FileBufferedBufferWriter.java
 *
 * Created on 22.11.2004 11:07:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.util;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.netflix.servo.annotations.Monitor;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.connect.BufferWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.netflix.servo.annotations.DataSourceType.GAUGE;

/**
 * A BufferWriter that stores the contents of each received ByteBuffer in files iff its delegate
 * BufferWriter is not fast enough.
 * Relies on a Disruptor/RingBuffer to forward data to its delegate.
 * There are three threads involved:
 * <ol>
 * <li>
 * the writer thread either puts received buffers into the RingBuffer or, if a file is currently
 * being written or the RingBuffer is full, appends them to a file.</li>
 * <li>
 * the RingBuffer's client thread retrieves data from the RingBuffer and forwards it
 * to the delegate. When the RingBuffer is drained and data has been written to a file, this thread
 * submits a read task to the ExecutorService that runs as the third thread </li>
 * <li>
 * a read thread reads data from the file(s) written by the first thread and tries to
 * catch up with that thread; as soon as that happens, the task will finish</li>
 * </ol>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Deprecated // (didn't find any references besides a unit test)
@ManagedResource
public class FileBufferedBufferWriter implements BufferWriter, InitializingBean, DisposableBean,
        Lifecycle, BeanNameAware, EventFactory<ByteBuffer>, EventHandler<ByteBuffer> {

    // common data for read/write
    private abstract class FileBufferData implements Closeable {
        // overflow data
        final File file;

        final FileChannel channel;

        final ByteBuffer bb;

        int position = 0;

        FileBufferData(File file, String mode, ByteBuffer bb) throws FileNotFoundException {
            this.file = file;
            this.channel = new RandomAccessFile(file, mode).getChannel();
            this.bb = bb;
            this.bb.clear();
        }

        @Override
        public void close() throws IOException {
            this.channel.close();
        }

        boolean shouldRotate() {
            return this.position > maxFileSize;
        }
    }

    /**
     * Reads data from files written by the Writer
     */
    private class Reader extends FileBufferData {
        private int numFailed = 0;

        // if readNext fails continuously, this flag helps to limit the amount of logging
        private boolean lastFailed = false;

        Reader(File f, ByteBuffer bb) throws FileNotFoundException {
            super(f, "r", bb);
            bb.clear().flip();
        }

        @Override
        public void close() throws IOException {
            super.close();
            delete(this.file);
            if (this.numFailed > 0) {
                logger.error("<close> " + this.file.getName() + ", read failed for " + this.numFailed + " messages");
            }
        }

        // read from file into ByteBuffer
        int readNext() throws IOException {
            while (this.bb.remaining() < 4) {
                fillBuffer(4);
            }
            final int length = this.bb.getInt();
            while (this.bb.remaining() < length) {
                fillBuffer(length);
            }
            final int oldLimit = this.bb.limit();
            final int newLimit = this.bb.position() + length;
            this.bb.limit(newLimit);
            try {
                delegate.write(this.bb);
                this.lastFailed = false;
            } catch (Throwable t) {
                numFailed++;
                if (!this.lastFailed) {
                    logger.error("<readNext> failed in delegate", t);
                    this.lastFailed = true;
                }
            }
            this.bb.limit(oldLimit);
            this.bb.position(newLimit);

            final int msgLength = length + 4;
            this.position += msgLength;
            return msgLength;
        }

        private int fillBuffer(int numRequired) throws IOException {
            if (this.bb.remaining() < numRequired) {
                this.bb.compact();  // partial write
            }
            final int n = this.channel.read(this.bb);
            this.bb.flip();
            return n;
        }
    }

    /**
     * Writes ByteBuffer contents to files
     */
    private class Writer extends FileBufferData {
        Writer(File f) throws FileNotFoundException {
            super(f, "rw", ByteBuffer.allocate(4));
        }

        void append(ByteBuffer buffer) throws IOException {
            this.bb.clear();
            this.bb.putInt(buffer.remaining()).flip();
            writeAll(this.bb);
            writeAll(buffer);
        }

        private void writeAll(ByteBuffer buffer) throws IOException {
            int toWrite = buffer.remaining();
            this.position += toWrite;
            while (toWrite > 0) {
                toWrite -= this.channel.write(buffer);
            }
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * size of RingBuffer slots, must at least be the max. size of ByteBuffer submitted to the
     * {@link #write(java.nio.ByteBuffer)} method
     */
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private Writer writer;

    private Reader reader;

    private RingBuffer<ByteBuffer> ringBuffer;

    private Disruptor<ByteBuffer> disruptor;

    private ExecutorService executor;

    private boolean myExecutor = true;

    // the target for the incoming data
    private BufferWriter delegate;

    private String beanName;

    private volatile boolean stop = false;

    private File storeDir = new File(System.getProperty("java.io.tmpdir"));

    private int ringBufferSize = 16;

    @Monitor(type = GAUGE)
    private final AtomicLong numBytesPending = new AtomicLong(); // amount of data in the ringbuffer

    private final AtomicBoolean readerScheduledOrRunning = new AtomicBoolean();

    /**
     * overflow buffer
     * writer adds files in order of creation so that reader can retrieve them for reading
     */
    private final BlockingQueue<File> files = new LinkedBlockingDeque<>();

    private int maxFileSize = 100 * 1024 * 1024;

    /**
     * Remembers the last sequence that was used to publish to the ringBuffer;
     */
    private long lastWriteSequence = 0L;

    /**
     * The value of lastWriteSequence that was used <em>before</em> we switched from publishing
     * data to the ringBuffer to writing data to a file (the overflow);
     * only when the 2nd thread (consumer) sees this sequence number it can submit the task
     * that is executed in the 3rd thread.
     */
    private final AtomicLong readerTriggerSequence = new AtomicLong(-1);

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
    }

    public void setStoreDir(File storeDir) {
        this.storeDir = storeDir;
    }

    @Override
    public void setBeanName(String s) {
        this.beanName = s;
    }

    public void setDelegate(BufferWriter delegate) {
        this.delegate = delegate;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
        this.myExecutor = false;
    }

    @ManagedAttribute
    public long getNumPendingBytes() {
        return this.numBytesPending.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!this.storeDir.isDirectory() && !this.storeDir.mkdirs()) {
            throw new IllegalArgumentException("not a directory: " + this.storeDir.getAbsolutePath());
        }
        if (this.executor == null) {
            this.executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    new ThreadFactory() {
                        private final AtomicInteger no = new AtomicInteger();
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, beanName + "-" + no.incrementAndGet());
                        }
                    });
        }

        this.disruptor = new Disruptor<>(this, this.ringBufferSize,
                this.executor, ProducerType.MULTI, new BlockingWaitStrategy());
        this.disruptor.handleEventsWith(this);
    }

    @Override
    public void destroy() throws Exception {
        IoUtils.close(this.writer);
        IoUtils.close(this.reader);

        for (File file : this.files) {
            delete(file);
        }
    }

    @Override
    public boolean isRunning() {
        return this.ringBuffer != null;
    }

    @Override
    public void start() {
        this.ringBuffer = this.disruptor.start();
    }

    @Override
    public void stop() {
        this.stop = true;

        this.disruptor.halt();
        if (this.myExecutor) {
            this.executor.shutdown();
            try {
                if (this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    this.logger.info("<stop> done");
                }
                else {
                    this.logger.warn("<stop> executor did not terminate");
                }
            } catch (InterruptedException e) {
                this.logger.error("<stop> interrupted?!");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void runReader() {
        try {
            doRunReader();
        } finally {
            this.readerScheduledOrRunning.set(false);
        }
    }

    private void doRunReader() {
        this.logger.info("<runReader> started");
        int numBytesRead;
        do {
            if (this.stop) {
                this.logger.info("<runReader> stopped");
                return;
            }
            try {
                numBytesRead = doRead();
            } catch (Throwable e) {
                this.logger.error("<runReader> FATAL error", e);
                return;
            }
        } while (this.numBytesPending.addAndGet(-numBytesRead) != 0L);
        this.logger.info("<runReader> finished");
    }

    private int doRead() throws IOException, InterruptedException {
        if (this.reader == null || this.reader.shouldRotate()) {
            this.reader = rotateReader();
        }
        return this.reader.readNext();
    }

    // create a new reader
    private Reader rotateReader() throws FileNotFoundException, InterruptedException {
        IoUtils.close(this.reader);
        // re-use the byte buffer
        final Reader result = new Reader(this.files.take(),
                this.reader != null ? this.reader.bb : ByteBuffer.allocate(this.bufferSize + 4));
        this.logger.info("<createReader> for " + result.file.getName());
        return result;
    }

    private void delete(final File file) {
        if (!file.delete()) {
            this.logger.warn("<delete> failed for " + file.getName());
        }
        else {
            this.logger.info("<delete> succeeded for " + file.getName());
        }
    }

    @Override
    public ByteBuffer newInstance() {
        return ByteBuffer.allocate(this.bufferSize);
    }

    // called by the disruptor on an executor thread
    @Override
    public void onEvent(ByteBuffer event, long sequence, boolean endOfBatch) {
        try {
            this.delegate.write(event);
        } catch (Exception e) {
            this.logger.error("<onEvent> write failed for " + sequence, e);
        } finally {
            // this has to be 'finally', so we always act on a matching sequence
            this.logger.debug("<onEvent> sequence: " + sequence + " endOfBatch: " + endOfBatch
            + " readerTriggerSequence: " + this.readerTriggerSequence.get());
            if (endOfBatch && this.readerTriggerSequence.get() == sequence) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<onEvent> submit reader after " + sequence);
                }
                submitReader();
            }
        }
    }

    private void submitReader() {
        this.logger.warn("<submitReader> setting readerScheduledOrRunning to true");
        if (!this.readerScheduledOrRunning.compareAndSet(false, true)) {
            this.logger.warn("<submitReader> when readerScheduledOrRunning is true?!", new Exception());
            return;
        }
        this.executor.submit(this::runReader);
    }

    @Override
    public void write(ByteBuffer bb) throws IOException {
        Thread.yield();
        if (this.numBytesPending.get() > 0) {
            appendToFile(bb, true); // buffer is full we might need to submit a reader
        }
        else {
            try {
                this.lastWriteSequence = this.ringBuffer.tryNext(1);
                publish(bb, this.lastWriteSequence);
            } catch (InsufficientCapacityException e) {
                // the sequence when the file-reader needs to be started since the buffer is drained
                this.readerTriggerSequence.set(this.lastWriteSequence);
                this.logger.info("<write> insufficient capacity, last = " + this.lastWriteSequence
                        + " overflow will be appended to file");
                // do not request to submit the reader as the buffer has to be drained first;
                // the reader will be submitted after the readerTriggerSequence has been processed.
                appendToFile(bb, false);
            }
        }
    }

    boolean hasBytesToRead() {
        return this.numBytesPending.get() != 0;
    }

    private void appendToFile(ByteBuffer bb, boolean maySubmitReader) throws IOException {
        if (this.writer == null || this.writer.shouldRotate()) {
            this.writer = createWriter();
        }
        final int remaining = bb.remaining();  // remaining: bytes to write
        this.writer.append(bb);
        if (this.numBytesPending.getAndAdd(4L + remaining) == 0L && maySubmitReader) {
            // the reader finished while we were adding, so we have to re-submit the reader
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<appendToFile> submit reader");
            }
            submitReader();
        }
    }

    // create a new writer that is responsible for a single file
    private Writer createWriter() throws IOException {
        IoUtils.close(this.writer);
        Thread.yield();
        final Writer result = new Writer(File.createTempFile(this.beanName + "-", ".buf", this.storeDir));
        this.logger.info("<createWriter> for " + result.file.getName());
        addFile(result);
        return result;
    }

    // this method runs in the main thread
    private void addFile(Writer result) {
        this.files.add(result.file);
        // safety-net if the reader is not running for any reason (it should definitely be running
        // for more than 1 pending file...) -- if this log message appears, the submitReader()-logic
        // has to be re-checked.
        if (this.files.size() > 1
                && !this.readerScheduledOrRunning.get()  // reader is not started until the consumer asked for the last slot
                && ringBuffer.remainingCapacity() == ringBufferSize) { // this means the ringbuffer is empty
            this.logger.warn("<addFile> more than 1 file and no reader running, buffer is empty, queue of files is: "
                + Arrays.toString(files.toArray()) + "");
            submitReader();
        }
    }

    private void publish(ByteBuffer bb, long seq) {
        final ByteBuffer target = this.ringBuffer.get(seq);
        target.clear();
        target.put(bb);
        target.flip();
        this.ringBuffer.publish(seq);
    }
}
