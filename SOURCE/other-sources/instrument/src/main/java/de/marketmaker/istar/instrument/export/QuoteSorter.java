/*
 * InstrumentSorter.java
 *
 * Created on 20.08.2009 11:15:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.search.SearchSorter;

/**
 * Sorts quotes in an instrument so that quotes with a lower sort value will be retrieved
 * earlier (if this sort is actually used, that is).
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface QuoteSorter extends SearchSorter {
    /**
     * called before {@link #getOrder(de.marketmaker.istar.domain.instrument.Quote)} is called
     * for any of instrument's quotes.
     * @param instrument
     */
    void prepare(Instrument instrument);

    /**
     * Returns the sort for this quote; the returned values must be compatible the the type
     * of the SortField returned by this object's return value for
     * {@link de.marketmaker.istar.instrument.search.SearchSorter#getSortField()}.
     * @param quote for which sort is requested, will be a quote of the instrument that was used
     * in the most recent call of {@link #prepare(de.marketmaker.istar.domain.instrument.Instrument)}
     * @return order for quote
     */
    int getOrder(Quote quote);
}