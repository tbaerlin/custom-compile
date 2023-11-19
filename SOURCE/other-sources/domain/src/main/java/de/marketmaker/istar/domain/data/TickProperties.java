/*
 * TickProperties.java
 *
 * Created on 08.04.2005 13:50:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TickProperties {
    /**
     * returns true if the tick was associated with a kassa field
     */
    boolean isWithKassa();

    /**
     * returns true if the tick was associated with a kassa field
     */
    boolean isWithClose();

    /**
     * returns true if the tick was associated with a yield field
     */
    boolean isWithYield();

    /**
     * returns the yield associated with a tick, only defined if {@link #isYield()} returns true
     */
    long getYield();
}
