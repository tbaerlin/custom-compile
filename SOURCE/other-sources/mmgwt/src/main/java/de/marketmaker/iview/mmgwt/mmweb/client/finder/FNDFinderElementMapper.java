/*
 * FinderFND.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FNDFinderElementMapper {

    static final RowMapper<FNDFinderElement> RATIO_ROW_MAPPER = new AbstractRowMapper<FNDFinderElement>() {
        public Object[] mapRow(FNDFinderElement e) {
            final String rating = e.getRatingMorningstar();
            final String img = (rating == null)
                    ? null : "<img src=\"" + Settings.INSTANCE.morningstarStarUrl().replace("{rating}", rating) + "\"/>"; // $NON-NLS$
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getVolatility1Y(),
                    e.getVolatility3Y(),
                    e.getVolatility5Y(),
                    e.getSharpeRatio1Y(),
                    e.getSharpeRatio3Y(),
                    e.getSharpeRatio5Y(),
                    e.getSharpeRatio10Y(),
                    e.getRatingFeri(),
                    img,
                    e.getSrriValue(),
                    e.getDiamondRating()
            };
        }
    };

    static final RowMapper<FNDFinderElement> PERF_ROW_MAPPER = new AbstractRowMapper<FNDFinderElement>() {
        public Object[] mapRow(FNDFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getBviperformance1D(),
                    e.getBviperformance1W(),
                    e.getBviperformance1M(),
                    e.getBviperformance3M(),
                    e.getBviperformance6M(),
                    e.getBviperformance1Y(),
                    e.getBviperformance3Y(),
                    e.getBviperformance5Y(),
                    e.getBviperformance10Y(),
                    e.getMaximumLoss3Y()
            };
        }
    };

    static final RowMapper<FNDFinderElement> STATIC_ROW_MAPPER = new AbstractRowMapper<FNDFinderElement>() {
        public Object[] mapRow(FNDFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getIssuername(),
                    e.getInvestmentFocus(),
                    e.getFundVolume(),
                    e.getIssueDate(),
                    e.getDistributionStrategy(),
                    e.getIssueSurcharge(),
                    e.getManagementFee(),
                    e.getAccountFee(),
                    e.getOngoingCharge(),
                    e.getTer()
            };
        }
    };

    static final RowMapper<FNDFinderElement> BASE_ROW_MAPPER = new AbstractRowMapper<FNDFinderElement>() {
        public Object[] mapRow(FNDFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getFundtype(),
                    e.getWmInvestmentAssetPoolClass(),
                    e.getIssuePrice(),
                    e.getRepurchasingPrice(),
                    e.getDate(),
                    e.getQuotedata().getMarketName(),
                    e.getChangeNet(),
                    e.getChangePercent()
            };
        }
    };

    private static QuoteWithInstrument createQuoteWithInstrument(FNDFinderElement e) {
        return AbstractFinder.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }
}
