/*
 * BNDFinderElementMapper.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.dmxml.BNDFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BNDFinderElementMapper {

    static final RowMapper<BNDFinderElement> BASE_ROW_MAPPER = new AbstractRowMapper<BNDFinderElement>() {
        public Object[] mapRow(BNDFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    qwi,
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    qwi,
                    e.getQuotedata().getMarketName(),
                    e.getPrice(),
                    e.getQuotedata().getCurrencyIso(),
                    e.getBid(),
                    e.getAsk(),
                    e.getTotalVolume(),
                    e.getDate(),
                    e.getDate(),
                    e.getIssuername(),
                    e.getIssuerCategory(),
                    e.getSector(),
                    e.getCouponType(),
                    e.getBondType(),
                    e.getCountry(),
                    e.getCoupon(),
                    e.getExpirationDate(),
                    e.getSmallestTransferableUnit()
            };
        }
    };

    static final AbstractRowMapper<BNDFinderElement> RATIO_ROW_MAPPER = new AbstractRowMapper<BNDFinderElement>() {
        public Object[] mapRow(BNDFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    qwi,
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getQuotedata().getMarketName(),
                    e.getPrice(),
                    e.getDate(),
                    e.getYieldRelativePerYear(),
                    e.getModifiedDuration(),
                    e.getDuration(),
                    e.getBrokenPeriodInterest(),
                    e.getBasePointValue(),
                    e.getConvexity(),
                    e.getInterestRateElasticity(),
                    e.getRatingFitchLongTerm(),
                    e.getRatingMoodys()
            };
        }
    };

    static final RowMapper<BNDFinderElement> RISK_ROW_MAPPER = new AbstractRowMapper<BNDFinderElement>() {
        public Object[] mapRow(BNDFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    qwi,
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getQuotedata().getMarketName(),
                    e.getPrice(),
                    e.getDate(),
                    e.getVolatility3M(),
                    e.getVolatility6M(),
                    e.getVolatility1Y(),
                    e.getVolatility3Y()
            };
        }
    };

    static final RowMapper<BNDFinderElement> PERF_ROW_MAPPER = new AbstractRowMapper<BNDFinderElement>() {
        public Object[] mapRow(BNDFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    qwi,
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getQuotedata().getMarketName(),
                    e.getPrice(),
                    e.getDate(),
                    e.getPerformanceCurrentYear(),
                    e.getPerformance1W(),
                    e.getPerformance1M(),
                    e.getPerformance3M(),
                    e.getPerformance6M(),
                    e.getPerformance1Y(),
                    e.getPerformance3Y(),
                    e.getPerformance5Y(),
                    e.getPerformance10Y()
            };
        }
    };

    static QuoteWithInstrument createQuoteWithInstrument(BNDFinderElement e) {
        return AbstractFinder.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }
}
