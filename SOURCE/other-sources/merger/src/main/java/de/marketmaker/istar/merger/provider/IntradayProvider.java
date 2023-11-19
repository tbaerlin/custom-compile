/*
 * IntradayProvider.java
 *
 * Created on 07.07.2006 11:17:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.OrderbookData;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.api.VendorkeyListRequest;
import de.marketmaker.istar.feed.api.VendorkeyListResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IntradayProvider {
    /**
     * Convenience method to obtain PriceRecords, so that caller does not have to extract
     * the data from IntradayData objects.
     *
     * @return list of PriceRecords for all given quotes;
     */
    List<PriceRecord> getPriceRecords(List<Quote> quotes);

    /**
     * Convenience method to be used when data for only one quote is required
     *
     * @return IntradayData for q
     */
    IntradayData getIntradayData(Quote q, Interval tickInterval);

    /**
     * Returns a list of IntradayData objects that correspond to the Quote objects in quotes.
     *
     * @param quotes for which to request IntradayData.
     * @param tickInterval if not null, ticks will be retrieved for all days between
     * the interval's start and end day
     * @return list of IntradayData objects
     */
    List<IntradayData> getIntradayData(List<Quote> quotes, Interval tickInterval);

    /**
     * Returns IntradayData for a list of quotes; the list may contain duplicate quotes without
     * having a negative impact on performance.
     *
     * @param quotes for which to request IntradayData.
     * @param tickInterval if not null, ticks will be retrieved for all days between
     * the interval's start and end day
     * @param ttl if &gt;0, the IntradayResponse will be cached for this many seconds. Caching
     * is only enabled for requests that include tick data for the current day
     * @return list of IntradayData objects corresponding to the quote list, will not contain null
     * elements.
     */
    List<IntradayData> getIntradayData(List<Quote> quotes, Interval tickInterval, int ttl);

    List<AggregatedTickImpl> getAggregatedTrades(Quote quote, DateTime start, DateTime end,
            Duration aggregation, TickType tickType);

    List<AggregatedTickImpl> getAggregatedTrades(Quote quote, List<Interval> intervals,
            Duration aggregation, TickType tickType);

    List<AggregatedTickImpl> getAggregatedTrades(Quote quote, Interval interval,
            Duration aggregation, TickType tickType, int minTickNum, boolean alignWithStart);

    List<AggregatedTickImpl> getAggregatedTrades4TradeScreen(Quote quote, Interval interval,
            Duration aggregation);

    PageResponse getPage(PageRequest request);

    VendorkeyListResponse getVendorkeys(VendorkeyListRequest request);

    TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request);

    OrderbookData getOrderbook(Quote quote);

    /**
     * Returns true iff orderbook data for the given quote is available, taking into account
     * the current profile.
     *
     * @param quote to be queried
     * @return true iff available
     */
    boolean isWithOrderbook(Quote quote);
}
