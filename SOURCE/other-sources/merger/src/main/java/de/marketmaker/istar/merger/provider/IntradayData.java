/*
 * IntradayData.java
 *
 * Created on 15.09.2006 13:14:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;

/**
 * Contains intraday data for a certain quote.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class IntradayData implements Serializable {
    protected static final long serialVersionUID = 1L;

    public static final IntradayData NULL
            = new IntradayData(0, NullPriceRecord.INSTANCE, NullSnapRecord.INSTANCE, null);

    private final long qid;

    private final TickRecord ticks;

    private final SnapRecord priceSnap;

    private final PriceRecord price;

    IntradayData(long qid, PriceRecord price, SnapRecord priceSnap,
            TickRecord ticks) {
        this.qid = qid;
        this.price = price != null ? price : NullPriceRecord.INSTANCE;
        this.priceSnap = priceSnap != null ? priceSnap : NullSnapRecord.INSTANCE;
        this.ticks = ticks;
    }

    public PriceRecord getPrice() {
        return this.price;
    }

    public SnapRecord getSnap() {
        return this.priceSnap;
    }

    public TickRecord getTicks() {
        return this.ticks;
    }

    public long getQid() {
        return qid;
    }
}
