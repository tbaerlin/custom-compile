/*
 * FilterLbbwQuoteStrategy.java
 *
 * Created on 01.04.2015 15:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Arrays;
import java.util.HashSet;

/**
 * A DataRecordStrategy that returns a DataRecord if it is listed on a whitelisted exchange
 * @author jkirchg
 */
public class FilterLbbwQuoteStrategy extends AbstractMarketQuoteStrategy {

    private static final String[] WHITELISTED_MARKETS = {
            "ETR", "EEU", "EUS", "XETF", "FFM", "FFMST", "FFMFO", "STG", "STG2", "EUWAX", "DDF",
            "MCH", "BLN", "EUB", "HBG", "HNV", "DTB", "LBBW", "FONDS", "CITIF", "DBKF", "TUBD",
            "UBSZ", "DZF", "SCGP", "GMF", "FXVWD", "BUBA", "HKI", "STX", "DJ", "HK", "IQ", "NIKKEI",
            "NL", "BL", "UK", "VX", "IT", "Q", "N", "FR", "SW", "CH"};

    protected FilterLbbwQuoteStrategy() {
        super(new HashSet<>(Arrays.asList(WHITELISTED_MARKETS)), null);
    }

    @Override
    public QuoteRatios select(QuoteRatios[] records) {
        return getQuote(records, true);
    }

    @Override
    public Type getType() {
        return Type.FILTER_LBBW_QUOTE;
    }

}
