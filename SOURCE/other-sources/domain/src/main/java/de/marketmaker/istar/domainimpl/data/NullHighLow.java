/*
 * NullHighLow.java
 *
 * Created on 26.07.2006 16:48:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;

import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.RatioDataRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullHighLow implements Serializable, HighLow{
    protected static final long serialVersionUID = -256545420467633L;

    public static final HighLow INSTANCE = new NullHighLow();

    private NullHighLow() {
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    public String toString() {
        return "NullHighLow[]";
    }

    public HighLow copy(PriceRecord pr) {
        return this;
    }

    public HighLow copy(RatioDataRecord rdr) {
        return this;
    }

    public Interval getInterval() {
        return null;
    }

    public Price getHigh() {
        return NullPrice.INSTANCE;
    }

    public Price getLow() {
        return NullPrice.INSTANCE;
    }
}
