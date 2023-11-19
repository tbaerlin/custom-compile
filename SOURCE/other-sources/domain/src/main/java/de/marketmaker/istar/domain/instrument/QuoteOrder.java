/*
 * QuoteOrder.java
 *
 * Created on 20.11.2009 14:24:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

/**
 * Represents different criteria for ordering quotes
 * <p>
 * <b>Implementation note</b> QuoteOrder is only used for internal purposes. The current
 * implementation in QuoteDp2 can only deal with 7 different enums in this class
 * -- if more are needed, that implementation will have to be changed!
 *
 *
 * @author oflege
 */
public enum QuoteOrder {
    /**
     * The same order as established by a
     * <code>de.marketmaker.istar.instrument.export.ByVolumeQuoteSorter</code>
     */
    VOLUME,

    /**
     * The same order as established by a
     * <code>de.marketmaker.istar.instrument.export.GermanByVolumeQuoteSorter</code>
     */
    VOLUME_DE
}
