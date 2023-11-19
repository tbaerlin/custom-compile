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
 * A DataRecordChooser only finds a selectable DataRecord, which is
 * is listed on a swiss exchange.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class FilterSwissQuoteStrategy extends PreferSwissQuoteStrategy {
    public QuoteRatios select(QuoteRatios[] records) {
        return getQuote(records);
    }

    public Type getType() {
        return Type.FILTER_SWISS_QUOTE;
    }
}