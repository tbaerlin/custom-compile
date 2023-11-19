/*
 * QuoteFilter.java
 *
 * Created on 25.11.2009 14:46:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.List;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * @author oflege
 */
public interface QuoteSelector {
    /**
     * Selects a Quote from quotes. If quotes is empty, an implementation may consider to
     * return any of the quotes provided by the instrument.
     * @param instrument
     * @param quotes
     * @return selecte Quote or null if this selector is not applicable or cannot find any
     * suitable quote.
     */
    Quote select(Instrument instrument, List<Quote> quotes);
}