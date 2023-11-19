/*
 * ZeroPrice.java
 *
 * Created on 12.12.2006 10:11:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.Price;

/**
 * An undefined price with a value of zero.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ZeroPrice implements Price, Serializable {
    protected static final long serialVersionUID = -1L;

    public static final Price INSTANCE = new ZeroPrice();

    private ZeroPrice() {
    }

    public String toString() {
        return "ZeroPrice[]";
    }

    public boolean isDefined() {
        return false;
    }

    public DateTime getDate() {
        return null;
    }

    public BigDecimal getValue() {
        return BigDecimal.ZERO;
    }

    public Long getVolume() {
        return 0L;
    }

    public String getSupplement() {
        return null;
    }

    public boolean isRealtime() {
        return false;
    }

    public boolean isDelayed() {
        return false;
    }

    public boolean isEndOfDay() {
        return false;
    }
}
