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
public class NullBondRatios implements BondRatios, Serializable {
    protected static final long serialVersionUID = -2565453L;

    public static final BondRatios INSTANCE = new NullBondRatios();

    private NullBondRatios() {
    }

    public BigDecimal getYield() {
        return null;
    }

    public BigDecimal getBrokenPeriodInterest() {
        return null;
    }

    public BigDecimal getDuration() {
        return null;
    }

    public BigDecimal getConvexity() {
        return null;
    }

    public BigDecimal getInterestRateElasticity() {
        return null;
    }

    public BigDecimal getBasePointValue() {
        return null;
    }

    public BigDecimal getBuyingPrice() {
        return null;
    }

    public BigDecimal getBuyingYield() {
        return null;
    }

    public BigDecimal getSellingPrice() {
        return null;
    }

    public BigDecimal getSellingYield() {
        return null;
    }

    public String toString() {
        return "NullBondRatios[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}
