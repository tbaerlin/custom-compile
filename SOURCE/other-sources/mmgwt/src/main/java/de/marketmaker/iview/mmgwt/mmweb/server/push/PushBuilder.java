/*
 * PushBuilder.java
 *
 * Created on 10.02.2010 07:14:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import net.jcip.annotations.ThreadSafe;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedBuilder;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

/**
 * FeedBuilder that informs its Registrations delegate about feed updates for certain keys
 * @author oflege
 */
@ThreadSafe
public class PushBuilder implements FeedBuilder, OrderedUpdateBuilder {
    private Registrations registrations;

    public void setRegistrations(Registrations registrations) {
        this.registrations = registrations;
    }

    public byte[] getApplicableMessageTypes() {
        return VwdFeedConstants.getXfeedDynamicAndStatic();
    }

    @Override
    public void process(OrderedFeedData feedData, OrderedUpdate update) {
        this.registrations.ackUpdateFor(feedData.getVwdcode(), update);
    }

    public void process(FeedData feedData, ParsedRecord pr) {
        this.registrations.ackUpdateFor(feedData.getVwdcode(), pr);
    }
}
