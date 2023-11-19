/*
 * Bond.java
 *
 * Created on 17.12.2004 11:48:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import de.marketmaker.istar.domain.Currency;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CurrencyCrossrate extends Instrument {

    Currency getSourceCurrency();

    Currency getTargetCurrency();

    double getSourceToTargetFactor();
}
