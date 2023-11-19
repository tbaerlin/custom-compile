/*
 * BidAskTradeTickImpl.java
 *
 * Created on 11.06.2010 10:38:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

/**
 * @author tkiesgen
 */
public class BidAskTradeTickImpl extends TickImpl {
    private final BigDecimal bidPrice;
    private final long bidVolume;
    private final BigDecimal askPrice;
    private final long askVolume;

    public BidAskTradeTickImpl(DateTime dateTime, BigDecimal price, long volume, String supplement,
            String tradeIdentifier, TickImpl.Type type, BigDecimal bidPrice, long bidVolume,
            BigDecimal askPrice, long askVolume) {
        this(dateTime, price, volume, supplement, tradeIdentifier, type, bidPrice, bidVolume, askPrice, askVolume, null);
    }

    public BidAskTradeTickImpl(DateTime dateTime, BigDecimal price, long volume, String supplement,
            String tradeIdentifier, TickImpl.Type type, BigDecimal bidPrice, long bidVolume,
            BigDecimal askPrice, long askVolume, List<SnapField> fields) {
        super(dateTime, price, volume, supplement, tradeIdentifier, type, fields);
        this.bidPrice = bidPrice;
        this.bidVolume = bidVolume;
        this.askPrice = askPrice;
        this.askVolume = askVolume;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public Long getBidVolume() {
        if(bidVolume == Long.MIN_VALUE) {
            return null;
        }
        return bidVolume;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public Long getAskVolume() {
        if(askVolume == Long.MIN_VALUE) {
            return null;
        }
        return askVolume;
    }
    
    public boolean hasAskVolume() {
        return this.askVolume != Long.MIN_VALUE;
    }

    public long askVolume() {
        return this.askVolume;
    }

    public boolean hasBidVolume() {
        return this.bidVolume != Long.MIN_VALUE;
    }

    public long bidVolume() {
        return this.bidVolume;
    }
}
