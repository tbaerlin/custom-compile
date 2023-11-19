/*
 * PreferIssuerFundQuoteStrategy.java
 *
 * Created on 09.01.2007 18:12:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;

/**
 * A DataRecordChooser that prefers a selectable DataRecord listed on a swiss exchange;
 * if no such record exists, it will also consider non-swiss records.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class PreferSwissQuoteStrategy extends AbstractMarketQuoteStrategy {
    public PreferSwissQuoteStrategy() {
        super(new HashSet<>(Arrays.asList("CH", "VX")), Collections.singleton("CH"));
    }

    public Type getType() {
        return Type.PREFER_SWISS_QUOTE;
    }
}