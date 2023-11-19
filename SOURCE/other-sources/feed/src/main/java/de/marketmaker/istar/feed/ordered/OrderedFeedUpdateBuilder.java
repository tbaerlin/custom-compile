/*
 * OrderedFeedUpdateBuilder.java
 *
 * Created on 23.10.12 09:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;

/**
 * Forwards <code>OrderedUpdate</code>s to delegate {@link OrderedUpdateBuilder}s.
 * @author oflege
 */
public class OrderedFeedUpdateBuilder extends OrderedFeedBuilder {
    private final OrderedUpdate update = new OrderedUpdate();

    private OrderedUpdateBuilder[] builders = new OrderedUpdateBuilder[0];

    public void setBuilders(OrderedUpdateBuilder[] builders) {
        this.builders = builders;
    }

    @Override
    public void process(FeedData data, ParsedRecord pr) {
        doProcess(data, pr);
        this.bb.getShort();
        this.update.reset(this.bb);

        OrderedFeedData ofd = (OrderedFeedData) data;
        for (OrderedUpdateBuilder builder : this.builders) {
            builder.process(ofd, this.update);
        }
    }
}
