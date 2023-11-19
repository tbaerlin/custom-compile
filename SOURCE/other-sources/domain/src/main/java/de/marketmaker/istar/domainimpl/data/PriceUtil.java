/*
 * PriceUtil.java
 *
 * Created on 07.05.2010 14:50:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;

/**
 * @author oflege
 */
public final class PriceUtil {
    public static Price multiply(Price p, BigDecimal factor) {
        if (isMultiplicationNecessary(p, factor)) {
            return new PriceImpl(p.getValue().multiply(factor), p.getVolume(), p.getSupplement(),
                    p.getDate(), getPriceQuality(p));
        }
        return p;
    }

    public static Price multiply(Price p, BigDecimal factor, int scale, RoundingMode roundingMode) {
        if (isMultiplicationNecessary(p, factor)) {
            return new PriceImpl(p.getValue().multiply(factor).setScale(scale, roundingMode), p.getVolume(), p.getSupplement(),
                    p.getDate(), getPriceQuality(p));
        }
        return p;
    }

    private static boolean isMultiplicationNecessary(Price p, BigDecimal factor) {
        return p != null && p.getValue() != null
                && p != NullPrice.INSTANCE && p != ZeroPrice.INSTANCE
                && !BigDecimal.ONE.equals(factor) && !BigDecimal.ZERO.equals(p.getValue());
    }

    private static PriceQuality getPriceQuality(Price p) {
        if (p.isRealtime()) {
            return PriceQuality.REALTIME;
        }
        if (p.isDelayed()) {
            return PriceQuality.DELAYED;
        }
        if (p.isEndOfDay()) {
            return PriceQuality.END_OF_DAY;
        }
        return PriceQuality.NONE;
    }

    private PriceUtil() { // avoid instantiation
    }
}
