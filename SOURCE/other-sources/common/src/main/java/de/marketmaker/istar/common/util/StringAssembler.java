/*
 * StringAssembler.java
 *
 * Created on 01.08.2002 07:45:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

/**
 * Assembles strings from a number of bytes. Optimized for speed and memory consumption.
 * The same object can be reused to assemble any number of strings.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: StringAssembler.java,v 1.1 2004/11/17 15:12:37 tkiesgen Exp $
 */
public final class StringAssembler {
    private static final int SIZE_INCREMENT = 256;

    private static final byte SPACE = (byte)' ';

    /** current length of the string */
    private int len = 0;

    /** max. length of the string that can be constructed with this object */
    private final int maxLength;

    /** the chars used to create the string */
    private char[] chars = new char[SIZE_INCREMENT];


    public StringAssembler(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Resets this object so it can be used to assemble a new string
     */
    public final void reset() {
        this.len = 0;
    }

    /**
     * Append another byte to the string to be assembled. The given byte is assumed to
     * represent an Iso-Latin2 character (i.e., negative byte values will be converted
     * into chars with a value > 0x7f). Leading whitespace in a string is ignored!
     * @param b byte to be appended.
     * @throws IllegalStateException if appending yet another byte would exceed
     * this object's maximum length.
     */
    public final void append(final byte b) {
        if (this.len == 0 && b == SPACE) {
            return; // ignore leading spaces
        }

        if (this.len == chars.length) {
            expand();
        }

        // if b < 0, it is a char with a code > 127, so we have to and it with 0xff
        // to get the "real" char.
        this.chars[this.len++] = (b < 0) ? (char) (b & 0xFF) : (char)b;
    }

    private void expand() {
        final int newLength = Math.max(chars.length + SIZE_INCREMENT, this.maxLength);
        if (this.chars.length == newLength) {
            throw new IllegalStateException("Cannot expand, reached max length:"
                    + this.maxLength);
        }
        final char[] tmp = new char[newLength];
        System.arraycopy(chars, 0, tmp, 0, this.len);
        this.chars = tmp;
    }

    /**
     * Returns the string assembled so far.
     * @return assembled string
     */
    public final String toString() {
        if (this.len == 0) {
            return "";
        }
        while (this.len > 0 && this.chars[--this.len] <= ' '); // trim end.
        return new String(this.chars, 0, ++this.len);
    }
}
