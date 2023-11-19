/*
 * OrderedEntitlement.java
 *
 * Created on 04.10.13 09:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.util;

import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.Immutable;

/**
 * Immutable BitSet that does not store the first n words of an ordinary BitSet if they are empty.
 * @author oflege
 */
@Immutable
public class CompactBitSet implements Comparable<CompactBitSet> {
    /**
     * a multiple of 64 that is used as base offset for all bits contained in this set.
     * this is to avoid lots of 0 values for exotic field groups that only contain large values
     */
    private final int base;

    private final int min;

    private final int max;

    private final long[] words;

    public static CompactBitSet fromTo(int from, int to) {
        BitSet result = new BitSet(to);
        result.set(from, to);
        return new CompactBitSet(result);
    }

    public CompactBitSet(BitSet bs) {
        if (bs.length() == 0) {
            throw new IllegalArgumentException("empty bitset");
        }
        this.min = bs.nextSetBit(0);
        this.max = bs.length() - 1;

        this.base = this.min & ~0x3F;
        final int length = 1 + ((bs.length() - 1 - base) >> 6);
        this.words = new long[length];
        for (int j = this.min; j >= 0; j = bs.nextSetBit(j + 1)) {
            words[wordIndex(j)] |= (1L << j);
        }
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean get(int i) {
        if (i < this.base) {
            return false;
        }
        final int idx = wordIndex(i);
        return idx < words.length && (words[idx] & (1L << i)) != 0;
    }

    /**
     * Tests whether this set intersects the given set that has a base of 0 and uses only
     * the first wordsInUse array elements
     * @return true if the sets intersect
     */
    public boolean intersects(long[] words0, final int wordsInUse) {
        final int offset = this.base >> 6;
        for (int i = offset, n = Math.min(offset + this.words.length, wordsInUse); i < n; i++) {
            if ((words0[i] & this.words[i - offset]) != 0) {
                return true;
            }
        }
        return false;
    }

    private int wordIndex(int k) {
        return (k - this.base) >> 6;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompactBitSet that = (CompactBitSet) o;

        return (base == that.base) && Arrays.equals(words, that.words);
    }

    @Override
    public int hashCode() {
        return 31 * base + Arrays.hashCode(words);
    }

    @Override
    public int compareTo(CompactBitSet o) {
        return Integer.compare(this.min, o.min);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append('{');
        for (int i = 0; i < this.words.length; i++) {
            int k = this.base + i * 64;
            long bits = this.words[i];
            for (int b = 0; b < 64; b++) { // we could do s.th. more efficient but this is hardly used
                if ((bits & (1L << b)) != 0) {
                    if (sb.length() > 1) sb.append(", ");
                    sb.append(k + b);
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    BitSet toBitSet() {
        long[] longs = new long[(this.base >> 6) + this.words.length];
        System.arraycopy(this.words, 0, longs, longs.length - this.words.length, this.words.length);
        return BitSet.valueOf(longs);
    }

    public CompactBitSet or(CompactBitSet other) {
        final BitSet bs = toBitSet();
        bs.or(other.toBitSet());
        return new CompactBitSet(bs);
    }
}
