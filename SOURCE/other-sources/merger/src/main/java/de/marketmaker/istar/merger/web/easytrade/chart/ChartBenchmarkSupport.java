/*
 * ChartBenchmarkSupport.java
 *
 * Created on 09.04.14 17:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.util.FndBenchmarkQuotes;

/**
 * Encapsulates how benchmarks for a chart are identified
 * @author oflege
 */
public class ChartBenchmarkSupport {

    private static final Quote[] NO_QUOTES = new Quote[0];

    private final ImgChartAnalyseCommand cmd;

    private final EasytradeInstrumentProvider instrumentProvider;

    private final FundDataProvider fundDataProvider;

    private final Quote quote;

    private final List<BigDecimal> shares = new ArrayList<>();

    public ChartBenchmarkSupport(ImgChartAnalyseCommand cmd, Quote quote, EasytradeInstrumentProvider instrumentProvider,
            FundDataProvider fundDataProvider) {
        this.cmd = cmd;
        this.quote = quote;
        this.instrumentProvider = instrumentProvider;
        this.fundDataProvider = fundDataProvider;
    }

    List<BigDecimal> getShares() {
        return shares;
    }

    public Quote[] identifyBenchmarks() {
        if (this.cmd.getBenchmark() == null || this.cmd.getBenchmark().length == 0) {
            return NO_QUOTES;
        }
        if (InstrumentUtil.isVwdFund(this.quote) && "asProvided".equals(this.cmd.getBenchmark()[0])) {
            return identifyProvidedBenchmark();
        }

        Quote[] benchmarks = new Quote[this.cmd.getBenchmark().length];
        for (int i = 0; i < this.cmd.getBenchmark().length; i++) {
            final String b = this.cmd.getBenchmark()[i];
            if (!StringUtils.hasText(b)) {
                continue;
            }
            try {
                final Quote bq = instrumentProvider.identifyQuote(b, this.cmd.getBenchmarkSymbolStrategy(), null, null);
                if (bq != null) {
                    benchmarks[i] = bq;
                }
            } catch (UnknownSymbolException e) {
                // ignore unknown benchmark
            }
        }
        return benchmarks;
    }

    private Quote[] identifyProvidedBenchmark() {
        final FndBenchmarkQuotes benchmarkQuotes = new FndBenchmarkQuotes(this.quote,
                this.instrumentProvider, this.fundDataProvider);
        benchmarkQuotes.computeQuotes();

        final Map<Quote, BigDecimal> quote2share = benchmarkQuotes.getQuotesWithShares();

        Quote[] benchmarks = new Quote[quote2share.size()];

        int index=0;
        for (final Map.Entry<Quote, BigDecimal> entry : quote2share.entrySet()) {
            benchmarks[index++] = entry.getKey();
            this.shares.add(entry.getValue());
        }
        return benchmarks;
    }
}
