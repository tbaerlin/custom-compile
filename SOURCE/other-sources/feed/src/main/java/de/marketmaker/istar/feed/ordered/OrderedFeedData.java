/*
 * OrderedFeedData.java
 *
 * Created on 02.10.13 16:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;

/**
 * @author oflege
 */
public interface OrderedFeedData extends FeedData {
    int getCreatedTimestamp();

    void setCreatedTimestamp(int ts);

    OrderedSnapData getSnapData(boolean realtime);

    OrderedTickData getOrderedTickData();

    @Override
    default boolean isToBeDisposed() {
        final OrderedTickData td = getOrderedTickData();
        return FeedData.super.isToBeDisposed() && ((td == null) || (td.getLength() == 0));
    }
}
