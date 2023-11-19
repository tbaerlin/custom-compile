/*
 * WNTFinderElementMapper.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.WNTFinderElement;
import de.marketmaker.iview.dmxml.WNTFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WNTFinderElementMapper {

    static final RowMapper<WNTFinderElement> RATIOS_ROW_MAPPER = new AbstractRowMapper<WNTFinderElement>() {
        public Object[] mapRow(WNTFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getOmega(),
                    e.getDelta(),
                    e.getGamma(),
                    e.getRho(),
                    e.getVega(),
                    e.getTheta(),
                    e.getImpliedVolatility(),
                    e.getIntrinsicValue(),
                    e.getExtrinsicValue(),
                    e.getOptionPrice(),
                    e.getOptionPricePerYear(),
                    e.getBreakeven(),
                    e.getFairValue(),
                    e.getLeverage(),
                    e.getSpread(),
                    e.getSpreadPercent()
            };
        }
    };

    static final RowMapper<WNTFinderElement> BASE_ROW_MAPPER = new AbstractRowMapper<WNTFinderElement>() {
        public Object[] mapRow(WNTFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    Renderer.WARRANT_TYPE.render(e.getWarrantType()),
                    e.getStrike(),
                    e.getQuotedata().getCurrencyIso(),
                    e.getExpirationDate(),
                    e.isIsAmerican() == null ? "n/a" : e.isIsAmerican() ? I18n.I.isAmericanAbbr() : I18n.I.isEuropeanAbbr()  // $NON-NLS-0$
            };
        }
    };

    static final RowMapper<WNTFinderElement> EDG_ROW_MAPPER = new AbstractRowMapper<WNTFinderElement>() {
        public Object[] mapRow(WNTFinderElement e) {
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

    static final RowMapper<WNTFinderElement> PRICE_ROW_MAPPER = new AbstractRowMapper<WNTFinderElement>() {
        public Object[] mapRow(WNTFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getPrice(),
                    e.getBid(),
                    e.getAsk(),
                    e.getChangePercent(),
                    e.getDate(),
                    e.getQuotedata().getMarketName(),
                    e.getIssuername()
            };
        }
    };

    static final RowMapper<WNTFinderElement> PERF_ROW_MAPPER = new AbstractRowMapper<WNTFinderElement>() {
        public Object[] mapRow(WNTFinderElement e) {
            return new Object[]{
                    e.getInstrumentdata().getWkn(),
                    e.getInstrumentdata().getIsin(),
                    createQuoteWithInstrument(e),
                    e.getPerformanceCurrentYear(),
                    e.getPerformance1W(),
                    e.getPerformance1M(),
                    e.getPerformance3M(),
                    e.getPerformance6M(),
                    e.getPerformance1Y(),
                    e.getVolatility1M(),
                    e.getVolatility3M()
            };
        }
    };

    static ViewSpec[] createResultViewSpec() {
        List<ViewSpec> viewSpec = new ArrayList<>();
        viewSpec.add(new ViewSpec(I18n.I.basis()));
        viewSpec.add(new ViewSpec(I18n.I.performanceAndVola()));
        viewSpec.add(new ViewSpec(I18n.I.ratios()));
        viewSpec.add(new ViewSpec(I18n.I.staticData()));
        if (Selector.EDG_RATING.isAllowed()) {
            viewSpec.add(new ViewSpec("EDG")); // $NON-NLS-0$
        }
        return viewSpec.toArray(new ViewSpec[viewSpec.size()]);
    }

    static Map<String, FinderMetaList> getMetaLists(WNTFinderMetadata result) {
        final Map<String, FinderMetaList> map = new HashMap<>();
        map.put("warrantType", null); // $NON-NLS$
        map.put("marketgroups", result.getMarkets()); // $NON-NLS$
        map.put("issuername", Customer.INSTANCE.addCustomerIssuers(result.getIssuername())); // $NON-NLS-0$
        return map;
    }

    private static QuoteWithInstrument createQuoteWithInstrument(WNTFinderElement e) {
        return AbstractFinder.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }
}
