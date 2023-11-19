/*
 * NullBondRatios.java
 *
 * Created on 28.07.2006 07:42:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullWarrantRatios implements WarrantRatios, Serializable {
    protected static final long serialVersionUID = 1L;

    public static final WarrantRatios INSTANCE = new NullWarrantRatios();

    private NullWarrantRatios() {
    }

    public BigDecimal getLeverage() {
        return null;
    }

    public BigDecimal getContango() {
        return null;
    }

    public BigDecimal getContangoPerYear() {
        return null;
    }

    public BigDecimal getIntrinsicValue() {
        return null;
    }

    public BigDecimal getExtrinsicValue() {
        return null;
    }

    public BigDecimal getBreakeven() {
        return null;
    }

    public BigDecimal getImpliedVolatility() {
        return null;
    }

    public BigDecimal getDelta() {
        return null;
    }

    public BigDecimal getFairPrice() {
        return null;
    }

    public BigDecimal getParity() {
        return null;
    }

    public BigDecimal getOmega() {
        return null;
    }

    public BigDecimal getGamma() {
        return null;
    }

    public BigDecimal getVega() {
        return null;
    }

    public BigDecimal getRho() {
        return null;
    }

    public BigDecimal getMoneyness() {
        return null;
    }

    public BigDecimal getMoneynessRelative() {
        return null;
    }

    public BigDecimal getTheta() {
        return null;
    }

    public BigDecimal getThetaRelative() {
        return null;
    }

    public BigDecimal getTheta1w() {
        return null;
    }

    public BigDecimal getTheta1wRelative() {
        return null;
    }

    public BigDecimal getTheta1m() {
        return null;
    }

    public BigDecimal getTheta1mRelative() {
        return null; 
    }

    public String toString() {
        return "NullWarrantRatios[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}
