/*
 * UnmodifiableBitSet.java
 *
 * Created on 15.01.2007 08:31:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.BitSet;

/**
 * A BitSet that cannot be modified after it has been created. All methods that could possibly
 * change a BitSet will throw an UnsupportedOperationException.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnmodifiableBitSet extends BitSet {
    /**
     * Creates an UnmodifiableBitSet from the source BitSet. Modifications of source afterwards
     * will not change this object.
     * @param source
     */
    public UnmodifiableBitSet(BitSet source) {
        super(source.size());
        super.or(source);
    }

    public void and(BitSet set) {
        throw new UnsupportedOperationException();
    }

    public void andNot(BitSet set) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public void clear(int bitIndex) {
        throw new UnsupportedOperationException();
    }

    public void clear(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public void flip(int bitIndex) {
        throw new UnsupportedOperationException();
    }

    public void flip(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public void or(BitSet set) {
        throw new UnsupportedOperationException();
    }

    public void set(int bitIndex) {
        throw new UnsupportedOperationException();
    }

    public void set(int bitIndex, boolean value) {
        throw new UnsupportedOperationException();
    }

    public void set(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public void set(int fromIndex, int toIndex, boolean value) {
        throw new UnsupportedOperationException();
    }

    public void xor(BitSet set) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "UnmodifiableBitSet" + super.toString();
    }
}
