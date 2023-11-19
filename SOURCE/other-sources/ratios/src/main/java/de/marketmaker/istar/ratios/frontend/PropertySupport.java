/*
 * InstrumentPropertySupport.java
 *
 * Created on 22.10.2007 11:58:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.BitSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class PropertySupport<T> {

    public void set(T ratios, int value) {
    }

    public void set(T ratios, long value) {
    }

    public void set(T ratios, BitSet value) {
    }

    public void set(T ratios, boolean value) {
    }

    public void set(T ratios, int localeIndex, String value) {
    }

    public boolean getBoolean(T ratios) {
        return false;
    }

    public int getInt(T ratios) {
        return Integer.MIN_VALUE;
    }

    public long getLong(T ratios) {
        return Long.MIN_VALUE;
    }

    public BitSet getBitSet(T ratios) {
        return RatioEnumSet.unmodifiableBitSet();
    }

    public String getString(T ratios) {
        return getString(ratios, 0);
    }

    public String getString(T ratios, int localeIndex) {
        return null;
    }
}
