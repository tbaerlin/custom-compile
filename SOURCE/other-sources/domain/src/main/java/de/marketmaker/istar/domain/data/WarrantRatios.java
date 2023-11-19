/*
 * BondRatios.java
 *
 * Created on 16.07.2006 21:38:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface WarrantRatios {
    BigDecimal getLeverage();

    BigDecimal getContango();

    BigDecimal getContangoPerYear();

    BigDecimal getIntrinsicValue();

    BigDecimal getExtrinsicValue();

    BigDecimal getBreakeven();

    BigDecimal getImpliedVolatility();

    BigDecimal getDelta();

    BigDecimal getFairPrice();

    BigDecimal getParity();

    BigDecimal getOmega();

    BigDecimal getGamma();

    BigDecimal getVega();

    BigDecimal getRho();

    BigDecimal getMoneyness();

    BigDecimal getMoneynessRelative();

    BigDecimal getTheta();

    BigDecimal getThetaRelative();

    BigDecimal getTheta1w();

    BigDecimal getTheta1wRelative();

    BigDecimal getTheta1m();

    BigDecimal getTheta1mRelative();
}
