/*
 * PreferIssuerFundQuoteStrategy.java
 *
 * Created on 09.01.2007 18:12:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.MarketcategoryEnum;

/**
 * A DataRecordChooser that first tries to find a selectable DataRecord, which is
 * not exchange-based; if it cannot find one, it uses the default selection
 * strategy from its superclass.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class PreferIssuerQuoteStrategy extends PreferIssuerFundQuoteStrategy {
    public QuoteRatios select(QuoteRatios[] records) {
        if (!(records[0] instanceof QuoteRatiosFND)) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < records.length; i++) {
                QuoteRatios dataRecord = records[i];
                if (dataRecord.isSelected() && dataRecord.getEntitlementQuote().getMarketcategory()
                        != MarketcategoryEnum.BOERSE) {
                    return dataRecord;
                }
            }
        }

        return super.select(records);
    }

    public Type getType() {
        return Type.PREFER_ISSUER_QUOTE;
    }
}