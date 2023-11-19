/*
 * TickFile.java
 *
 * Created on 22.11.12 07:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oflege
 */
public class TickFile implements WritableByteChannel {

    public static final Pattern TICK_FILE_NAME = Pattern.compile("([^-]+)-(20[0-9]{6}).[dt]d[3z]");

    public static String getMarket(File f) {
        final Matcher m = TICK_FILE_NAME.matcher(f.getName());
        if (m.matches()) {
            return m.group(1);
        }
        throw new IllegalArgumentException(f.getName());
    }

    public static int getDay(File f) {
        final Matcher m = TICK_FILE_NAME.matcher(f.getName());
        if (m.matches()) {
            return Integer.parseInt(m.group(2));
        }
        throw new IllegalArgumentException(f.getName());
    }

    private final File td3;

    private final File tmp;

    private FileChannel channel;

    private long indexPosition;

    // once the file has been closed, we are not going to open it again.
    private boolean closed = false;

    // we use channel for both writing and reading and need to synchronize that
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    TickFile(File f) {
        this(f, 0);
    }

    TickFile(File f, long indexPosition) {
        if (f.exists() == (indexPosition == 0)) {
            throw new IllegalStateException();
        }
        this.td3 = f;
        this.tmp = new File(f.getParentFile(),
                f.getName().substring(0, f.getName().length() - 3) + "tmp");
        this.indexPosition = indexPosition;
    }

    private FileChannel getChannel() throws IOException {
        assert !this.closed : "closed!";

        if (this.channel != null) {
            return this.channel;
        }

        this.rwl.writeLock().lock();
        try {
            if (this.channel == null) {
                if (this.td3.exists()) {
                    mv(this.td3, this.tmp);
                }
                this.channel = new RandomAccessFile(this.tmp, "rw").getChannel();
                if (this.indexPosition > 0) {
                    this.channel.position(this.indexPosition);
                    this.channel.truncate(this.indexPosition);
                    this.indexPosition = 0;
                }
            }
            return this.channel;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    private void mv(final File from, final File to) throws IOException {
        if (to.exists() && !to.delete()) {
            throw new IOException("rm " + to.getName() + " failed");
        }
        if (!from.renameTo(to)) {
            throw new IOException("mv " + from.getName() + " " + to.getName() + " failed");
        }
    }

    long position() throws IOException {
        this.rwl.readLock().lock();
        try {
            return (this.channel != null) ? this.channel.position() : this.indexPosition;
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        final int result = src.remaining();
        this.rwl.writeLock().lock();
        try {
            final WritableByteChannel ch = getChannel();
            while (src.hasRemaining()) {
                ch.write(src);
            }
        } finally {
            this.rwl.writeLock().unlock();
        }
        return result;
    }

    @Override
    public boolean isOpen() {
        this.rwl.readLock().lock();
        try {
            return this.channel != null && this.channel.isOpen();
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    @Override
    public void close() throws IOException {
        this.rwl.writeLock().lock();
        try {
            if (isOpen()) {
                this.channel.close();
                this.channel = null;
                if (this.indexPosition > 0) {
                    mv(this.tmp, this.td3);
                }
                this.closed = true;
            }
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    void setIndexPosition(long indexPosition) {
        this.indexPosition = indexPosition;
    }

    long getIndexPosition() {
        return this.indexPosition;
    }

    /**
     * Adds data from this file to dst
     * @param startAddress address of last tick chunk in this file to be added
     * @param dst tick data destination
     * @return true iff data could be read, false iff file was already closed
     * @throws IOException
     */
    boolean fillData(long startAddress, ByteBuffer dst) throws IOException {
        FileChannel fc = null;
        long writePosition = 0L;
        this.rwl.writeLock().lock();
        try {
            if (this.closed) {
                return false;
            }
            try {
                fc = getChannel();
                writePosition = fc.position();
                FileTickStore.fillData(fc, startAddress, dst);
            } finally {
                if (fc != null) {
                    fc.position(writePosition);
                }
            }
            return true;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }
}
