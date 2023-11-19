/*
 * QuoteCategorizerImpl.java
 *
 * Created on 12.05.2010 13:48:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.InstrumentUtil;

/**
 * @author zzhao
 */
public class QuoteCategorizerImpl implements QuoteCategorizer {
    /**
     * Categorizes quote according to its VWD feed market.
     * <dl>
     * <dt>FUND_MARKET</dt><dd>{@link QuoteCategory#FUND_MARKET}</dd>
     * <dt>Others</dt><dd>{@link QuoteCategory#COMMON}</dd>
     * </dl>
     * @param quote a quote
     * @return a quote category
     */
    public QuoteCategory categorize(Quote quote) {
        return categorize(quote.getSymbolVwdfeedMarket());
    }

    /**
     * Categorizes quote according to VWD feed symbol.
     * <dl>
     * <dt>FUND_MARKET</dt><dd>{@link QuoteCategory#FUND_MARKET}</dd>
     * <dt>Others</dt><dd>{@link QuoteCategory#COMMON}</dd>
     * </dl>
     * @param vwdfeedsymbol a symbol denotes VWD feed symbol
     * @return a quote category
     */
    public QuoteCategory categorize(String vwdfeedsymbol) {
        if (InstrumentUtil.isVwdFundFeedsymbol(vwdfeedsymbol)) {
            return QuoteCategory.FUND_MARKET;
        }

        return QuoteCategory.COMMON;
    }
}
