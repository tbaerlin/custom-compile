/*
 * RatioUpdatable.java
 *
 * Created on 26.10.2005 10:44:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.BitSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RatioUpdatable {
    /**
     * set String value for the specified field
     * @param fieldid specifies which value should be set
     * @param value to be set
     */
    void set(int fieldid, int localeIndex, String value);

    /**
     * set Number value for the specified field
     * @param fieldid specifies which value should be set
     * @param value to be set
     */
    void set(int fieldid, long value);

    /**
     * set BitSet value for the specified field
     * @param fieldid specifies which value should be set
     * @param value to be set
     */
    void set(int fieldid, BitSet value);

    /**
     * set Number value for the specified field
     * @param fieldid specifies which value should be set
     * @param value to be set
     */
    void set(int fieldid, int value);

    /**
     * set Boolean value for the specified field
     * @param fieldid specifies which value should be set
     * @param value to be set
     */
    void set(int fieldid, boolean value);
}
