/*
 * ByteBufferOutputStream.java
 *
 * Created on 14.09.2010 14:26:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Extends {@link java.io.ByteArrayOutputStream} to provide the {@link #toBuffer()} method
 * which does not copy the internal byte array but rather wraps it. For larger arrays, it is much
 * more efficient to create a small ByteBuffer object then to create a new array just for the sake
 * of ignoring the unused bytes at the end in the original byte array.
 * @author oflege
 */
public class ByteBufferOutputStream extends ByteArrayOutputStream {
    public ByteBufferOutputStream() {
    }

    public ByteBufferOutputStream(int size) {
        super(size);
    }

    public ByteBuffer toBuffer() {
        return ByteBuffer.wrap(this.buf, 0, size());
    }
}
