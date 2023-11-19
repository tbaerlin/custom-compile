/*
 * MarketDay.java
 *
 * Created on 23.11.12 16:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import de.marketmaker.istar.common.util.ByteString;

/**
* @author oflege
*/
class MarketDay {
    static MarketDay create(ByteString market, int day) {
        // todo: cache?
        return new MarketDay(market, day);
    }

    final ByteString market;

    final int day;

    private MarketDay(ByteString market, int day) {
        this.market = market;
        this.day = day;
    }

    @Override
    public String toString() {
        return this.market + "-" + this.day;
    }

    @Override
    public int hashCode() {
        return this.market.hashCode() * 31 + this.day;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarketDay o = (MarketDay) obj;
        return this.day == o.day && this.market.equals(o.market);
    }
}
