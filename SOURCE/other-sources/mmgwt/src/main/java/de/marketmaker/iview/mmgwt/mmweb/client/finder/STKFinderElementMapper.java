/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.STKFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import java.util.Arrays;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.FIVE_YEARS;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.Item;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.ONE_MONTH;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.ONE_WEEK;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.ONE_YEAR;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.SIX_MONTHS;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.TEN_YEARS;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.THREE_YEARS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class STKFinderElementMapper {
    private static final List<Item> P_ALL
            = Arrays.asList(ONE_WEEK, ONE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS, FIVE_YEARS, TEN_YEARS);

    static final RowMapper<STKFinderElement> FUNDAMENTAL_ROW_MAPPER
            = new AbstractRowMapper<STKFinderElement>() {
        public Object[] mapRow(STKFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    getWkn(e),
                    getIsin(e),
                    qwi,
                    e.getPriceEarningRatio1Y(),
                    e.getPriceEarningRatio2Y(),
                    e.getPriceSalesRatio1Y(),
                    e.getPriceSalesRatio2Y(),
                    e.getPriceCashflowRatio1Y(),
                    e.getPriceCashflowRatio2Y(),
                    e.getPriceBookvalueRatio1Y(),
                    e.getPriceBookvalueRatio2Y(),
                    e.getDividendYield1Y(),
                    e.getDividendYield2Y(),
                    e.getSales1Y(),
                    e.getSales2Y(),
                    e.getProfit1Y(),
                    e.getProfit2Y(),
                    e.getEbit1Y(),
                    e.getEbit2Y(),
                    e.getEbitda1Y(),
                    e.getEbitda2Y(),
                    getScreenerLink(qwi, e.getScreenerInterest()),
                    e.getRecommendation()
            };
        }
    };

    static final RowMapper<STKFinderElement> BENCHMARK_ROW_MAPPER
            = new AbstractRowMapper<STKFinderElement>() {
        public Object[] mapRow(STKFinderElement e) {
            return new Object[]{
                    getWkn(e),
                    getIsin(e),
                    createQuoteWithInstrument(e),
                    e.getPerformanceToBenchmarkCurrentYear(),
                    e.getPerformanceToBenchmark1W(),
                    e.getPerformanceToBenchmark1M(),
                    e.getPerformanceToBenchmark3M(),
                    e.getPerformanceToBenchmark6M(),
                    e.getPerformanceToBenchmark1Y(),
                    e.getPerformanceToBenchmark3Y(),
                    e.getPerformanceToBenchmark5Y(),
                    e.getPerformanceToBenchmark10Y(),
                    e.getCorrelationCurrentYear(),
                    e.getCorrelation1W(),
                    e.getCorrelation1M(),
                    e.getCorrelation3M(),
                    e.getCorrelation6M(),
                    e.getCorrelation1Y(),
                    e.getCorrelation3Y(),
                    e.getCorrelation5Y(),
                    e.getCorrelation10Y(),
                    e.getBenchmarkName()
            };
        }
    };

    static final AbstractRowMapper<STKFinderElement> PERF_ROW_MAPPER
            = new AbstractRowMapper<STKFinderElement>() {
        public Object[] mapRow(STKFinderElement e) {
            return new Object[]{
                    getWkn(e),
                    getIsin(e),
                    createQuoteWithInstrument(e),
                    e.getPerformanceCurrentYear(),
                    e.getPerformance1W(),
                    e.getPerformance1M(),
                    e.getPerformance3M(),
                    e.getPerformance6M(),
                    e.getPerformance1Y(),
                    e.getPerformance3Y(),
                    e.getPerformance5Y(),
                    e.getPerformance10Y(),
                    e.getChangePercentHigh1Y(),
                    e.getChangePercentLow1Y(),
                    e.getChangePercentHighAlltime(),
                    e.getAverageVolume1W(),
                    e.getAverageVolume3M(),
                    e.getAverageVolume1Y(),
                    e.getAverageVolume3Y(),
                    e.getAverageVolume5Y()
            };
        }
    };

    static final AbstractRowMapper<STKFinderElement> BASE_ROW_MAPPER = new AbstractRowMapper<STKFinderElement>() {
        public Object[] mapRow(STKFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    qwi,
                    getWkn(e),
                    getIsin(e),
                    createQuoteWithInstrument(e),
                    e.getPrice(),
                    e.getVolumeTrade(),
                    e.getChangeNet(),
                    e.getChangePercent(),
                    e.getVolume(),
                    e.getBid(),
                    e.getAsk(),
                    e.getDate(),
                    e.getQuotedata() == null ? null : e.getQuotedata().getMarketName(),
                    e.getMarketCapitalization(),
                    e.getMarketCapitalizationEUR(),
                    e.getMarketCapitalizationUSD(),
                    e.getSector()
            };
        }
    };

    static final RowMapper<STKFinderElement> RISK_ROW_MAPPER = new AbstractRowMapper<STKFinderElement>() {
        public Object[] mapRow(STKFinderElement e) {
            return new Object[]{
                    getWkn(e),
                    getIsin(e),
                    createQuoteWithInstrument(e),
                    e.getVolatilityCurrentYear(),
                    e.getVolatility1W(),
                    e.getVolatility1M(),
                    e.getVolatility3M(),
                    e.getVolatility6M(),
                    e.getVolatility1Y(),
                    e.getVolatility3Y(),
                    e.getVolatility5Y(),
                    e.getVolatility10Y(),
                    e.getBeta1M(),
                    e.getBeta1Y(),
                    e.getAlpha1M(),
                    e.getAlpha1Y(),
            };
        }
    };

    private static Link getScreenerLink(final QuoteWithInstrument qwi, final String irst) {
        if (irst == null || qwi == null) {
            return null;
        }
        final String img = "<img src=\"images/mm/screener/irst" + irst + ".gif\" width=\"47\" height=\"11\"/>"; // $NON-NLS$
        return new Link(new LinkListener<Link>() {
            public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                qwi.goToScreener();
            }
        }, img, null);
    }

    static QuoteWithInstrument createQuoteWithInstrument(STKFinderElement e) {
        if (e.getInstrumentdata() == null || e.getQuotedata() == null) {
            return null;
        }
        return AbstractFinder.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }

    static String getIsin(STKFinderElement e) {
        return e.getInstrumentdata() == null ? null : e.getInstrumentdata().getIsin();
    }

    static String getWkn(STKFinderElement e) {
        return e.getInstrumentdata() == null ? null : e.getInstrumentdata().getWkn();
    }

}
