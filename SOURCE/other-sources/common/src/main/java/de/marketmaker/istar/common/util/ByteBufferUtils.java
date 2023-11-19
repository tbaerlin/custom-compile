/*
 * ByteBufferUtils.java
 *
 * Created on 24.11.2004 11:22:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Static helper methods for dealing with {@link java.nio.ByteBuffer} objects.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ByteBufferUtils {
    private ByteBufferUtils() {
    }


    /**
     * Similar to {@link java.nio.ByteBuffer#duplicate()} but also preserves bb's order
     * @param bb to be duplicated
     * @return duplicate of bb with same byte order
     */
    public static ByteBuffer duplicate(ByteBuffer bb) {
        return bb.duplicate().order(bb.order());
    }

    /**
     * @return byte array with a copy of bb's remaining bytes.
     */
    public static byte[] toByteArray(ByteBuffer bb) {
        final byte[] result = new byte[bb.remaining()];
        if (bb.hasArray()) {
            System.arraycopy(bb.array(), bb.position(), result, 0, result.length);
        }
        else {
            bb.get(result);
        }
        return result;
    }

    /**
     * Adds as many data from src to target as is possible, but not more than maxLength;
     * will not add a single byte if either src or target has no remaining bytes.
     */
    public static void put(ByteBuffer target, ByteBuffer src, int maxLength) {
        if (!target.hasRemaining()) {
            return;
        }

        final int n = Math.min(Math.min(target.remaining(), src.remaining()), maxLength);

        if (n == 0) {
            return;
        }
        if (n == src.remaining()) {
            target.put(src);
        }
        else {
            final int tmp = src.limit();
            src.limit(src.position() + n);
            target.put(src);
            src.limit(tmp);
        }
    }

    public static String getStringShortEncodedLength(ByteBuffer bb) {
        final short length = bb.getShort();

        if(length== Short.MIN_VALUE) {
            return null;
        }

        return getString(bb, length);
    }

    public static String getString(ByteBuffer bb, int length) {
        final byte[] b = new byte[length];
        bb.get(b);

        final char[] ch = new char[length];

        for (int i = 0; i < b.length; i++) {
            if (b[i] == 0) {
                return (i == 0) ? "" : new String(ch, 0, i);
            }
            ch[i] = (char) (b[i] & 0xFF);
        }

        return new String(ch);
    }

    public static void putStringShortEncodedLength(ByteBuffer bb, String str) {
        if(str==null) {
            bb.putShort(Short.MIN_VALUE);
            return;
        }

        bb.putShort((short) str.length());
        for(int i=0;i<str.length();i++) {
            bb.put((byte) str.charAt(i));
        }
    }

    public static void putString(ByteBuffer bb, byte[] data, int offset, int length, int maxLength) {
        bb.put(data, offset, Math.min(length, maxLength));

        if (length < maxLength) {
            bb.put((byte) 0);
        }
    }

    /** @deprecated */
    public static boolean equals(ByteBuffer bb, int length, String str) {
        if (str == null) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            final int c = bb.get() & 0xff;

            if (c == 0) {
                return i == str.length();
            }

            if (i == str.length()) {
                return false;
            }

            if (c != str.charAt(i)) {
                return false;
            }
        }

        return length == str.length();
    }

    /** @deprecated */
    public static int read(ReadableByteChannel channel, ByteBuffer bb, int desiredLength) throws IOException {
        int bytesRead = 0;
        while (bytesRead < desiredLength) {
            final int tmpBytesRead = channel.read(bb);
            if (tmpBytesRead < 0) {
                break;
            }
            bytesRead += tmpBytesRead;
        }

        return bytesRead;
    }
}