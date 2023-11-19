/*
 * NullPrice.java
 *
 * Created on 10.07.2006 12:58:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.Price;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullPrice implements Price, Serializable {
    protected static final long serialVersionUID = -1L;

    public static final Price INSTANCE = new NullPrice();

    private NullPrice() {
    }

    public String toString() {
        return "NullPrice[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }    

    public boolean isDefined() {
        return false;
    }

    public DateTime getDate() {
        return null;
    }

    public BigDecimal getValue() {
        return null;
    }

    public Long getVolume() {
        return null;
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
