/*
 * Derivative.java
 *
 * Created on 28.02.2005 18:45:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.Currency;

/**
 * @author Martin Wilke
 */
public interface DerivativeWithStrike extends Derivative {
    BigDecimal getStrike();
    Currency getStrikeCurrency();
    DerivativeTypeEnum getType();
}
