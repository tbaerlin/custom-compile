/*
 * AbstractMarketQuoteStrategy.java
 *
 * Created on 6/16/14 10:18 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domainimpl.instrument.EntitlementQuote;

import java.util.Set;

/**
 * @author Stefan Willenbrock
 */
public abstract class AbstractMarketQuoteStrategy extends DefaultDataRecordStrategy {

    private final Set<String> markets;

    private final Set<String> countries;

    protected AbstractMarketQuoteStrategy(Set<String> markets, Set<String> countries) {
        this.markets = markets;
        this.countries = countries;
    }

    public QuoteRatios select(QuoteRatios[] records) {
        final QuoteRatios result = getQuote(records);
        return (result != null) ? result : super.select(records);
    }

    protected QuoteRatios getQuote(QuoteRatios[] records) {
        return getQuote(records, false);
    }

    protected QuoteRatios getQuote(QuoteRatios[] records, boolean requireMarkets) {
        final QuoteRatios resultForMarkets = getQuote(records, MarketcategoryEnum.BOERSE, false);
        if (resultForMarkets != null || requireMarkets) {
            return resultForMarkets;
        }
        final QuoteRatios result = getQuote(records, MarketcategoryEnum.BOERSE, true);
        return (result != null) ? result : getQuote(records, null, true);
    }

    private QuoteRatios getQuote(QuoteRatios[] records,
                                 final MarketcategoryEnum marketCategory, boolean allowAllMarkets) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < records.length; i++) {
            QuoteRatios ratios = records[i];
            if (ratios.isSelected() && isHit(ratios, marketCategory, allowAllMarkets)) {
                return ratios;
            }
        }
        return null;
    }

    private boolean isHit(QuoteRatios ratios,
            final MarketcategoryEnum marketCategory, boolean allowAllMarkets) {
        final EntitlementQuote eq = ratios.getEntitlementQuote();
        return (this.countries == null || this.countries.contains(eq.getMarketCountrySymbolIso()))
                && (marketCategory == null || eq.getMarketcategory() == marketCategory)
                && (allowAllMarkets || this.markets.contains(ratios.getSymbolVwdfeedMarket()));
    }
}
