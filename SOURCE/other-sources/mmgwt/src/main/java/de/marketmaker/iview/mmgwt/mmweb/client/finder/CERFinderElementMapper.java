/*
 * CERFinderElementMapper.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CERFinderElementMapper {

    static final RowMapper<CERFinderElement> BONUS_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getParticipationLevel(),
                    e.getYield(),
                    e.getYieldRelative(),
                    e.getYieldRelativePerYear(),
                    e.getGapBonusLevelRelative(),
                    e.getUnderlyingToCapRelative(),
                    e.getAgioRelative(),
                    e.getAgioRelativePerYear(),
                    e.isIsknockout() == null ? "--" : e.isIsknockout() ? I18n.I.no() : I18n.I.yes()  // $NON-NLS-0$
            };
        }
    };

    static final RowMapper<CERFinderElement> BASE_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getIssuername(),
                    e.getProductnameIssuer(),
                    e.getPrice(),
                    e.getDate(),
                    e.getDate(),
                    e.getBid(),
                    e.getAsk(),
                    e.getQuotedata().getMarketVwd(),
                    e.getQuotedata().getCurrencyIso(),
                    e.getExpirationDate()
            };
        }
    };

    static final RowMapper<CERFinderElement> KNOCKOUT_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getStoploss(),
                    e.getAgioRelative(),
                    e.getAgioRelativePerYear(),
                    e.getGapBarrier(),
                    e.getGapBarrierRelative(),
                    e.isIsknockout() == null ? "--" : e.isIsknockout() ? I18n.I.no() : I18n.I.yes()  // $NON-NLS-0$
            };
        }
    };

    static final RowMapper<CERFinderElement> EDG_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            String topClass = "n/a"; // $NON-NLS-0$
            if (e.getEdgTopClass() != null) {
                topClass = EdgUtil.getEdgRiskClassString(e.getEdgTopClass()) + " (" + e.getEdgTopClass() + ")"; // $NON-NLS$
            }
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    topClass,
                    EdgUtil.getEdgTopClassRating(e)
            };
        }
    };

    static final RowMapper<CERFinderElement> EXPRESS_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getGapBarrier(),
                    e.getGapBarrierRelative(),
                    e.isIsknockout() == null ? "--" : e.isIsknockout() ? I18n.I.no() : I18n.I.yes()  // $NON-NLS-0$
            };
        }
    };

    static final RowMapper<CERFinderElement> DISCOUNT_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getCap(),
                    e.getDiscountRelative(),
                    e.getUnchangedYieldRelative(),
                    e.getMaximumYieldRelative(),
                    e.getMaximumYieldRelativePerYear()
            };
        }
    };

    static final RowMapper<CERFinderElement> OUTPERF_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getGapStrikeRelative(),
                    e.getGapCapRelative(),
                    e.getParticipationLevel(),
                    e.getAgioRelative(),
                    e.getAgioRelativePerYear()
            };
        }
    };

    static final RowMapper<CERFinderElement> REVERSE_CONV_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getMaximumYieldRelativePerYear(),
                    e.getUnchangedYieldRelativePerYear(),
                    e.getUnderlyingToCapRelative(),
                    e.getCapToUnderlyingRelative()
            };
        }
    };

    static final RowMapper<CERFinderElement> OTHER_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            final QuoteWithInstrument qwi = createQuoteWithInstrument(e);
            return new Object[]{
                    qwi,
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getYieldRelativePerYear(),
                    e.getPerformanceAlltime()
            };
        }
    };

    static final RowMapper<CERFinderElement> SPRINT_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getCap(),
                    e.getGapCapRelative(),
                    e.getMaximumYieldRelativePerYear()
            };
        }
    };

    static final RowMapper<CERFinderElement> GUARANTEE_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getCap(),
                    e.getMaximumYieldRelativePerYear(),
                    e.getDateBarrierReached()
            };
        }
    };

    static final RowMapper<CERFinderElement> BASKET_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getYieldRelativePerYear(),
                    e.getPerformanceAlltime()
            };
        }
    };

    static final RowMapper<CERFinderElement> INDEX_ROW_MAPPER = new AbstractRowMapper<CERFinderElement>() {
        public Object[] mapRow(CERFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getPerformanceAlltime()
            };
        }
    };

    private static QuoteWithInstrument createQuoteWithInstrument(CERFinderElement e) {
        return AbstractFinder.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }
}
