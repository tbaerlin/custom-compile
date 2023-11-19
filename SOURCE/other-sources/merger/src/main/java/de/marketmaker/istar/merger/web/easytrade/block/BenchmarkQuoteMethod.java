/*
 * BenchmarkQuoteMethod.java
 *
 * Created on 31.08.2009 09:41:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.web.easytrade.util.FndBenchmarkQuotes;

/**
 * Computes the benchmark quote for a given quote.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class BenchmarkQuoteMethod {
    private final static long DAX_QUOTEID = 106547L;
    private final static long MSCI_WORLD_QUOTEID = 10353L;

    private static final Map<Long, BigDecimal> DEFAULT = Collections.singletonMap(DAX_QUOTEID, BigDecimal.ONE);
    private static final Map<Long, BigDecimal> DEFAULT_FUND = Collections.singletonMap(MSCI_WORLD_QUOTEID, BigDecimal.ONE);

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkQuoteMethod.class);

    private Quote quote;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private FundDataProvider fundDataProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private MasterDataFund masterData;

    public BenchmarkQuoteMethod(Quote quote, ProfiledIndexCompositionProvider indexCompositionProvider,
            FundDataProvider fundDataProvider, EasytradeInstrumentProvider instrumentProvider,
            MasterDataFund masterData) {
        this.quote = quote;
        this.indexCompositionProvider = indexCompositionProvider;
        this.fundDataProvider = fundDataProvider;
        this.instrumentProvider = instrumentProvider;
        this.masterData = masterData;
    }

    public Quote invoke() {
        final Map<Long, BigDecimal> bqids = getBenchmarkQids();
        return bqids == null || bqids.isEmpty()
                ? null
                : this.instrumentProvider.identifyQuotes(bqids.keySet()).get(0);
    }

    public Map<Quote, BigDecimal> invokeList() {
        final Map<Long, BigDecimal> bqids = getBenchmarkQids();

        if (bqids == null || bqids.isEmpty()) {
            return null;
        }

        final Map<Quote, BigDecimal> quote2Share = new LinkedHashMap<>();

        final List<Long> qids = new ArrayList<>(bqids.keySet());
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<invokeList> " + this.quote + ": benchmark " + qids.get(i)
                            + ".qid not available for " + RequestContextHolder.getRequestContext().getProfile().getName());
                }
                // if a quote is null (probably because not allowed), it does not make sense
                // to return the other parts of a composite benchmark, so return none at all.
                return null;
            }
            quote2Share.put(quote, bqids.get(quote.getId()));
        }
        return quote2Share;
    }

    private Map<Long, BigDecimal> getBenchmarkQids() {
        if (isType(InstrumentTypeEnum.STK) || isType(InstrumentTypeEnum.BND) || isType(InstrumentTypeEnum.CUR) || isType(InstrumentTypeEnum.GNS)) {
            final Long qid = this.indexCompositionProvider.getBenchmarkId(this.quote.getInstrument());
            return qid != null ? Collections.singletonMap(qid, BigDecimal.ONE) : DEFAULT;
        }

        if (this.fundDataProvider != null && isType(InstrumentTypeEnum.FND)) {
            final MasterDataFund md = getMasterDataFund();
            if (md != null && md.getBenchmarkQid() != null) {
                return Collections.singletonMap(md.getBenchmarkQid(), BigDecimal.ONE);
            }

            final FndBenchmarkQuotes fbq
                    = new FndBenchmarkQuotes(quote, instrumentProvider, fundDataProvider);
            fbq.computeQuotes();
            final Map<Quote, BigDecimal> quotes = fbq.getQuotesWithShares();

            if (quotes.isEmpty()) {
                return md == null || md.isWithDefaultBenchmark() ? DEFAULT_FUND : null;
            }

            final Map<Long, BigDecimal> result = new LinkedHashMap<>();
            for (final Map.Entry<Quote, BigDecimal> entry : quotes.entrySet()) {
                result.put(entry.getKey().getId(), entry.getValue());
            }
            return result;
        }

        return DEFAULT;
    }

    private MasterDataFund getMasterDataFund() {
        if (this.masterData != null) {
            return this.masterData;
        }
        final FundDataRequest fdr =
                new FundDataRequest(this.quote.getInstrument()).withMasterData();
        final FundDataResponse response = this.fundDataProvider.getFundData(fdr);
        final List<MasterDataFund> masterDataFunds = response.getMasterDataFunds();
        // TODO: use isValid after next provider update
        return masterDataFunds != null && !masterDataFunds.isEmpty() ? masterDataFunds.get(0) : null;
    }

    private boolean isType(InstrumentTypeEnum e) {
        return this.quote.getInstrument().getInstrumentType() == e;
    }
}
