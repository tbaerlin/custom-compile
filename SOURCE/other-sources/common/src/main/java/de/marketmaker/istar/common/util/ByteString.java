/*
 * ByteString.java
 *
 * Created on 02.10.2002 12:04:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import net.jcip.annotations.Immutable;

/**
 * A string based on a byte array instead of a char array. Another difference to String
 * is that the underlying byte array will never be shared by two different instances.
 * Although this means that substring operations take a bit longer and the result
 * needs its own byte array, it avoids having to maintain an offset and a
 * length count for each instance.<p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public final class ByteString implements Comparable<ByteString>, Serializable {
    static final long serialVersionUID = 261739354125458561L;

    public static final int LENGTH_ENCODING_NONE = 0;
    public static final int LENGTH_ENCODING_BYTE = 1;
    public static final int LENGTH_ENCODING_INT = 2;

    public static final ByteString EMPTY = new ByteString("");

    public static final ByteString NULL = new ByteString(new byte[] { 0 }, 0, 1);

    /**
     * The bytes that make up this string. This variable should _never_ be exposed by
     * any method since that would allow to modify the contents of the string.
     */
    private final byte[] value;

    /** the cached hash value for this string */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("JCIP")
    private transient int hash = 0;

    /**
     * Creates a new object.
     * @param value source for the bytes used to create the new object
     * @param offset index of the first byte from value used to create the string
     * @param count number of bytes from value used
     * @throws StringIndexOutOfBoundsException if offset and count do not define a valid
     * range of bytes in value.
     */
    public ByteString(byte[] value, int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset > value.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }

        this.value = new byte[count];
        System.arraycopy(value, offset, this.value, 0, count);
    }

    /**
     * Creates new object from a String.
     * @param s source
     * @throws IllegalArgumentException if s contains any char with a value larger than
     * 255.
     */
    public ByteString(String s) {
        this.value = new byte[s.length()];
        for (int i = 0; i < value.length; i++) {
            final char c = s.charAt(i);
            if (c > 255) {
                throw new IllegalArgumentException(c + " at pos. " + i + " > 255");
            }
            value[i] = (byte)c;
        }
    }

    /**
     * Private constructor, should only be used with a newly created array as argument.
     * @param value bytes representing this string.
     */
    private ByteString(byte[] value) {
        this.value = value;
    }

    /**
     * Returns a copy of the byte array that stores this string's characters.
     * @return a copy of this object's bytes.
     */
    public byte[] getBytes() {
        return Arrays.copyOf(this.value, this.value.length);
    }

    /**
     * Returns this string's length, that is the number of bytes it contains.
     * @return string's length
     */
    public int length() {
        return this.value.length;
    }

    /**
     * Returns whether or not this object equals the given one.
     * @param anObject to be compared to this object
     * @return true iff this object equals the given one.
     */
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (!(anObject instanceof ByteString)) {
            return false;
        }
        ByteString anotherString = (ByteString)anObject;
        int n = this.value.length;
        if (n != anotherString.value.length) {
            return false;
        }
        while (--n >= 0) {
            if (this.value[n] != anotherString.value[n])
                return false;
        }
        return true;
    }

    public boolean equals(byte[] bytes, int offset, int length) {
        if (this.length() != length) {
            return false;
        }
        for (int i = this.length(); i != 0; ) {
            i--;
            if (this.value[i] != bytes[i + offset]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            h = hashCode(this.value, 0, this.value.length);
            this.hash = h;
        }
        return h;
    }

    static int hashCode(byte[] bytes, int offset, int length) {
        int h = 0;
        for (int i = offset, n = offset + length; i < n; i++) {
            h = 31 * h + bytes[i];
        }
        return h;
    }

    /**
     * @param bs compared with this object
     * @return the position of the first byte that differs between this object and bs or -1 if
     * both objects are equal.
     */
    public int indexOfDifference(ByteString bs) {
        final int min = Math.min(length(), bs.length());
        for (int i = 0; i < min; i++) {
            if (this.value[i] != bs.value[i]) {
                return i;
            }
        }
        return (length() == bs.length()) ? -1 : min;
    }

    public int compareTo(ByteString bs) {
        final int cmp = compareTo(bs, this.length());
        if (cmp != 0) {
            return cmp;
        }
        return this.length() - bs.length();
    }

    /**
     * Compares this to bs, but evaluates at most len bytes.
     * @param bs to compare
     * @param len max number of bytes from start to compare
     * @return negative if this is smaller, 0 if equal, positive if this is larger
     */
    public int compareTo(ByteString bs, int len) {
        if (this == bs) {
            return 0;
        }
        int i = 0;
        final int n = Math.min(len, Math.min(this.length(), bs.length()));
        while (i < n) {
            if (this.value[i] == bs.value[i]) {
                i++;
            }
            else {
                return this.value[i] - bs.value[i];
            }
        }
        return 0;
    }

    /**
     * Converts this ByteString into a "real" String.
     * @return String object.
     */
    public String toString() {
        if (length() == 0) {
            return "EMPTY";
        }
        if (length() == 1 && this.value[0] == 0) {
            return "NULL";
        }
        // return new String(this.value) is _too_ expensive due to character decoding.

        final StringBuilder sb = new StringBuilder(this.value.length);
        for (int i = 0, n = this.value.length; i < n; i++) {
            sb.append(charAt(i));
        }
        return sb.toString();
    }

    public char charAt(int i) {
        return (char)(this.value[i] & 0xFF);
    }

    /**
     * Returns the byte at the given position in this string.
     * @param pos prosition of the byte to return, first byte is at position 0
     * @return byte at given position
     * @throws ArrayIndexOutOfBoundsException if pos is invalid.
     */
    public byte byteAt(int pos) {
        return this.value[pos];
    }

    /**
     * Returns whether the byte at the given position equals the ASCII code of
     * a number [0..9].
     * @param pos position to test.
     * @return true if byte at position pos represents a digit, false otherwise
     * @throws ArrayIndexOutOfBoundsException if pos is invalid.
     */
    public boolean isDigit(int pos) {
        return this.value[pos] >= (byte)'0' && this.value[pos] <= (byte)'9';
    }

    /**
     * Returns true if this string contains the given string at the given offset.
     * @param prefix string to look for
     * @param offset position in this string where to look for prefix
     * @return true if this string contains the given string at the given offset.
     */
    public boolean startsWith(ByteString prefix, int offset) {
        int to = offset;
        int po = 0;
        int pc = prefix.value.length;
        if ((offset < 0) || (offset > this.value.length - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (value[to++] != prefix.value[po++]) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWith(ByteString prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(ByteString suffix) {
        return startsWith(suffix, this.value.length - suffix.length());
    }

    /**
     * Returns the first index of the given char in this string
     * @param ch char to search.
     * @return position of ch in this string or -1 if it does not occur.
     */
    public int indexOf(char ch) {
        return indexOf(ch, 0);
    }

    /**
     * Returns the first index of the given char in this string after the given position.
     * @param ch char to search.
     * @param fromIndex start position in this string for searching.
     * @return position of ch in this string or -1 if it does not occur.
     */
    public int indexOf(char ch, int fromIndex) {
        if (fromIndex < 0) {
            return indexOf(ch, 0);
        }

        final int max = this.value.length;
        if (fromIndex >= max || ch > 255) {
            return -1;
        }

        final byte b = (byte)ch;
        for (int i = fromIndex; i < max; i++) {
            if (this.value[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(char ch) {
        final byte b = (byte)ch;
        for (int i = this.value.length; i-- > 0; ) {
            if (this.value[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(ByteString bs) {
        return indexOf(bs, 0);
    }

    public int indexOf(ByteString bs, int fromIndex) {
        if (bs == null) {
            return -1;
        }
        if (bs.length() == 0) {
            return fromIndex;
        }

        final int max = this.value.length - bs.value.length + 1;

        if (max < fromIndex) {
            return -1;
        }

        int n = fromIndex;
        NEXT: while (n < max) {
            if (this.value[n] != bs.value[0]) {
                n++;
                continue;
            }

            for (int i = 1; i < bs.value.length; i++) {
                if (this.value[n + i] != bs.value[i]) {
                    n++;
                    continue NEXT;
                }
            }
            return n;
        }

        return -1;
    }

    public ByteString prepend(ByteString bs) {
        return concat(bs.value, this.value);        
    }

    public ByteString prepend(byte[] bytes) {
        return concat(bytes, this.value);
    }

    public ByteString append(ByteString bs) {
        return concat(this.value, bs.value);
    }

    public ByteString append(byte[] bytes) {
        return concat(this.value, bytes);
    }

    private static ByteString concat(byte[] first, byte[] second) {
        final byte[] tmp = new byte[first.length + second.length];
        System.arraycopy(first, 0, tmp, 0, first.length);
        System.arraycopy(second, 0, tmp, first.length, second.length);
        return new ByteString(tmp);
    }

    /**
     * Returns a new ByteString that is a substring of this string starting at the
     * given position and extending to its end.
     * @param beginIndex first position of where the substring should begin.
     * @return new substring.
     */
    public ByteString substring(int beginIndex) {
        return substring(beginIndex, this.value.length);
    }

    /**
     * Returns a new ByteString that is a substring starting at the given position and
     * ending before the given end position.
     * @param beginIndex index of first byte to be included in the substring.
     * @param endIndex index of the first byte not to be included.
     * @return new substring.
     */
    public ByteString substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > this.value.length) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
        }
        if ((beginIndex == 0) && (endIndex == this.value.length)) {
            return this;
        }

        final byte v[] = new byte[endIndex - beginIndex];
        System.arraycopy(this.value, beginIndex, v, 0, endIndex - beginIndex);
        return new ByteString(v);
    }

    public ByteString replace(int start, int end, ByteString str) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (start > this.value.length) {
            throw new StringIndexOutOfBoundsException("start > length()");
        }
        if (start > end) {
            throw new StringIndexOutOfBoundsException("start > end");
        }

        if (end > this.value.length) {
            end = this.value.length;
        }
        int len = str.length();
        byte[] bytes = new byte[this.value.length + len - (end - start)];

        if (start > 0) {
            System.arraycopy(value, 0, bytes, 0, start);
        }
        if (str.length() > 0) {
            System.arraycopy(str.value, 0, bytes, start, len);
        }
        if (end < this.value.length) {
            System.arraycopy(value, end, bytes, start + len, this.value.length - end);
        }
        return new ByteString(bytes);
    }

    /**
     * Writes the contents of this string to the given ByteBuffer.
     * @param bb target for writing.
     * @param lengthEncoding how to encode the length of this string.
     */
    public void writeTo(ByteBuffer bb, int lengthEncoding) {
        switch (lengthEncoding) {
            case LENGTH_ENCODING_BYTE:
                bb.put((byte)this.value.length);
                break;
            case LENGTH_ENCODING_INT:
                bb.putInt(this.value.length);
                break;
            default:
                // empty
        }
        bb.put(this.value);
    }

    /**
     * Creates a new ByteString that is read from the given ByteBuffer, the first byte read
     * is supposed to contain an unsigned byte that represents the length of the string to be read.
     * @param bb source for reading the new string.
     * @return the new string.
     */
    public static ByteString readFrom(ByteBuffer bb) {
        return readFrom(bb, LENGTH_ENCODING_BYTE);
    }

    /**
     * Creates a new ByteString that is read from the given ByteBuffer.
     * @param bb source for reading the new string.
     * @return the new string.
     */
    public static ByteString readFrom(ByteBuffer bb, int lengthEncoding) {
        switch (lengthEncoding) {
            case LENGTH_ENCODING_BYTE:
                return readWithLengthFrom(bb, bb.get() & 0xFF);
            case LENGTH_ENCODING_INT:
                return readWithLengthFrom(bb, bb.getInt());
            default:
                throw new IllegalArgumentException("no length encoded");
        }
    }

    /**
     * Creates a new ByteString that is read from the given ByteBuffer.
     * @param bb source for reading
     * @param length length of string to be read.
     * @return the new string.
     */
    public static ByteString readWithLengthFrom(ByteBuffer bb, int length) {
        final byte[] v = new byte[length];
        bb.get(v);
        return new ByteString(v);
    }


}
