/*
 * @(#)BitSet.java	1.60 04/02/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package de.marketmaker.istar.common.util;

import java.util.Arrays;

import java.io.Serializable;

/**
 * A very simple BitSet with a fixed size. A major difference compared to java.util.BitSet
 * is that it is possible to get access to the underlying storage, which allows for efficient
 * custom serialization.
 *
 * Trying to access a bit with a higher index than size might or might not succeed,
 * depending on whether the storage allocated for the bits is big enough. In general,
 * accessing those bits should be avoided.
 *
 * Some of this class's methods have been copied from java.util.BitSet.
 *
 * A <code>SimpleBitSet</code> is not safe for multithreaded use without
 * external synchronization.
 * @author Oliver Flege
 */
public class SimpleBitSet implements Serializable {
    private static final long serialVersionUID = 7997698588900078753L;

    /*
     * BitSets are packed into arrays of "units."  Currently a unit is a long,
     * which consists of 64 bits, requiring 6 address bits.  The choice of unit
     * is determined purely by performance concerns.
     */
    private final static int ADDRESS_BITS_PER_UNIT = 6;

    private final static int BITS_PER_UNIT = 1 << ADDRESS_BITS_PER_UNIT;

    private final static int BIT_INDEX_MASK = BITS_PER_UNIT - 1;

    private static final long ALL_SET = ~0L;

    /**
     * Storage for bits in this BitSet.  The ith bit is stored in bits[i/64] at
     * bit position i % 64 (where bit position 0 refers to the least
     * significant bit and 63 refers to the most significant bit).
     */
    private long unit[];


    /**
     * Given a bit index return unit index containing it.
     */
    private static int unitIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_UNIT;
    }

    /**
     * Given a bit index, return a unit that masks that bit in its unit.
     */
    private static long bit(int bitIndex) {
        return 1L << (bitIndex & BIT_INDEX_MASK);
    }

    /**
     * Returns a long that has all bits that are less significant
     * than the specified index set to 1. All other bits are 0.
     */
    private static long bitsRightOf(int x) {
        return (x == 0 ? 0 : ALL_SET >>> (64 - x));
    }

    /**
     * Returns a long that has all the bits that are more significant
     * than or equal to the specified index set to 1. All other bits are 0.
     */
    private static long bitsLeftOf(int x) {
        return ALL_SET << x;
    }


    /**
     * Creates a bit set whose initial size is large enough to explicitly
     * represent bits with indices in the range <code>0</code> through
     * <code>nbits-1</code>. All bits are initially <code>false</code>.
     * @param nbits the initial size of the bit set.
     */
    public SimpleBitSet(int nbits) {
        if (nbits < 0) {
            throw new IllegalArgumentException("nbits < 0: " + nbits);
        }
        this.unit = new long[(unitIndex(nbits - 1) + 1)];
    }

    /**
     * Creates a bit set with the given bits. Bit 0 is the least significant bit in bits[0] etc.
     * @param bits
     */
    public SimpleBitSet(long[] bits) {
        this.unit = new long[bits.length];
        System.arraycopy(bits, 0, this.unit, 0, this.unit.length);
    }

    /**
     * Returns (index + 1) of highest bit set in this bit set or 0 if none is set.
     */
    public int length() {
        for (int i = this.unit.length - 1; i >= 0; i--) {
            if (this.unit[i] != 0) {
                int highPart = (int) (this.unit[i] >>> 32);
                return 64 * i +
                        (highPart == 0 ? bitLen((int) this.unit[i])
                                : 32 + bitLen((int) highPart));
            }
        }
        return 0;
    }

    /**
     * bitLen(w) is the number of bits in w.
     */
    private static int bitLen(int w) {
        // Binary search - decision tree (5 tests, rarely 6)
        return
                (w < 1 << 15 ?
                        (w < 1 << 7 ?
                                (w < 1 << 3 ?
                                        (w < 1 << 1 ? (w < 1 << 0 ? (w < 0 ? 32 : 0) : 1) : (w < 1 << 2 ? 2 : 3)) :
                                        (w < 1 << 5 ? (w < 1 << 4 ? 4 : 5) : (w < 1 << 6 ? 6 : 7))) :
                                (w < 1 << 11 ?
                                        (w < 1 << 9 ? (w < 1 << 8 ? 8 : 9) : (w < 1 << 10 ? 10 : 11)) :
                                        (w < 1 << 13 ? (w < 1 << 12 ? 12 : 13) : (w < 1 << 14 ? 14 : 15)))) :
                        (w < 1 << 23 ?
                                (w < 1 << 19 ?
                                        (w < 1 << 17 ? (w < 1 << 16 ? 16 : 17) : (w < 1 << 18 ? 18 : 19)) :
                                        (w < 1 << 21 ? (w < 1 << 20 ? 20 : 21) : (w < 1 << 22 ? 22 : 23))) :
                                (w < 1 << 27 ?
                                        (w < 1 << 25 ? (w < 1 << 24 ? 24 : 25) : (w < 1 << 26 ? 26 : 27)) :
                                        (w < 1 << 29 ? (w < 1 << 28 ? 28 : 29) : (w < 1 << 30 ? 30 : 31)))));
    }

    /**
     * Returns a subset of this bit set containing all bits from fromIndex (incl) to
     * toIndex (excl).
     */
    public SimpleBitSet get(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
        if (toIndex < 0) {
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex +
                    " > toIndex: " + toIndex);
        }

        // If no set bits in range return empty bitset
        int len = length();
        if (len <= fromIndex || fromIndex == toIndex) {
            return new SimpleBitSet(toIndex - fromIndex);
        }

        // An optimization
        if (len < toIndex) {
            toIndex = len;
        }

        SimpleBitSet result = new SimpleBitSet(toIndex - fromIndex);
        int startBitIndex = fromIndex & BIT_INDEX_MASK;
        int endBitIndex = toIndex & BIT_INDEX_MASK;
        int targetWords = (toIndex - fromIndex + 63) / 64;
        int sourceWords = unitIndex(toIndex) - unitIndex(fromIndex) + 1;
        int inverseIndex = 64 - startBitIndex;
        int targetIndex = 0;
        int sourceIndex = unitIndex(fromIndex);

        // Process all words but the last word
        while (targetIndex < targetWords - 1) {
            result.unit[targetIndex++] =
                (unit[sourceIndex++] >>> startBitIndex) |
                        ((inverseIndex == 64) ? 0 : unit[sourceIndex] << inverseIndex);
        }

        // Process the last word
        result.unit[targetIndex] = (sourceWords == targetWords ?
                (unit[sourceIndex] & bitsRightOf(endBitIndex)) >>> startBitIndex :
                (unit[sourceIndex++] >>> startBitIndex) | ((inverseIndex == 64) ? 0 :
                        (getBits(sourceIndex) & bitsRightOf(endBitIndex)) << inverseIndex));

        return result;
    }

    /**
     * Returns the unit of this bitset at index j as if this bitset had an
     * infinite amount of storage.
     */
    private long getBits(int j) {
        return (j < this.unit.length) ? unit[j] : 0;
    }

    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        }
        else {
            clear(bitIndex);
        }
    }

    /**
     * Sets the bit at the specified index to <code>true</code>.
     * @param bitIndex a bit index.
     */
    public void set(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        this.unit[unitIndex] |= bit(bitIndex);
    }

    public void set(int fromIndex, int toIndex) {
        int endUnitIndex = unitIndex(toIndex);
        int startUnitIndex = unitIndex(fromIndex);

        long bitMask = 0;
        if (startUnitIndex == endUnitIndex) {
            // Case 1: One word
            bitMask = (1L << (toIndex & BIT_INDEX_MASK)) -
                    (1L << (fromIndex & BIT_INDEX_MASK));
            this.unit[startUnitIndex] |= bitMask;
            return;
        }

        // Case 2: Multiple words
        // Handle first word
        bitMask = bitsLeftOf(fromIndex & BIT_INDEX_MASK);
        this.unit[startUnitIndex] |= bitMask;

        // Handle intermediate words, if any
        if (endUnitIndex - startUnitIndex > 1) {
            for (int i = startUnitIndex + 1; i < endUnitIndex; i++) {
                this.unit[i] |= ALL_SET;
            }
        }

        // Handle last word
        bitMask = bitsRightOf(toIndex & BIT_INDEX_MASK);
        this.unit[endUnitIndex] |= bitMask;
    }

    public void clear(int fromIndex, int toIndex) {
        int startUnitIndex = unitIndex(fromIndex);
        int endUnitIndex = unitIndex(toIndex);

        long bitMask = 0;
        if (startUnitIndex == endUnitIndex) {
            // Case 1: One word
            bitMask = (1L << (toIndex & BIT_INDEX_MASK)) -
                    (1L << (fromIndex & BIT_INDEX_MASK));
            this.unit[startUnitIndex] &= ~bitMask;
            return;
        }

        // Case 2: Multiple words
        // Handle first word
        bitMask = bitsLeftOf(fromIndex & BIT_INDEX_MASK);
        this.unit[startUnitIndex] &= ~bitMask;

        // Handle intermediate words, if any
        if (endUnitIndex - startUnitIndex > 1) {
            for (int i = startUnitIndex + 1; i < endUnitIndex; i++) {
                this.unit[i] = 0;
            }
        }

        // Handle last word
        if (endUnitIndex < this.unit.length) {
            bitMask = bitsRightOf(toIndex & BIT_INDEX_MASK);
            this.unit[endUnitIndex] &= ~bitMask;
        }
    }

    /**
     * Sets the bit specified by the index to <code>false</code>.
     * @param bitIndex the index of the bit to be cleared.
     */
    public void clear(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        this.unit[unitIndex] &= ~bit(bitIndex);
    }

    /**
     * Sets all of the bits in this BitSet to <code>false</code>.
     */
    public void clear() {
        for (int i = 0; i < this.unit.length; i++) {
            this.unit[i] = 0;
        }
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is <code>true</code> if the bit with the index <code>bitIndex</code>
     * is currently set in this <code>BitSet</code>; otherwise, the result
     * is <code>false</code>.
     * @param bitIndex the bit index.
     * @return the value of the bit with the specified index.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     */
    public boolean get(int bitIndex) {
        final int unitIndex = unitIndex(bitIndex);
        return ((unit[unitIndex] & bit(bitIndex)) != 0);
    }


    /**
     * Returns true if this <code>BitSet</code> contains no bits that are set
     * to <code>true</code>.
     * @return boolean indicating whether this <code>BitSet</code> is empty.
     */
    public boolean isEmpty() {
        for (int i = 0; i < this.unit.length; i++) {
            if (this.unit[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of bits set to <tt>true</tt> in this
     * <code>BitSet</code>.
     * @return the number of bits set to <tt>true</tt> in this
     *         <code>BitSet</code>.
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < this.unit.length; i++) {
            sum += bitCount(unit[i]);
        }
        return sum;
    }

    /**
     * Returns a copy of the array of longs used to store this object's bits. Can be used
     * with the constructor that takes a long array as parameter to create a copy of this
     * object.
     * @return
     */
    public long[] getUnits() {
        long[] result = new long[this.unit.length];
        System.arraycopy(this.unit, 0, result, 0, this.unit.length);
        return result;
    }

    /**
     * Returns the number of bits set in val.
     * For a derivation of this algorithm, see
     * "Algorithms and data structures with applications to
     * graphics and geometry", by Jurg Nievergelt and Klaus Hinrichs,
     * Prentice Hall, 1993.
     */
    private static int bitCount(long val) {
        val -= (val & 0xaaaaaaaaaaaaaaaaL) >>> 1;
        val = (val & 0x3333333333333333L) + ((val >>> 2) & 0x3333333333333333L);
        val = (val + (val >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
        val += val >>> 8;
        val += val >>> 16;
        return ((int) (val) + (int) (val >>> 32)) & 0xff;
    }

    /**
     * Returns a hash code value for this bit set. The has code
     * depends only on which bits have been set within this
     * <code>BitSet</code>. The algorithm used to compute it may
     * be described as follows.<p>
     * Suppose the bits in the <code>BitSet</code> were to be stored
     * in an array of <code>long</code> integers called, say,
     * <code>bits</code>, in such a manner that bit <code>k</code> is
     * set in the <code>BitSet</code> (for nonnegative values of
     * <code>k</code>) if and only if the expression
     * <pre>((k&gt;&gt;6) &lt; bits.length) && ((bits[k&gt;&gt;6] & (1L &lt;&lt; (bit & 0x3F))) != 0)</pre>
     * is true. Then the following definition of the <code>hashCode</code>
     * method would be a correct implementation of the actual algorithm:
     * <pre>
     * public int hashCode() {
     *      long h = 1234;
     *      for (int i = bits.length; --i &gt;= 0; ) {
     *           h ^= bits[i] * (i + 1);
     *      }
     *      return (int)((h &gt;&gt; 32) ^ h);
     * }</pre>
     * Note that the hash code values change if the set of bits is altered.
     * <p>Overrides the <code>hashCode</code> method of <code>Object</code>.
     * @return a hash code value for this bit set.
     */
    public int hashCode() {
        long h = 1234;
        for (int i = unit.length; --i >= 0;)
            h ^= unit[i] * (i + 1);

        return (int) ((h >> 32) ^ h);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SimpleBitSet that = (SimpleBitSet) o;
        return Arrays.equals(unit, that.unit);
    }

    /**
     * Returns the number of bits of space actually in use by this
     * <code>BitSet</code> to represent bit values.
     * The maximum element in the set is the size - 1st element.
     * @return the number of bits currently in this bit set.
     */
    public int size() {
        return unit.length << ADDRESS_BITS_PER_UNIT;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.unit.length * (BITS_PER_UNIT + 2));
        for (int i = 0; i < this.unit.length; i++) {
            for (int j = 0; j < BITS_PER_UNIT; j++) {
                if (j > 0 && (j % 8) == 0) {
                    sb.append(" ");
                }
                sb.append(get(i * BITS_PER_UNIT + j) ? "1" : "0");
            }
            if (i < this.unit.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
