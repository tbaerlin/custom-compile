/*
 * ByteBufferInputStream.java
 *
 * Created on 20.06.12 09:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An InputStream that gets its data from a ByteBuffer.
 * @author oflege
 */
public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer bb;

    public ByteBufferInputStream(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public int available() throws IOException {
        return this.bb.remaining();
    }

    @Override
    public long skip(long n) throws IOException {
        final long result = Math.min(n, this.bb.remaining());
        this.bb.position(this.bb.position() + (int) result);
        return result;
    }

    @Override
    public int read() throws IOException {
        return this.bb.hasRemaining() ? (this.bb.get() & 0xFF) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (!this.bb.hasRemaining()) {
            return -1;
        }
        final int result = Math.min(bb.remaining(), len);
        this.bb.get(b, off, result);
        return result;
    }
}
