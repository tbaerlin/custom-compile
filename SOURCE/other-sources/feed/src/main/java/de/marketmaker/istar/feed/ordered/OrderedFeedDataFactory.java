/*
 * OrderedFeedDataFactory.java
 *
 * Created on 06.11.12 16:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataFactory;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;

/**
 * @author oflege
 */
public class OrderedFeedDataFactory implements FeedDataFactory {
    public static final OrderedFeedDataFactory RT
            = new OrderedFeedDataFactory(false, false, false);

    public static final OrderedFeedDataFactory RT_NT
            = new OrderedFeedDataFactory(true, false, false);

    public static final OrderedFeedDataFactory RT_UNSAFE
            = new OrderedFeedDataFactory(false, false, true);

    public static final OrderedFeedDataFactory RT_NT_UNSAFE
            = new OrderedFeedDataFactory(true, false, true);

    public static final OrderedFeedDataFactory RT_TICKS
            = new OrderedFeedDataFactory(false, true, false);

    public static final OrderedFeedDataFactory RT_NT_TICKS
            = new OrderedFeedDataFactory(true, true, false);

    public static final OrderedFeedDataFactory RT_UNSAFE_TICKS
            = new OrderedFeedDataFactory(false, true, true);

    public static final OrderedFeedDataFactory RT_NT_UNSAFE_TICKS
            = new OrderedFeedDataFactory(true, true, true);

    private final boolean withNtSnap;

    private final boolean withTickData;

    private final boolean unsafeSnap;

    private OrderedFeedDataFactory(boolean withNtSnap, boolean withTickData, boolean unsafeSnap) {
        this.withNtSnap = withNtSnap;
        this.withTickData = withTickData;
        this.unsafeSnap = unsafeSnap;
    }

    @Override
    public OrderedFeedData create(Vendorkey vendorkey, FeedMarket market) {
        final OrderedFeedData result = new OrderedFeedDataImpl(vendorkey, market,
                createSnap(), createNtSnap(), createTickData());
        result.setState(FeedData.STATE_NEW);
        return result;
    }

    private OrderedTickData createTickData() {
        return withTickData ? new OrderedTickData() : null;
    }

    private OrderedSnapData createNtSnap() {
        return this.withNtSnap ? createSnap() : null;
    }

    private OrderedSnapData createSnap() {
        return this.unsafeSnap ? new UnsafeSnapData() : new OrderedSnapDataImpl();
    }
}
