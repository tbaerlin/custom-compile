/*
 * ByteChannelUtils.java
 *
 * Created on 24.01.12 13:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author oflege
 */
public class ByteChannelUtils {
    /**
     * A WritableByteChannel that buffers writes before it forwards them to its delegate. Useful if
     * the target is a FileChannel and there are lots of small writes.
     */
    public static class BufferedWritableByteChannel implements WritableByteChannel {

        private final WritableByteChannel delegate;

        private final ByteBuffer bb;

        private long numBytesWritten = 0L;

        public BufferedWritableByteChannel(WritableByteChannel delegate, ByteBuffer bb) {
            this.delegate = delegate;
            this.bb = bb;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            final int result = src.remaining();
            while (src.hasRemaining()) {
                if (this.bb.remaining() < src.remaining()) {
                    flush();
                }
                if (src.remaining() <= this.bb.remaining()) {
                    this.bb.put(src);
                } else {
                    final int oldLimit = src.limit();
                    src.limit(src.position() + this.bb.remaining());
                    this.bb.put(src);
                    src.limit(oldLimit);
                }
            }
            this.numBytesWritten += result;
            return result;
        }

        @Override
        public boolean isOpen() {
            return this.delegate.isOpen();
        }

        @Override
        public void close() throws IOException {
            flush();
            this.delegate.close();
        }

        public void flush() throws IOException {
            this.bb.flip();
            while (this.bb.hasRemaining()) {
                this.delegate.write(this.bb);
            }
            this.bb.clear();
        }

        public long getNumBytesWritten() {
            return this.numBytesWritten;
        }
    }

    /**
     * Stores data written to it in a delegate ByteBuffer. That buffer will not overflow, if it is full,
     * additional data will be discarded. Instances can also be configured to ignore the first n
     * bytes that are written.
     */
    public static class ByteBufferWritableByteChannel implements WritableByteChannel {
        private final ByteBuffer delegate;

        private int from;

        public ByteBufferWritableByteChannel(ByteBuffer delegate) {
            this(delegate, 0);
        }

        public ByteBufferWritableByteChannel(ByteBuffer delegate, int from) {
            this.delegate = delegate;
            this.from = from;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            final int result = src.remaining();
            if (this.from != 0) {
                final int n = Math.min(this.from, src.remaining());
                src.position(src.position() + n);
                this.from -= n;
            }
            if (src.hasRemaining() && this.delegate.hasRemaining()) {
                final int length = Math.min(src.remaining(), this.delegate.remaining());
                if (length < src.remaining()) {
                    int srcLimit = src.limit();
                    src.limit(src.position() + length);
                    this.delegate.put(src);
                    src.limit(srcLimit);
                } else {
                    this.delegate.put(src);
                }
            }
            return result;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void close() throws IOException {
            // empty
        }
    }

}
