/*
 * BCD.java
 *
 * Created on 06.12.12 16:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author zzhao
 */
public final class BCD {

    public static enum Boundary {None, Low, High}

    private static final char PADDING_NONE = 'E';

    private static final char PADDING_P = ';';

    private BCD() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static byte[] encode(String num) {
        final int len = num.length();
        final boolean even = (len % 2) == 0;
        final int idx = num.lastIndexOf('.');
        if (idx < 0) {
            // no fraction
            return even ? toBCD(num, PADDING_NONE) : toBCD(num, PADDING_P);
        }
        else {
            int idxNonZero = len - 1;
            while (num.charAt(idxNonZero) == '0') {
                idxNonZero--;
            }

            final int eLen = idxNonZero + 1;
            final boolean eEven = (eLen % 2) == 0;
            return eEven ?
                    (num.charAt(idxNonZero) == '.'
                            ? toBCD(num, 0, eLen - 1, PADDING_P) : toBCD(num, 0, eLen)) :
                    (num.charAt(idxNonZero) == '.'
                            ? toBCD(num, 0, idxNonZero) : toBCD(num, 0, eLen, PADDING_P));
        }
    }

    private static final byte[] EMPTY_BA = new byte[0];

    private static byte[] toBCD(String num, int from, int to, char padding) {
        final int len = (to - from + (PADDING_NONE == padding ? 2 : 1)) / 2;
        final byte[] bytes = Arrays.copyOf(EMPTY_BA, len);
        for (int i = from; i < to; i++) {
            final byte b = toBCDByte(num.charAt(i));
            if (b != 0) {
                final int idx = (i - from) / 2;
                final boolean even = ((i - from) % 2) == 0;
                if (even) {
                    bytes[idx] |= (b << 4);
                }
                else {
                    bytes[idx] |= b;
                }
            }
        }

        if (PADDING_NONE == padding) {
            bytes[len - 1] |= (BOUNDARY_BYTE << 4);
        }
        else {
            bytes[len - 1] |= toBCDByte(padding);
        }
        return bytes;
    }

    private static byte[] toBCD(String num, int fromIdx, int toIdx) {
        return toBCD(num, fromIdx, toIdx, PADDING_NONE);
    }

    private static byte[] toBCD(String num, char padding) {
        return toBCD(num, 0, num.length(), padding);
    }

    public static byte toBCDByte(char ch) {
        switch (ch) {
            case '0':
                return (byte) 0;
            case '1':
                return (byte) 1;
            case '2':
                return (byte) 2;
            case '3':
                return (byte) 3;
            case '4':
                return (byte) 4;
            case '5':
                return (byte) 5;
            case '6':
                return (byte) 6;
            case '7':
                return (byte) 7;
            case '8':
                return (byte) 8;
            case '9':
                return (byte) 9;
            case '.':
                return (byte) 10;
            case '-':
                return (byte) 11;
            case ';':
                return BOUNDARY_BYTE;
            default:
                throw new UnsupportedOperationException("no support for: " + ch);
        }
    }

    public static final byte BOUNDARY_BYTE = 0x0C;

    public static final byte BOUNDARY_BYTE_W = (byte) (0x0C << 4);

    public static void putBoundary(ByteBuffer bb) {
        bb.put(BOUNDARY_BYTE_W);
    }

    public static Boundary checkBoundary(byte b) {
        if ((b & 0x0F) == BOUNDARY_BYTE) {
            return Boundary.Low;
        }
        else if (((b >> 4) & 0x0F) == BOUNDARY_BYTE) {
            return Boundary.High;
        }
        return Boundary.None;
    }

    public static boolean isBoundary(byte b) {
        return BCD.Boundary.None != checkBoundary(b);
    }

    public static boolean isBoundary(ByteBuffer bb) {
        return BCD.Boundary.None != checkBoundary(bb.get(bb.position() - 1));
    }

    public static String parsePrice(ByteBuffer bb) {
        final int pos = bb.position();
        byte b = bb.get();
        while (!isBoundary(b)) {
            b = bb.get();
        }

        return toBCDString(bb, pos, bb.position());
    }

    private static final ThreadLocal<StringBuilder> sbHolder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(32);
        }
    };

    public static BigDecimal decode(byte[] bytes) {
        return new BigDecimal(toBCDString(bytes));
    }

    public static String toBCDString(byte[] bytes) {
        return toBCDString(bytes, 0, bytes.length);
    }

    public static String toBCDString(ByteBuffer bb, int from, int to) {
        final StringBuilder sb = sbHolder.get();
        sb.setLength(0);
        for (int i = from; i < to; i++) {
            final byte b = bb.get(i);
            final Boundary boundary = checkBoundary(b);
            if (Boundary.None == boundary) {
                sb.append(toBCDChar((byte) ((b >> 4) & 0x0F)));
                sb.append(toBCDChar((byte) (b & 0x0F)));
            }
            else if (boundary == Boundary.Low) {
                sb.append(toBCDChar((byte) ((b >> 4) & 0x0F)));
            }
        }
        return sb.toString();
    }

    public static String toBCDString(byte[] bytes, int from, int to) {
        final StringBuilder sb = sbHolder.get();
        sb.setLength(0);
        for (int i = from; i < to; i++) {
            final byte b = bytes[i];
            final Boundary boundary = checkBoundary(b);
            if (Boundary.None == boundary) {
                sb.append(toBCDChar((byte) ((b >> 4) & 0x0F)));
                sb.append(toBCDChar((byte) (b & 0x0F)));
            }
            else if (boundary == Boundary.Low) {
                sb.append(toBCDChar((byte) ((b >> 4) & 0x0F)));
            }
        }
        return sb.toString();
    }

    public static char toBCDChar(byte b) {
        switch (b) {
            case 0:
                return (byte) '0';
            case 1:
                return (byte) '1';
            case 2:
                return (byte) '2';
            case 3:
                return (byte) '3';
            case 4:
                return (byte) '4';
            case 5:
                return (byte) '5';
            case 6:
                return (byte) '6';
            case 7:
                return (byte) '7';
            case 8:
                return (byte) '8';
            case 9:
                return (byte) '9';
            case 10:
                return (byte) '.';
            case 11:
                return (byte) '-';
            default:
                throw new UnsupportedOperationException("no support for: " + b);
        }
    }

    public static void transferPrices(ByteBuffer tar, ByteBuffer src, int count) {
        int seen = 0;
        while (seen < count) {
            final byte b = src.get();
            tar.put(b);
            if (isBoundary(b)) {
                seen++;
            }
        }
    }

    public static void bypassPrices(ByteBuffer buf, int dayCount) {
        int seen = 0;
        while (seen < dayCount) {
            final byte b = buf.get();
            if (BCD.isBoundary(b)) {
                seen++;
            }
        }
    }

    public static void replacePriceEnd(ByteBuffer tar) {
        if (checkBoundary(EodUtil.lastByte(tar)) == BCD.Boundary.High) {
            tar.position(tar.position() - 1);
            putBoundary(tar);
        }
    }
}
