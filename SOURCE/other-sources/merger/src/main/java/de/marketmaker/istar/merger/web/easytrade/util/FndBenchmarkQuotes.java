/*
 * FndBenchmarkQuotes.java
 *
 * Created on 17.12.2007 16:29:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategyFactory;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndBenchmarkQuotes {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final EasytradeInstrumentProvider instrumentProvider;

    protected final FundDataProvider fundDataProvider;

    protected final Quote quote;

    protected final Map<String, Object> result = new HashMap<>();

    protected final Map<Quote, BigDecimal> quotes = new LinkedHashMap<>();

    public FndBenchmarkQuotes(Quote quote, EasytradeInstrumentProvider instrumentProvider,
            FundDataProvider fundDataProvider) {
        this.instrumentProvider = instrumentProvider;
        this.fundDataProvider = fundDataProvider;
        this.quote = quote;
    }

    public List<Quote> getQuotes() {
        return new ArrayList<>(this.quotes.keySet());
    }

    public Map<Quote,BigDecimal> getQuotesWithShares() {
        return Collections.unmodifiableMap(this.quotes);
    }

    public final void computeQuotes() {
        final FundDataRequest fdr = new FundDataRequest(this.quote.getInstrument())
                .withBenchmarks();

        final FundDataResponse fundResponse = this.fundDataProvider.getFundData(fdr);
        if (!fundResponse.isValid()) {
            return;
        }

        final List<InstrumentAllocation> ias = fundResponse.getBenchmarksList().get(0);

        if (ias.isEmpty()) {
            return;
        }

        final Map<Long, BigDecimal> iid2share = new HashMap<>();

        final List<Long> iids = new ArrayList<>(ias.size());
        for (final InstrumentAllocation ia : ias) {
            iids.add(ia.getId());
            iid2share.put(ia.getId(), ia.getShare());
        }

        final List<Instrument> instruments = this.instrumentProvider.identifyInstruments(iids);
        if (instruments == null || instruments.isEmpty()) {
            return;
        }

        final List<Quote> qs = getBenchmarkQuotes(instruments);
        if (qs == null) {
            return;
        }

        for (final Quote q : qs) {
            this.quotes.put(q, iid2share.get(q.getInstrument().getId()));
        }
    }

    private List<Quote> getBenchmarkQuotes(List<Instrument> instruments) {
        final List<Quote> result = new ArrayList<>(instruments.size());
        final MarketStrategy strategy
                = new MarketStrategy.Builder(MarketStrategyFactory.getDefaultMarketStrategy())
                .withFilter(QuoteFilters.WITH_PRICES)
                .withSelector(QuoteSelectors.MSCI_INDEX)
                .build();
        for (final Instrument instrument : instruments) {
            try {
                final Quote bq = this.instrumentProvider.getQuote(instrument, strategy);
                if (bq == null) {
                    return null;
                }
                result.add(bq);
            }
            catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<compute> benchmark not allowed: " + instrument.getId()
                            + " for profile " + RequestContextHolder.getRequestContext().getProfile().getName());
                }
                return null;
            }
        }
        return result;
    }

}
