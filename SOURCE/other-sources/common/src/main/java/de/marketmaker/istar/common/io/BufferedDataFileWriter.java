/*
 * DataFileWriter.java
 *
 * Created on 10.07.12 17:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zzhao
 */
public class BufferedDataFileWriter implements Closeable, Flushable {
    private final ByteBuffer bb = ByteBuffer.allocate(1024 * 8);

    private final DataFile df;

    public BufferedDataFileWriter(DataFile df) {
        this.df = df;
    }

    public long position() throws IOException {
        flush();
        return this.df.position();
    }

    public BufferedDataFileWriter(File f) throws IOException {
        this.df = new DataFile(f, false);
    }

    public void writeLong(long data) throws IOException {
        if (this.bb.remaining() < 8) {
            bufferToFile();
        }
        this.bb.putLong(data);
    }

    public void writeInt(int data) throws IOException {
        if (this.bb.remaining() < 4) {
            bufferToFile();
        }
        this.bb.putInt(data);
    }

    public void writeInt(long pos, int data) throws IOException {
        this.df.writeInt(pos, data);
    }

    public void write(byte data) throws IOException {
        if (!this.bb.hasRemaining()) {
            bufferToFile();
        }
        this.bb.put(data);
    }

    private void bufferToFile() throws IOException {
        this.bb.flip();
        while (this.bb.hasRemaining()) {
            this.df.write(bb);
        }
        this.bb.clear();
    }


    public void close() throws IOException {
        flush();
        this.df.close();
    }

    @Override
    public void flush() throws IOException {
        bufferToFile();
        this.df.force(false);
    }
}
