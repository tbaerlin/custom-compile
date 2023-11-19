/*
 * FndBenchmarkPerformance.java
 *
 * Created on 10.12.2007 13:45:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Interval;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * Method object that computes a model containing the performances and volas
 * of a fund's benchmark for several intervals. Can also handle composite benchmarks.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndBenchmarkRatios extends FndBenchmarkQuotes {


    private final HistoricRatiosProvider historicRatiosProvider;

    // todo: this is kind of crazy, why not use computed names? adapt template(s)!
    public static final String PERF_P1M = "bperf1Month";

    public static final String PERF_P6M = "bperf6Months";

    public static final String PERF_P1Y = "bperf1Year";

    public static final String PERF_P3Y = "bperf3Years";

    public static final String PERF_P5Y = "bperf5Years";

    public static final String PERF_P10Y = "bperf10Years";

    public static final String VOLA_P1M = "bvola1Month";

    public static final String VOLA_P6M = "bvola6Months";

    public static final String VOLA_P1Y = "bvola1Year";

    public static final String VOLA_P3Y = "bvola3Years";

    public static final String VOLA_P5Y = "bvola5Years";

    public static final String VOLA_P10Y = "bvola10Years";

    private static final String[] PERFNAMES = new String[]{
            PERF_P1M, PERF_P6M, PERF_P1Y, PERF_P3Y, PERF_P5Y, PERF_P10Y
    };

    private static final String[] VOLANAMES = new String[]{
            VOLA_P1M, VOLA_P6M, VOLA_P1Y, VOLA_P3Y, VOLA_P5Y, VOLA_P10Y
    };

    private static final String[] INTERVALS = new String[]{
            "P1M", "P6M", "P1Y", "P3Y", "P5Y", "P10Y"
    };

    private BigDecimal[] perfs = new BigDecimal[PERFNAMES.length];

    private BigDecimal[] volas = new BigDecimal[PERFNAMES.length];


    public FndBenchmarkRatios(EasytradeInstrumentProvider instrumentProvider,
            FundDataProvider fundDataProvider, HistoricRatiosProvider historicRatiosProvider,
            Quote quote) {
        super(quote, instrumentProvider, fundDataProvider);
        this.historicRatiosProvider = historicRatiosProvider;
        Arrays.fill(this.perfs, BigDecimal.ZERO);
        Arrays.fill(this.volas, BigDecimal.ZERO);
    }

    private List<Interval> getIntervals() {
        final List<Interval> result = new ArrayList<>(INTERVALS.length);
        for (final String s : INTERVALS) {
            result.add(DateUtil.getInterval(s));
        }
        return result;
    }

    /**
     * Computes the benchmark's performance
     * @return a model containing the performances or an empty model if no benchmark is defined
     *         or its performances cannot be determined.
     */
    public Map<String, Object> compute() {
        computeQuotes();
        if (this.quotes.isEmpty()) {
            return this.result;
        }

        this.result.put("benchmarks", getQuotes());

        final List<Interval> intervals = getIntervals();
        final Map<Long, List<BasicHistoricRatios>> qid2ratios = getHistoricRatios(intervals);

        for (final Map.Entry<Quote, BigDecimal> entry : this.quotes.entrySet()) {
            final BigDecimal share = entry.getValue();
            final List<BasicHistoricRatios> ratios = qid2ratios.get(entry.getKey().getId());
            addPerformances(share, ratios);
            addVolas(share, ratios);
        }

        for (int i = 0; i < INTERVALS.length; i++) {
            if (perfs[i] != null) {
                result.put(PERFNAMES[i], this.perfs[i]);
            }
            if (volas[i] != null) {
                result.put(VOLANAMES[i], this.volas[i]);
            }
        }

        return result;
    }

    private Map<Long, List<BasicHistoricRatios>> getHistoricRatios(final List<Interval> intervals) {
        final Map<Long, List<BasicHistoricRatios>> qid2ratios = new HashMap<>();
        for (Map.Entry<Quote, BigDecimal> entry : this.quotes.entrySet()) {
            qid2ratios.put(entry.getKey().getId(), this.historicRatiosProvider.getBasicHistoricRatios(SymbolQuote.create(entry.getKey()), null, intervals));
        }
        return qid2ratios;
    }

    private void addPerformances(BigDecimal share, final List<BasicHistoricRatios> bhrs) {
        for (int i = 0; i < this.perfs.length; i++) {
            if (this.perfs[i] == null) {
                continue;
            }
            final BigDecimal perf = bhrs.get(i).getPerformance();
            if (perf == null) {
                this.perfs[i] = null;
                continue;
            }
            this.perfs[i] = this.perfs[i].add(share.multiply(perf, Constants.MC));
        }
    }

    private void addVolas(BigDecimal share, final List<BasicHistoricRatios> bhrs) {
        for (int i = 0; i < this.volas.length; i++) {
            if (this.volas[i] == null) {
                continue;
            }
            final BigDecimal vola = bhrs.get(i).getVolatility();
            if (vola == null) {
                this.volas[i] = null;
                continue;
            }
            this.volas[i] = this.volas[i].add(share.multiply(vola, Constants.MC));
        }
    }
}
