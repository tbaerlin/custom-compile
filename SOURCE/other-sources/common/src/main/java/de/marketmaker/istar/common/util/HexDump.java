/*
 * HexDump.java
 *
 * Created on 25.02.2005 09:57:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.*;

/**
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class HexDump {
    private static final String LS = System.getProperty("line.separator");

    private static final char HEX[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static final int DEFAULT_BYTES_PER_LINE = 16;

    static final String ZEROS = "0000000";

    private HexDump() {
    }

    public static void writeHex(PrintWriter pw, final byte[] data) {
        writeHex(pw, data, DEFAULT_BYTES_PER_LINE);
    }

    public static void writeHex(PrintWriter pw, final byte[] data, final int bytesPerLine) {
        for (int i = 0; i < data.length; i += bytesPerLine) {
            pw.println(toHex(i, data, bytesPerLine));
        }
    }

    public static void writeHex(PrintStream ps, final byte[] data) {
        writeHex(ps, data, DEFAULT_BYTES_PER_LINE);
    }

    public static void writeHex(PrintStream ps, final byte[] data, final int bytesPerLine) {
        for (int i = 0; i < data.length; i += bytesPerLine) {
            ps.println(toHex(i, data, bytesPerLine));
        }
    }

    public static String toHex(byte[] data) {
        return toHex(data, DEFAULT_BYTES_PER_LINE);
    }

    public static String toHex(byte[] data, final int bytesPerLine) {
        return toHex(data, 0, data.length / bytesPerLine + 1, bytesPerLine);
    }

    public static String toHex(byte[] data, final int offset, final int numLines, final int bytesPerLine) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0: " + offset);
        }
        final int to = Math.min(offset + (numLines * bytesPerLine), data.length);
        final StringBuilder sb = new StringBuilder(Math.min(255, numLines * bytesPerLine * 5));
        for (int i = offset; i < to; i += bytesPerLine) {
            sb.append(toHex(i, data, bytesPerLine));
            sb.append(LS);
        }
        return sb.toString();
    }

    private static String toHex(int offset, byte[] data, int bytesPerLine) {
        final StringBuilder sb = new StringBuilder(bytesPerLine * 5);
        final String o = Integer.toString(offset, 16);
        sb.append(ZEROS.substring(0, ZEROS.length() - o.length() + 1)).append(o).append(": ");

        for (int j = offset; j < offset + bytesPerLine; j++) {
            if (j < data.length) {
                byte b = data[j];
                sb.append(HEX[((b >> 4) & 0xF)]);
                sb.append(HEX[(b & 0xF)]);
                sb.append(' ');
            }
            else {
                sb.append("   ");
            }
        }
        sb.append("| ");
        for (int j = offset; j < offset + bytesPerLine; j++) {
            if (j < data.length) {
                int b = data[j] & 0xFF;
                sb.append((b < 32 || b > 127) ? '.' : (char) b);
            }
            else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        byte[] b = new byte[256];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) i;
        }
        System.out.println(toHex(b, 32));
    }
}
