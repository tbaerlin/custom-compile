/*
 * TickFile.java
 *
 * Created on 03.12.2004 11:20:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DataFile implements Closeable {
    private static final int MAX_FILE_READ_CHUNK_SIZE =
            Integer.getInteger("MmMaxFileReadChunkSize", 64 * 1024);
    private static final int ONE_MB = 1024 * 1024;

    private final File file;

    private RandomAccessFile raf;

    private FileChannel channel;

    public DataFile(File f, boolean readOnly) throws IOException {
        this.raf = new RandomAccessFile(f, readOnly? "r" : "rw");
        this.channel = this.raf.getChannel();
        this.file = f;
    }

    public String toString() {
        return "DataFile[" + this.file.getAbsolutePath() + "]";
    }

    public void transferTo(DataFile df, long position, long count) throws IOException {
        // =======================================================================
        // FileChannel transferTo is broken in linux kernel 2.6.32 aka. CentOS 6.2
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7052359
        // see also http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=641419

        // CANNOT BE USED:   this.channel.transferTo(position, count, df.channel);
        // =======================================================================
        // workaround using our own buffer:
        
        final long oldPosition = position();
        seek(position);
        int numToWrite = (int) count;
        final ByteBuffer bb = ByteBuffer.allocate(Math.min(ONE_MB, numToWrite));
        while (numToWrite > 0) {
            bb.clear();
            bb.limit(Math.min(ONE_MB, numToWrite));
            numToWrite -= bb.remaining();
            this.channel.read(bb);
            bb.flip();
            df.write(bb);
        }
        seek(oldPosition);
    }

    public long transferFrom(DataFile df, long position, long count) throws IOException {
        return this.channel.transferFrom(df.channel, position, count);
    }

    public FileChannel getChannel() {
        return channel;
    }

    public FileChannel truncate(long size) throws IOException {
        return this.channel.truncate(size);
    }

    public int getIndexStart() throws IOException {
        return readInt(size() - 4);
    }

    public int readInt(long position) throws IOException {
        seek(position);
        return readInt();
    }

    public long readLong() throws IOException {
        return this.raf.readLong();
    }

    public long readLong(long position) throws IOException {
        seek(position);
        return this.raf.readLong();
    }

    public int readInt() throws IOException {
        return this.raf.readInt();
    }

    public int readByte() throws IOException {
        return this.raf.read();
    }

    public void writeInt(long position, int value) throws IOException {
        seek(position);
        writeInt(value);
    }

    public void writeInt(int value) throws IOException {
        this.raf.writeInt(value);
    }

    public void writeLong(long position, long value) throws IOException {
        seek(position);
        writeLong(value);
    }

    public void writeLong(long value) throws IOException {
        this.raf.writeLong(value);
    }

    public long size() throws IOException {
        return this.channel.size();
    }

    public void seek(long position) throws IOException {
        this.channel.position(position);
    }

    public long position() throws IOException {
        return this.channel.position();
    }

    public void close() throws IOException {
        this.channel.close();
        this.channel = null;
        this.raf = null;
    }

    public void write(ByteBuffer bb) throws IOException {
        this.channel.write(bb);
    }

    public void force(boolean metaData) throws IOException {
        this.channel.force(metaData);
    }

    public void read(ByteBuffer bb, int length) throws IOException {
        if (length <= MAX_FILE_READ_CHUNK_SIZE || bb.isDirect()) {
            bb.limit(bb.position() + length);
            this.channel.read(bb);
        }
        else {
            // Java's NIO uses a direct ByteBuffer each time chanel.read(bb) is called.
            // Although those buffers are cached per thread using SoftReferences, creating
            // too many and/or too large buffers may lead to an OoME. Therefore,
            // we read that data in chunks no larger than MAX_READ_CHUNK_SIZE
            int n = length;
            while (n > 0) {
                int currLength = Math.min(MAX_FILE_READ_CHUNK_SIZE, n);
                bb.limit(bb.position() + currLength);
                this.channel.read(bb);
                n -= currLength;
            }
        }
        bb.limit(bb.capacity());
    }

    /**
     * read as many bytes as fit into bb (e.g., bb.remaining())
     * @param bb target for bytes read
     * @return number of bytes read
     * @throws IOException on error
     */
    public int read(ByteBuffer bb) throws IOException {
        final int tmp = bb.position();
        read(bb, bb.remaining());
        return bb.position() - tmp;
    }
}
