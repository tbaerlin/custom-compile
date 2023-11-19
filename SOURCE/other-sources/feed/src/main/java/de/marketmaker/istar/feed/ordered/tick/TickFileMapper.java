/*
 * TickFileMapper.java
 *
 * Created on 13.09.13 11:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps tick files to an array of MappedByteBuffers. The array is necessary as a single
 * tick file may be larger than the 2GB that such an object can hold. Mapping a tickfile makes
 * a lot of sense as tick bytes for a symbol are usually scattered in chunks all over the file.
 * If the intention is to work with ticks for all symbols in the file, mapping the file avoids
 * millions of seek operations that would otherwise be necessary.
 *
 * @author oflege
 */
public abstract class TickFileMapper {
    public static final boolean UNMAP_SUPPORTED;

    static {
        boolean v;
        try {
            Class.forName("sun.misc.Cleaner");
            Class.forName("java.nio.DirectByteBuffer")
                    .getMethod("cleaner");
            v = true;
        } catch (Exception e) {
            v = false;
        }
        UNMAP_SUPPORTED = v;
    }

    private static final long SINGLE_BUFFER_SIZE = Integer.MAX_VALUE;

    private static final long MULTI_BUFFER_SIZE = 1L << 30;

    private static final long MULTI_BUFFER_SIZE_MASK = MULTI_BUFFER_SIZE - 1;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final File file;

    protected final ByteOrder byteOrder;

    private MappedByteBuffer[] mbbs;

    public TickFileMapper(ByteOrder byteOrder, File file) {
        this.byteOrder = byteOrder;
        this.file = file;
    }

    /**
     * Try to unmap the buffer, this method silently fails if no support
     * for that in the JVM. On Windows, this leads to the fact,
     * that mmapped files cannot be modified or deleted.
     */
    static void unmap(final ByteBuffer buffer) throws IOException {
        if (UNMAP_SUPPORTED) {
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                    final Method getCleanerMethod = buffer.getClass()
                            .getMethod("cleaner");
                    getCleanerMethod.setAccessible(true);
                    final Object cleaner = getCleanerMethod.invoke(buffer);
                    if (cleaner != null) {
                        cleaner.getClass().getMethod("clean")
                                .invoke(cleaner);
                    }
                    return null;
                });
            } catch (PrivilegedActionException e) {
                final IOException ioe = new IOException("unable to unmap the mapped buffer");
                ioe.initCause(e.getCause());
                throw ioe;
            }
        }
    }

    protected void unmapBuffers() throws IOException {
        if (UNMAP_SUPPORTED && mbbs != null) {
            for (MappedByteBuffer mbb : mbbs) {
                unmap(mbb);
            }
        }
    }

    /**
     * Return a buffer that contains data starting from the file at position and is guaranteed
     * to contain at least length bytes for reading.
     */
    protected ByteBuffer getBuffer(long position, int length) {
        if (this.mbbs.length == 1) {
            this.mbbs[0].position((int) position);
            return this.mbbs[0];
        }

        final int mi = bufferIndex(position);
        final int mj = bufferIndex(position + length);
        this.mbbs[mi].position((int) (position & MULTI_BUFFER_SIZE_MASK));
        if (mi == mj) {
            return this.mbbs[mi];
        }

        // need to combine data from two buffers:
        final byte[] bytes = new byte[length];
        final int iLength = this.mbbs[mi].remaining();
        this.mbbs[mi].get(bytes, 0, iLength);
        this.mbbs[mj].position(0);
        this.mbbs[mj].get(bytes, iLength, length - iLength);

        return ByteBuffer.wrap(bytes).order(this.byteOrder);
    }

    private int bufferIndex(long position) {
        return (int) (position >> 30);
    }

    protected void mapFile(FileChannel ch) throws IOException {
        this.mbbs = createBuffers(ch);
        // esp. with a spinning disk it is a lot faster to fill the buffer(s) using
        // one sequential read compared to random access ticks chunk reads
        for (MappedByteBuffer mbb : this.mbbs) {
            mbb.load();
        }
        this.logger.info(this.file.getName() + " loaded " + this.mbbs.length + " buffer(s)");
    }

    private MappedByteBuffer[] createBuffers(FileChannel ch) throws IOException {
        if (ch.size() <= SINGLE_BUFFER_SIZE) {
            final MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            mbb.order(this.byteOrder);
            return new MappedByteBuffer[]{mbb};
        }

        final int numBuffs = bufferIndex(ch.size());
        final MappedByteBuffer[] result = new MappedByteBuffer[numBuffs + 1];
        long from = 0L;
        for (int i = 0; i < result.length; i++) {
            final long size = (i == numBuffs) ? (ch.size() - from) : MULTI_BUFFER_SIZE;
            result[i] = ch.map(FileChannel.MapMode.READ_ONLY, from, size);
            result[i].order(this.byteOrder);
            from += MULTI_BUFFER_SIZE;
        }
        return result;
    }

    protected int fillFileTickStoreTicks(long startAddress, byte[] dst) {
        int numSeeks = 0;

        long address = startAddress;
        int end = dst.length;

        while (address != 0) {
            final long p = FileTickStore.decodePosition(address);
            final int length = FileTickStore.decodeLength(address) - 8;
            final int start = end - length;
            numSeeks++;

            final ByteBuffer buffer = getBuffer(p, length + 8);

            address = buffer.getLong();
            buffer.get(dst, start, end - start);

            assert TickWriter.isFileAddress(address) : Long.toHexString(address);

            end = start;
        }
        return numSeeks;
    }
}
