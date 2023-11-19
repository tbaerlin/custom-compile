/*
 * SyntheticTradeAggregator.java
 *
 * Created on 10.04.13 11:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.math.BigDecimal;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.SyntheticTickBuilder;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author oflege
 */
public class SyntheticTradeAggregator extends RawTickAggregator {

    private final SyntheticTickBuilder builder = new SyntheticTickBuilder();

    private int currentTime = Integer.MIN_VALUE;

    public SyntheticTradeAggregator(AbstractTickRecord.TickItem item,
            DateTime lastTickDateTime,
            int aggregationIntervalInSeconds,
            boolean aggregateOnlyPositivePrices) {
        super(item, lastTickDateTime, aggregationIntervalInSeconds, TickType.SYNTHETIC_TRADE, aggregateOnlyPositivePrices);
    }

    @Override
    public boolean process(RawTick tick) {
        if (tick.getTime() < currentTime) {
            return true; // ignore out of order ticks
        }
        currentTime = tick.getTime();
        if (checkLast && tick.getTime() > endSec) {
            return false;
        }
        boolean usable = false;
        if (tick.isAsk() && tick.getAskPrice() > 0) {
            builder.updateAsk(PriceCoder.decode(tick.getAskPrice()));
            usable = true;
        }
        if (tick.isBid() && tick.getBidPrice() > 0) {
            builder.updateBid(PriceCoder.decode(tick.getBidPrice()));
            usable = true;
        }

        if (usable) {
            addTick(currentTime, getPrice(), 0L);
        }
        return true;
    }

    private long getPrice() {
        final BigDecimal bd = this.builder.getSyntheticPrice();
        return PriceCoder.encode(bd.unscaledValue().longValue(), -bd.scale());
    }
}
