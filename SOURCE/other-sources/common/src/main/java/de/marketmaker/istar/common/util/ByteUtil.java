/*
 * ByteUtil.java
 *
 * Created on 13.06.2005 11:40:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import de.marketmaker.istar.common.io.ByteBufferInputStream;
import de.marketmaker.istar.common.io.ByteBufferOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.codec.binary.Hex;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ByteUtil {
    public static String toString(byte[] b) {
        if (b == null) {
            return null;
        }

        return toString(b, 0, b.length);
    }

    /**
     * Returns Base64-encoded version of b as a string
     * @param b to be encoded
     * @return encoded value as String
     */
    public static String toBase64String(byte[] b) {
        if (b == null) {
            return null;
        }

        return toString(Base64.encodeBase64(b, false));
    }
    /**
     * Returns Hex-encoded version of b as a string
     * @param b to be encoded
     * @return encoded value as String
     */
    public static String toHexString(byte[] b) {
        if (b == null) {
            return null;
        }

        return new String(Hex.encodeHex(b));
    }

    public static String toString(byte[] b, int start, int length) {
        if (b == null) {
            return null;
        }

        // this is a couple of times faster than using String's decoder for byte-to-char conversion
        // even though an additional char array is created
        final char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = (char) (b[start + i] & 0xFF);
        }
        return new String(str);
    }

    public static String toStringStopAt0(byte[] b, int start, int length) {
        if (length == 0) {
            return "";
        }
        int end = start;
        final int maxEnd = start + length;
        while (end < maxEnd && b[end] != 0) {
            end++;
        }

        return toString(b, start, end - start);
    }

    public static byte[] toBytes(String s) {
        final byte[] result = new byte[s.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (s.charAt(i) & 0xFF);
        }
        return result;
    }

    public static String toBase64(ByteBuffer bb, boolean compress) throws IOException {
        final ByteBuffer in = compress ? compress(bb) : bb;
        final ByteBufferOutputStream out = new ByteBufferOutputStream(Math.max(16, in.remaining() / 6 * 9));
        final Base64OutputStream b64 = new Base64OutputStream(out);
        b64.write(in.array(), in.position(), in.remaining());
        b64.close();
        final ByteBuffer encoded = out.toBuffer();
        return new String(encoded.array(), encoded.position(), encoded.remaining());
    }

    public static ByteBuffer fromBase64(String string, boolean compressed) throws IOException {
        final ByteBuffer encoded = ByteBuffer.wrap(string.getBytes());
        final Base64InputStream b64 = new Base64InputStream(new ByteBufferInputStream(encoded));
        byte[] mightBeCompressed = readFully(b64);
        byte[] decompressed;
        if (compressed) {
            final GZIPInputStream gzis = new GZIPInputStream(new ByteBufferInputStream(ByteBuffer.wrap(mightBeCompressed)), 1024);
            decompressed = readFully(gzis);
        } else {
            decompressed = mightBeCompressed;
        }
        return ByteBuffer.wrap(decompressed);
    }

    public static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    // using gzip
    public static ByteBuffer compress(ByteBuffer bb) throws IOException {
        final ByteBufferOutputStream bbos = new ByteBufferOutputStream(bb.remaining() / 4);
        final GZIPOutputStream gzos = new GZIPOutputStream(bbos, 1024);        
        gzos.write(bb.array(), bb.position(), bb.remaining());
        gzos.close();
        return bbos.toBuffer();
    }

    // not using deflater
    public static byte[] compress(byte[] input) throws IOException {
        final Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        compressor.setInput(input);
        compressor.finish();

        final ByteArrayOutputStream baos =
                new ByteArrayOutputStream(Math.max(64, input.length / 8));

        final byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            final int count = compressor.deflate(buf);
            baos.write(buf, 0, count);
        }

        // free memory allocated on the c-heap by native methods; if this does not happen
        // and GC does not take place, you end up with an OutOfMemoryException
        compressor.end();

        return baos.toByteArray();
    }

    // not using inflater
    public static byte[] decompress(byte[] compressedData) throws IOException {
        final Inflater decompressor = new Inflater();
        decompressor.setInput(compressedData);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(compressedData.length * 8);

        final byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                final int count = decompressor.inflate(buf);
                baos.write(buf, 0, count);
            }
            catch (DataFormatException e) {
                throw new IOException("DataFormatException ", e);
            }
        }

        // free memory allocated on the c-heap by native methods; if this does not happen
        // and GC does not take place, you end up with an OutOfMemoryException
        decompressor.end();

        return baos.toByteArray();
    }

    /**
     * @return last index of <code>valueToFind</code> in <code>src</code> which is not smaller than
     * <code>start</code>; -1 if valueToFind is not found.
     */
    public static int lastIndexOf(byte[] src, byte[] valueToFind, int start) {
        return lastIndexOf(src, valueToFind, start, src.length);
    }

    /**
     * @return last index of <code>valueToFind</code> in <code>src</code> which is not smaller than
     * <code>start</code> and not larger than <code>to</code>; -1 if valueToFind is not found
     * within the given range.
     */
    public static int lastIndexOf(byte[] src, byte[] valueToFind, int start, int to) {
        if (to - start < valueToFind.length) {
            return -1;
        }
        int i = to - valueToFind.length;
        while (i >= start) {
            if (startsWithAt(src, valueToFind, i)) {
                return i;
            }
            i--;
        }
        return -1;
    }

    private static boolean startsWithAt(byte[] src, byte[] valueToFind, int p) {
        for (int j = 0; j < valueToFind.length; j++) {
            if (src[p + j] != valueToFind[j]) {
                return false;
            }
        }
        return true;
    }
}
