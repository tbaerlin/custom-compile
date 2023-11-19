/*
 * PriceQuality.java
 *
 * Created on 10.07.2006 18:02:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum PriceQuality {
    // order is important    
    REALTIME, DELAYED, END_OF_DAY, NONE;

    public static final Set<PriceQuality> REALTIME_OR_DELAYED = EnumSet.of(REALTIME, DELAYED);

    public static final Set<PriceQuality> NOT_NONE = EnumSet.complementOf(EnumSet.of(NONE));

    /**
     * Returns the minimum quality of <code>pq1</code> and <code>pq2</code>, i.e., the quality
     * with the least rights/visibility.
     * @param pq1
     * @param pq2
     * @return minimum of this and other
     */
    public static PriceQuality min(PriceQuality pq1, PriceQuality pq2) {
        return pq1.ordinal() < pq2.ordinal() ? pq2 : pq1;
    }


    /**
     * Returns the maximum quality of <code>pq1</code> and <code>pq2</code>, i.e., the quality
     * with the most rights/visibility.
     * @param pq1
     * @param pq2
     * @return maximum of this and other
     */
    public static PriceQuality max(PriceQuality pq1, PriceQuality pq2) {
        return pq1.ordinal() < pq2.ordinal() ? pq1 : pq2;
    }
}
