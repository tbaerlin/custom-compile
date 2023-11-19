/*
 * PreferIssuerFundQuoteStrategy.java
 *
 * Created on 09.01.2007 18:12:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * A DataRecordChooser that first tries to find a selectable DataRecord, which is
 * the standard CON-Future; if it cannot find one, it uses the default selection
 * strategy from its superclass.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class PreferConFuturesQuoteStrategy extends DefaultDataRecordStrategy {
    public QuoteRatios select(QuoteRatios[] records) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < records.length; i++) {
            QuoteRatios dataRecord = records[i];
            if (!dataRecord.isSelected()) {
                continue;
            }
            final String vwdcode = dataRecord.getString(RatioFieldDescription.vwdCode.id());
            if (vwdcode != null && vwdcode.endsWith(".CON")) {
                return dataRecord;
            }
        }

        return super.select(records);
    }

    public Type getType() {
        return Type.PREFER_CON_FUTURES_QUOTE;
    }
}