/*
 * MdpsParserErrorHandler.java
 *
 * Created on 30.11.2006 13:33:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Dumps feed records into files so that they can be parsed and debugged.
 * Writing happens asynchronously, the number of pending writes is limited so that it is
 * more or less unlikely that this component becomes a bottleneck or consumes too much memory.
 * Each record in the file starts with an int that specifies the length of the following record.
 * <p>
 * In addition to dumping records, problems may also be logged. Logging is limited to 60msgs/s,
 * that can be changed by calling {@link #setMaxLogsPerMinute(int)}.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class ParserErrorHandlerImpl implements ParserErrorHandler, InitializingBean,
        DisposableBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Name of primary dump file, backup files will have the same name plus the backup number
     */
    private File dumpFile;

    private long maxDumpFileSize = 10 * 1024 * 1024;

    private long dumpFileSize = 0;

    private int numBackupFiles = 4;

    private int writeBufferSize = 128 * 1024;

    protected ByteBuffer bb;

    private FileChannel fc;

    private boolean logExceptions = false;

    private boolean logErrors = true;

    private int logMinuteOfDay = -1;

    private int numLogsPerMinute = 0;

    private int maxLogsPerMinute = 60;

    // single threaded executor for async writing
    private final ExecutorService es = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10), // limits size of pending writes to 10*writeBufferSize
            r -> new Thread(r, ParserErrorHandlerImpl.this.getClass().getSimpleName()));

    public void setLogErrors(boolean logErrors) {
        this.logErrors = logErrors;
    }

    public void setLogExceptions(boolean logExceptions) {
        this.logExceptions = logExceptions;
    }

    public void setMaxLogsPerMinute(int maxLogsPerMinute) {
        this.maxLogsPerMinute = maxLogsPerMinute;
    }

    public void setDumpFile(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    public void setNumBackupFiles(int numBackupFiles) {
        this.numBackupFiles = numBackupFiles;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public void setMaxDumpFileSize(long maxDumpFileSize) {
        this.maxDumpFileSize = maxDumpFileSize;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.dumpFile == null) {
            throw new IllegalStateException("dumpFile not set");
        }
        if (!this.dumpFile.getParentFile().isDirectory() && !this.dumpFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("failed to create directory "
                    + this.dumpFile.getParentFile().getAbsolutePath());
        }
        this.dumpFileSize = this.dumpFile.length();
        this.fc = new RandomAccessFile(this.dumpFile, "rw").getChannel();
        this.bb = ByteBuffer.allocate(this.writeBufferSize);
    }

    protected Future<?> write(final boolean shutdown, final boolean rotate) {
        this.bb.flip();
        final ByteBuffer tmp = this.bb;
        this.bb = ByteBuffer.allocate(tmp.capacity());
        return submitForWriting(tmp, shutdown, rotate);
    }

    private Future<?> submitForWriting(final ByteBuffer buffer, final boolean shutdown,
            final boolean rotate) {
        try {
            return this.es.submit(() -> write(buffer, shutdown, rotate));
        } catch (RejectedExecutionException e) {
            this.logger.error("<write> was rejected");
            return null;
        }
    }

    private void write(ByteBuffer tmp, boolean shutdown, boolean rotate) {
        this.dumpFileSize += tmp.remaining();
        try {
            this.fc.write(tmp);
            if (!shutdown && (this.dumpFileSize < this.maxDumpFileSize && !rotate)) {
                return;
            }

            this.fc.close();
            if (shutdown) {
                this.logger.info("<write> closed file for shutdown");
                return;
            }

            rotate();

            this.fc = new RandomAccessFile(this.dumpFile, "rw").getChannel();
            this.dumpFileSize = 0;
        } catch (IOException e) {
            this.logger.warn("<write> failed", e);
        }
    }

    private void rotate() {
        final File f = getFile(this.numBackupFiles);
        if (f.exists() && !f.delete()) {
            this.logger.warn("<rotate> failed to delete " + f.getAbsolutePath());
        }

        for (int i = this.numBackupFiles; i > 0; i--) {
            final File target = getFile(i);
            final File src = getFile(i - 1);
            if (src.exists() && !src.renameTo(target)) {
                this.logger.warn("<rotate> failed to rename " + src.getAbsolutePath()
                        + " to " + target.getAbsolutePath());
            }
        }
    }

    private File getFile(int i) {
        return (i == 0)
                ? this.dumpFile
                : new File(this.dumpFile.getParent(), this.dumpFile.getName() + "." + i);
    }


    public synchronized void destroy() throws Exception {
        final Future<?> f = write(true, false);
        if (f != null) {
            f.get();
        }
        es.shutdown();
        if (f == null) {
            this.fc.close();
        }
        this.bb = null;
    }

    @ManagedOperation
    public synchronized void flush() {
        write(false, true);
    }

    public void handle(FeedRecord fr, ParsedRecord pr, Throwable t) {
        final byte[] bytes = fr.getData();
        final int length = fr.getEnd() - fr.getOffset();
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        append(buffer.array(), 0, 4);
        append(bytes, fr.getOffset(), length);
        handle(t);
    }

    @Override
    public void handle(ByteBuffer buffer, Throwable t) {
        append(buffer);
        handle(t);
    }

    private synchronized void append(ByteBuffer buffer) {
        if (this.bb == null) {
            return;
        }
        if (this.bb.remaining() < buffer.remaining()) {
            write(false, false);
            if (bb.remaining() < buffer.remaining()) {
                submitForWriting(ByteBuffer.allocate(buffer.remaining()).put(buffer), false, false);
                return;
            }
        }
        this.bb.put(buffer);
    }

    protected void handle(Throwable t) {
        if (t instanceof InvalidFieldsException) {
            if (!isLogLimitExceeded()) {
                String msg = t.getMessage();
                if (t.getCause() != null) {
                    msg = msg + " - " + t.getCause().getMessage();
                }
                this.logger.warn("<handle> invalid field(s) for " + msg);
            }
        }
        else if (t instanceof Exception) {
            if (this.logExceptions && !isLogLimitExceeded()) {
                if (t instanceof IllegalKeyException) {
                    this.logger.error("<handle> parse exception: " + t.getMessage());
                }
                else {
                    this.logger.error("<handle> parse exception", t);
                }
            }
        }
        else if (this.logErrors && !isLogLimitExceeded()) {
          if (t == null) {
            this.logger.error("<handle> parse error without attached Throwable");
          } else {
              this.logger.error("<handle> parse error: " + t.getMessage(), t);
          }
        }
    }

    private boolean isLogLimitExceeded() {
        final int mod = new DateTime().getMinuteOfDay();
        if (mod != this.logMinuteOfDay) {
            if (this.numLogsPerMinute > this.maxLogsPerMinute) {
                this.logger.warn("<isLogLimitExceeded> ignored "
                        + (this.numLogsPerMinute - this.maxLogsPerMinute)
                        + " log requests in minute " + this.logMinuteOfDay);
            }
            this.logMinuteOfDay = mod;
            this.numLogsPerMinute = 0;
        }
        return (this.numLogsPerMinute++ > this.maxLogsPerMinute);
    }

    protected synchronized void append(byte[] b, int offset, int length) {
        if (this.bb == null) {
            return;
        }
        if (bb.remaining() < length) {
            write(false, false);
            if (bb.remaining() < length) {
                this.logger.warn("<append> record too long");
                return;
            }
        }
        this.bb.put(b, offset, length);
    }
}
