/*
 * Selectable.java
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
public interface Selectable {
    /**
     * return String value for the specified field
     * @param fieldid specifies which value should be returned
     * @return value or null if none is available
     */
    String getString(int fieldid);

    /**
     * return String value for the specified field in the given locale
     * @param fieldid specifies which value should be returned
     * @param localeIndex specifies locale index position for the respective field
     * @return value or null if none is available; if the locale is not existent, the default locale (first in order for field) is returned
     */
    String getString(int fieldid, int localeIndex);

    /**
     * return Number value for the specified field
     * @param fieldid specifies which value should be returned
     * @return value or null if none is available
     */
    Long getLong(int fieldid);

    /**
     * return BitSet value for the specified field
     * @param fieldid specifies which value should be returned
     * @return value or null if none is available
     */
    BitSet getBitSet(int fieldid);

    /**
     * return Number value for the specified field
     * @param fieldid specifies which value should be returned
     * @return value or null if none is available
     */
    Integer getInt(int fieldid);

    /**
     * return Boolean value for the specified field
     * @param fieldid specifies which value should be returned
     * @return value or null if none is available
     */
    Boolean getBoolean(int fieldid);

}
