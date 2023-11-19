/*
 * PreferIssuerFundQuoteStrategy.java
 *
 * Created on 09.01.2007 18:12:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import net.jcip.annotations.Immutable;

/**
 * A DataRecordChooser that first tries to find a selectable DataRecord with
 * vwd feed market symbol "FONDS"; if it cannot find one, it uses the default selection
 * strategy from its superclass.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class PreferIssuerFundQuoteStrategy extends DefaultDataRecordStrategy {
    public QuoteRatios select(QuoteRatios[] records) {
        final QuoteRatios qr = getPreferredMarket("FONDS", records);
        if (qr != null) {
            return qr;
        }

        final QuoteRatios qr2 = getPreferredMarket("FONDNL", records);
        if (qr2 != null) {
            return qr2;
        }

        final QuoteRatios qr3 = getPreferredMarket("FONDIT", records);
        if (qr3 != null) {
            return qr3;
        }

        final QuoteRatios qr4 = getPreferredMarket("FONITI", records);
        if (qr4 != null) {
            return qr4;
        }

        return super.select(records);
    }

    public Type getType() {
        return Type.PREFER_FUND_ISSUER_QUOTE;
    }
}
