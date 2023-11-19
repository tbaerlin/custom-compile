/*
* ThrottledInputStream.java
*
* Created on 24.08.2007 11:17:47
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.common.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * An InputStream that limits the number of bytes that can be read per second.
 * @author Martin Wilke
 */

public class ThrottledInputStream extends FilterInputStream {

    private final long maxBps;
    private long bytesPerSecond;
    private long secondStart;

    private static final long SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Creates new ThrottledInputStream
     * @param in source for reading bytes
     * @param maxBps maximum number of bytes to read per second. The byte array used to
     * read from the stream should not contain more bytes than maxBps.
     */
    public ThrottledInputStream(InputStream in, long maxBps) {
        super(in);
        this.maxBps = maxBps;
        this.bytesPerSecond = 0;
        this.secondStart = System.currentTimeMillis();
    }

    private final byte[] oneByte = new byte[1];

    public int read() throws IOException {
        final int n = this.in.read(this.oneByte, 0, 1);
        return (n == 1) ? this.oneByte[0] : n;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }


    public int read(byte b[], int off, int len) throws IOException {
        final int numRead = in.read(b, off, len);

        this.bytesPerSecond += numRead;
        if (this.bytesPerSecond < this.maxBps) {
            return numRead;
        }

        final long elapsed = System.currentTimeMillis() - this.secondStart;
        if (elapsed < SECOND_IN_MILLIS) {
            try {
                Thread.sleep(SECOND_IN_MILLIS - elapsed);
            } catch (InterruptedException e) {
                // restore interruption status
                Thread.currentThread().interrupt();
            }
        }

        this.secondStart = System.currentTimeMillis();
        this.bytesPerSecond = 0;

        return numRead;
    }
}
