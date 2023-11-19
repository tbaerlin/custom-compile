/*
 * IntradayProviderMethod.java
 *
 * Created on 14.09.2009 14:27:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * Helps to retrieve intraday data and makes sure that requests that share the same request context
 * (usually from different atoms within the same request) will see the exactly same data. To this
 * end, the request context's intraday map is used.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class IntradayProviderMethod {
    private final IntradayProviderImpl provider;

    private List<Quote> quotes;

    private int ttl;

    /**
     * SharedIntradayData by symbolVwdfeed
     */
    private final SharedIntradayContext context
            = RequestContextHolder.getRequestContext().getIntradayContext();

    /**
     * SharedIntradayData.RequestInfo by symbolVwdfeed
     */
    private final Map<String, SharedIntradayData.RequestInfo> infos
            = new HashMap<>();

    private final LocalDate from;

    private final LocalDate to;

    IntradayProviderMethod(IntradayProviderImpl provider, List<Quote> quotes,
            Interval tickInterval, int ttl) {
        this.provider = provider;
        this.quotes = quotes;
        this.ttl = ttl;

        if (tickInterval != null) {
            this.from = tickInterval.getStart().toLocalDate();
            this.to = getEndDay(tickInterval);
        }
        else {
            this.from = null;
            this.to = null;
        }
    }

    private LocalDate getEndDay(Interval tickInterval) {
        final boolean isMidnight = tickInterval.getEnd().getSecondOfDay() == 0;
        final LocalDate result = tickInterval.getEnd().toLocalDate();
        // if interval ends on midnight, we can skip that day entirely as end is exclusive
        return isMidnight ? result.minusDays(1) : result;
    }

    List<IntradayData> invoke() {
        if (this.quotes == null || this.quotes.isEmpty()) {
            return Collections.emptyList();
        }

        final IntradayRequest ir = createIntradayRequest();
        if (ir.size() == 0 && this.infos.isEmpty()) {
            return Collections.nCopies(quotes.size(), IntradayData.NULL);
        }


        try {
            final IntradayResponse response = getResponse(ir);
            if (!response.isValid()) {
                throw new InternalFailure("no intraday data");
            }
            updateSharedData(response);
        } finally {
            // this does nothing bad in case updateSharedData was called, but makes sure to cancel
            // everything other threads might wait for if an exception occurs:
            cancel();
        }

        return createResult();
    }

    private List<IntradayData> createResult() {
        final Map<Quote, IntradayData> dataByQuote = getDataByQuote();

        final List<IntradayData> result = new ArrayList<>(this.quotes.size());
        for (Quote q : this.quotes) {
            final IntradayData data = dataByQuote.get(q);
            result.add(data != null ? data : IntradayData.NULL);
        }

        return result;
    }

    private IntradayResponse getResponse(IntradayRequest ir) {
        if (ir.size() == 0) {
            return new IntradayResponse();
        }
        return this.provider.getIntradayData(ir, this.ttl);
    }

    private Map<Quote, IntradayData> getDataByQuote() {
        final Map<Quote, IntradayData> result = new HashMap<>();
        for (Quote q : this.quotes) {
            if (q == null || !StringUtils.hasText(q.getSymbolVwdfeed())) {
                continue;
            }

            final SharedIntradayData.RequestInfo info = this.infos.get(q.getSymbolVwdfeed());
            if (info != null) {
                result.put(q, info.getIntradayData());
            }
        }
        return result;
    }

    private void updateSharedData(IntradayResponse response) {
        for (Map.Entry<String, SharedIntradayData.RequestInfo> entry : this.infos.entrySet()) {
            final SharedIntradayData.RequestInfo info = entry.getValue();
            final IntradayResponse.Item item = getResponseItem(response, entry.getKey());
            info.update(item);
        }
    }

    private void cancel() {
        for (Map.Entry<String, SharedIntradayData.RequestInfo> entry : this.infos.entrySet()) {
            final SharedIntradayData.RequestInfo info = entry.getValue();
            info.cancel();
        }
    }

    private IntradayResponse.Item getResponseItem(IntradayResponse response, String symbol) {
        final IntradayResponse.Item item = response.getItem(symbol);
        if (item != null) {
            return item;
        }
        return new IntradayResponse.Item(symbol, false);
    }

    private IntradayRequest createIntradayRequest() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Set<Quote> processedQuotes = new HashSet<>();

        final IntradayRequest ir = new IntradayRequest();
        ir.setTickDataFullAccess(profile.isAllowed(Selector.TICK_DATA_FULL_ACCESS));
        for (Quote q : this.quotes) {
            if (q == null) {
                continue;
            }

            if (!processedQuotes.add(q)) {
                continue;
            }

            final String symbol = q.getSymbolVwdfeed();
            if (!StringUtils.hasText(symbol)) {
                continue;
            }

            if (this.infos.containsKey(symbol)) {
                continue;
            }

            final PriceQuality priceQuality = profile.getPriceQuality(q);
            if (priceQuality == PriceQuality.NONE) {
                continue;
            }

            final SharedIntradayData data = getSharedData(profile, q, symbol, priceQuality);

            final SharedIntradayData.RequestInfo info
                    = data.addRequest(this.from, this.to);
            this.infos.put(symbol, info);

            final IntradayRequest.Item item = info.getItem();

            if (item != null) {
                ir.add(item);
            }
        }
        return ir;
    }

    private SharedIntradayData getSharedData(Profile profile, Quote q, String symbol,
            PriceQuality priceQuality) {
        final SharedIntradayData data = this.context.get(symbol);
        if (data != null) {
            return data;
        }
        final ProfiledSnapRecordFactory factory = this.provider.getSnapRecordFactory(q, profile);
        final SharedIntradayData created
                = new SharedIntradayData(q, priceQuality, profile, factory, this.context);
        final SharedIntradayData existing = this.context.putIfAbsent(symbol, created);
        return (existing != null) ? existing : created;
    }
}
