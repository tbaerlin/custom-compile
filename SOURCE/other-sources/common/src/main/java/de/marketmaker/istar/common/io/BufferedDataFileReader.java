/*
 * DataFileWriter.java
 *
 * Created on 10.07.12 17:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zzhao
 */
public class BufferedDataFileReader implements Closeable {
    private final ByteBuffer bb = ByteBuffer.allocate(1024 * 8);

    private final DataFile df;

    public BufferedDataFileReader(File f) throws IOException {
        this.df = new DataFile(f, true);
        this.df.read(bb);
        this.bb.flip();
    }

    public long size() throws IOException {
        return this.df.size();
    }

    public void position(long pos) throws IOException {
        this.bb.clear();
        this.df.seek(pos);
        this.df.read(bb);
        this.bb.flip();
    }

    public byte read() throws IOException {
        if (!this.bb.hasRemaining()) {
            final int read = fileToBuffer();
            if (read == 0) {
                throw new EOFException();
            }
        }
        return this.bb.get();
    }

    public long readLong() throws IOException {
        if (this.bb.remaining() < 8) {
            final int read = fileToBuffer();
            if (read == 0) {
                throw new EOFException();
            }
        }
        return this.bb.getLong();
    }

    private int fileToBuffer() throws IOException {
        this.bb.compact();
        final int read = this.df.read(this.bb);
        this.bb.flip();
        return read;
    }

    public void close() throws IOException {
        this.df.close();
    }

    public int readInt() throws IOException {
        if (this.bb.remaining() < 4) {
            final int read = fileToBuffer();
            if (read == 0) {
                throw new EOFException();
            }
        }
        return this.bb.getInt();
    }
}
