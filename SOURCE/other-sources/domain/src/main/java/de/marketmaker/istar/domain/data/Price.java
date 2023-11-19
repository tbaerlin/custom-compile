/*
 * Price.java
 *
 * Created on 07.07.2006 14:59:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * An arbitrary price (e.g., ask, bid, trade, open)
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Price {
    /**
     * Any other method will only return a sensible value if this method returns true
     * @return true iff this price is defined
     */
    boolean isDefined();

    /**
     * When this price was established.
     */
    DateTime getDate();

    /**
     * Value of this price; note that the value might be zero or even negative
     */
    BigDecimal getValue();

    /**
     * The number of units (shares, etc) that were subject to this price;
     * @return number of units or zero if volume does not apply to the given price
     */
    Long getVolume();

    /**
     * Supplement issued with the price, may be null (undefined), or empty (no supplement)
     */
    String getSupplement();

    /** True iff realtime price */
    boolean isRealtime();

    /** True iff delayed price */
    boolean isDelayed();

    /** True iff end-of-day price */
    boolean isEndOfDay();
}
