/*
 * HistoricRatiosProvider.java
 *
 * Created on 17.07.2006 17:33:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

import org.joda.time.Interval;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pm5.highLow")
public interface HighLowProvider {
    List<HighLow> getHighLow(SymbolQuote quote, List<Interval> intervals);

    List<List<HighLow>> getHighLows(List<SymbolQuote> quotes, List<Interval> intervals);

    /**
     * Returns the HighLow for the past 52weeks (including intraday data) for quote
     * @param quote reference
     * @param pr current price
     * @return HighLow for quote
     */
    HighLow getHighLow52W(Quote quote, PriceRecord pr);

    /**
     * Returns the HighLows for the past 52weeks (including intraday data) for quotes
     * @param quotes reference
     * @param prs current prices
     * @return HighLows for quotes
     */
    List<HighLow> getHighLows52W(List<Quote> quotes, List<PriceRecord> prs);
}
