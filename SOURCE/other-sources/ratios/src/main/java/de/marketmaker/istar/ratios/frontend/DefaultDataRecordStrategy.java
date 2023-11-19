/*
 * DefaultDataRecordChooser.java
 *
 * Created on 09.01.2007 17:38:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import net.jcip.annotations.Immutable;

/**
 * A DataRecordChooser which picks the first record satisfying the given Selector (if any).
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class DefaultDataRecordStrategy implements DataRecordStrategy {

    public QuoteRatios select(QuoteRatios[] records) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < records.length; i++) {
            final QuoteRatios dataRecord = records[i];
            if (!dataRecord.isSelected()) {
                continue;
            }
            final String market = dataRecord.getSymbolVwdfeedMarket();
            if (market != null && market.endsWith("OTC")) {
                continue;
            }
            return dataRecord;
        }

        return firstSelected(records);
    }

    private QuoteRatios firstSelected(QuoteRatios[] records) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < records.length; i++) {
            final QuoteRatios dataRecord = records[i];
            if (dataRecord.isSelected()) {
                return dataRecord;
            }
        }
        return null;
    }

    public QuoteRatios getPreferredMarket(String market, QuoteRatios[] records) {
        return getForMarket(market, true, records);
    }

    public QuoteRatios getWithoutMarket(String market, Selector s, QuoteRatios[] records) {
        return getForMarket(market, false, records);
    }

    public QuoteRatios getForMarket(String market, boolean include, QuoteRatios[] records) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < records.length; i++) {
            if (records[i].isSelected()
                    && include == market.equals(records[i].getSymbolVwdfeedMarket())) {
                return records[i];
            }
        }
        return null;
    }


    public Type getType() {
        return Type.DEFAULT;
    }
}
