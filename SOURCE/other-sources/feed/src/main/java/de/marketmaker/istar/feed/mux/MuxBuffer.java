/*
 * MuxBuffer.java
 *
 * Created on 12.03.14 07:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.IoUtils;

/**
 * A {@link MuxOutput} that either forwards data it receives
 * directly to its delegate <tt>MuxInputReceiver</tt>, or, if buffering has been enabled, writes
 * data it receives to a temporary file. After buffering is disabled again, incoming data will
 * still be appended to the file until all data from the file has been forwarded to the delegate.
 *
 * @author oflege
 */
@ManagedResource
public class MuxBuffer implements MuxOutput {

    private enum State {
        PASS_THROUGH, BUFFERING, RECOVERING
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.PASS_THROUGH);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final MuxOutput delegate;

    private final File directory;

    private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1 << 18);

    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1 << 18);

    // This value will allow a maximum of <value in bytes>/0.1s to be read from the buffer file
    // If this is changed probably at least the size of readBuffer above needs to be adjusted, too.
    private int maxNumBytesOutPer10thSec = 256 * 1024;

    private int numBytesOut = 0;

    private long numBytesOutResetAt;

    private long maxBufferFileSize = 10 * (1L << 30); // 10Gb

    private boolean logMaxBufferFileSizeExceeded = true;

    private long readPos;

    private long writePos;

    private FileChannel fc;

    private File f;

    public MuxBuffer(MuxOutput delegate, File directory) {
        this.delegate = delegate;
        this.directory = directory;
        prepareWriteBuffer();
    }

    public void setMaxBufferFileSize(long maxBufferFileSize) {
        this.maxBufferFileSize = maxBufferFileSize;
    }

    @ManagedAttribute
    public int getMaxNumBytesOutPer10thSec() {
        return maxNumBytesOutPer10thSec;
    }

    @ManagedAttribute
    public void setMaxNumBytesOutPer10thSec(int maxNumBytesOutPer10thSec) {
        this.maxNumBytesOutPer10thSec = maxNumBytesOutPer10thSec;
    }

    public boolean isEnabled() {
        return state.get() != State.PASS_THROUGH;
    }

    public boolean enable(boolean enable) {
        if (enable) {
            if (this.state.compareAndSet(State.PASS_THROUGH, State.BUFFERING)) {
                this.logger.info("<enable> start buffering");
                return true;
            }
        }
        else if (this.state.compareAndSet(State.BUFFERING, State.RECOVERING)) {
            this.logger.info("<enable> start recovering");
            return true;
        }
        this.logger.warn("<enable> cannot " + (enable ? "enable" : "disable") + " buffering in state " + this.state.get());
        return false;
    }

    @Override
    public void onInClosed() {
        this.delegate.onInClosed();
    }

    @Override
    public void reset() {
        this.delegate.reset();
    }

    @Override
    public boolean isAppendOnlyCompleteRecords() {
        return true;
    }

    public long getWritePos() {
        return writePos;
    }

    @Override
    public void append(ByteBuffer in) throws IOException {
        switch (this.state.get()) {
            case PASS_THROUGH:
                this.delegate.append(in);
                break;
            case BUFFERING:
                appendToBuffer(in);
                break;
            case RECOVERING:
                recover(in);
                break;
            default:
                throw new IllegalStateException(this.state + "");
        }
    }

    private void recover(ByteBuffer in) throws IOException {
        if (this.readPos == this.writePos) {
            endRecover(in);
            return;
        }

        appendToBuffer(in);

        try {
            appendFromFile();
        } catch (IOException e) {
            this.logger.error("<appendFromFile> failed", e);
            cleanup();
        }
    }

    private void endRecover(ByteBuffer in) throws IOException {
        try {
            if (this.writeBuffer.position() > 4) {
                this.writeBuffer.flip().position(4);
                this.delegate.append(this.writeBuffer);
            }
            this.delegate.append(in);
        } finally {
            cleanup();
        }
    }

    private boolean shouldAppendFromFile() {
        if (this.numBytesOut <= this.maxNumBytesOutPer10thSec) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now < this.numBytesOutResetAt) {
            return false;
        }
        this.numBytesOutResetAt = now + 100;
        this.numBytesOut = 0;
        return true;
    }

    private void appendFromFile() throws IOException {
        if (!shouldAppendFromFile()) {
            return;
        }
        seek(this.readPos);
        int numBytesRead = this.fc.read(this.readBuffer);
        if (numBytesRead == -1) {
            throw new IOException("unexpected EOF");
        }

        this.readPos += numBytesRead;

        this.readBuffer.flip();
        while (this.readBuffer.remaining() >= 4) {
            this.readBuffer.mark();
            int length = this.readBuffer.getInt();
            if (this.readBuffer.remaining() < length) {
                this.readBuffer.reset();
                break;
            }
            final int end = this.readBuffer.position() + length;
            final ByteBuffer out = (ByteBuffer) this.readBuffer.duplicate().limit(end);
            this.numBytesOut += out.remaining();
            this.delegate.append(out);
            this.readBuffer.position(end);
        }
        this.readBuffer.compact();
    }

    private void appendToBuffer(ByteBuffer in) {
        while (in.remaining() > this.writeBuffer.remaining()) {
            if (this.writeBuffer.position() > 4) {
                appendToFile();
            }
            else {
                this.logger.error("<appendToBuffer> in too large: "
                        + in.remaining() + " vs " + this.writeBuffer.remaining());
                return;
            }
        }
        this.writeBuffer.put(in);
    }

    private void appendToFile() {
        this.writeBuffer.putInt(0, this.writeBuffer.position() - 4);
        this.writeBuffer.flip();

        try {
            if (isMaxFileSizeExceeded()) {
                return;
            }
            ensureFileChannel();
            seek(this.writePos);
            this.writePos += this.writeBuffer.remaining();
            while (this.writeBuffer.hasRemaining()) {
                fc.write(this.writeBuffer);
            }
        } catch (IOException e) {
            this.logger.error("<appendToFile> failed", e);
            cleanup();
        } finally {
            prepareWriteBuffer();
        }
    }

    private boolean isMaxFileSizeExceeded() {
        if (this.writePos <= this.maxBufferFileSize) {
            return false;
        }
        if (this.logMaxBufferFileSizeExceeded) {
            this.logger.error("<appendToFile> max file size exceeded for " + f.getAbsolutePath());
            this.logMaxBufferFileSizeExceeded = false;
        }
        return true;
    }

    private void prepareWriteBuffer() {
        this.writeBuffer.clear().position(4);
    }

    private void seek(final long pos) throws IOException {
        if (this.fc.position() != pos) {
            this.fc.position(pos);
        }
    }

    private void ensureFileChannel() throws IOException {
        if (this.fc == null) {
            this.f = File.createTempFile("mux-", "buf", this.directory);
            this.fc = new RandomAccessFile(f, "rw").getChannel();
            this.logger.info("create buffer file " + f.getAbsolutePath());
            this.readPos = 0;
            this.writePos = 0;
            this.readBuffer.clear();
        }
    }

    private void cleanup() {
        this.logger.info("<cleanup> readPos=" + this.readPos + ", writePos=" + this.writePos + "...");
        IoUtils.close(this.fc);
        this.fc = null;
        if (this.f != null && this.f.isFile() && !this.f.delete()) {
            this.logger.error("<cleanup> failed to delete " + f.getAbsolutePath());
        }
        this.f = null;
        this.readPos = 0;
        this.writePos = 0;
        this.numBytesOut = 0;
        this.logMaxBufferFileSizeExceeded = true;
        this.state.set(State.PASS_THROUGH);
        prepareWriteBuffer();
        this.logger.info("<cleanup> finished, buffer is disabled");
    }
}
