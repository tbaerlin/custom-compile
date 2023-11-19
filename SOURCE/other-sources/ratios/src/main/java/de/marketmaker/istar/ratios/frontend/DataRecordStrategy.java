/*
 * QuoteRecordSelector.java
 *
 * Created on 09.01.2007 17:24:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

/**
 * A strategy for selecting one out of a number of DataRecord elements.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DataRecordStrategy {
    enum Type {
        DEFAULT(DefaultDataRecordStrategy.class),
        PREFER_ISSUER_QUOTE(PreferIssuerQuoteStrategy.class),
        PREFER_FUND_ISSUER_QUOTE(PreferIssuerFundQuoteStrategy.class),
        PREFER_SWISS_QUOTE(PreferSwissQuoteStrategy.class),
        FILTER_SWISS_QUOTE(FilterSwissQuoteStrategy.class),
        PREFER_CON_FUTURES_QUOTE(PreferConFuturesQuoteStrategy.class),
        PREFER_SEDEX_QUOTE(PreferSedexQuoteStrategy.class),
        FILTER_SEDEX_QUOTE(FilterSedexQuoteStrategy.class),
        FILTER_LBBW_QUOTE(FilterLbbwQuoteStrategy.class)
        // TODO: add a DataRecordStrategy to filter by current vwdCode/lmeTradingMonth
        ;

        private final Class<? extends DataRecordStrategy> clazz;

        Type(Class<? extends DataRecordStrategy> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends DataRecordStrategy> getClazz() {
            return this.clazz;
        }
    }

    /**
     * Select one out of a number of DataRecord elements.
     * <h2>Implementation Notes</h2>
     * a strategy might be invoked a million times for a single search, so performance is
     * critical
     * <ul>
     * <li>iterating over records with
     * <tt>for(int i = 0; i < records.length; i++)</tt> is faster than using
     * <tt>for(QuoteRatios qr: records)</tt></li>
     * <li>Searching and applying the strategy will be done using multiple threads, so the
     * strategy has to be thread-safe</li>
     * <li>a strategy should not create any new objects</li>
     * <li>the first action of the strategy for each record should be to ensure that its
     * {@link de.marketmaker.istar.ratios.frontend.QuoteRatios#isSelected()} method returns true,
     * as the array may contain unselected records as well. A previous version of the searcher
     * would eliminate those records before passing them to the strategy, but copying a million
     * arrays is not very smart.
     * </li>
     * </ul>
     *
     * @param records to select from, sorted by update time with most recently updated record
     * at position 0; will always contain at least one element for which
     * {@link de.marketmaker.istar.ratios.frontend.QuoteRatios#isSelected()} returns true
     * @return selected record or null if no appropriate record could be found
     */
    QuoteRatios select(QuoteRatios[] records);

    Type getType();
}
