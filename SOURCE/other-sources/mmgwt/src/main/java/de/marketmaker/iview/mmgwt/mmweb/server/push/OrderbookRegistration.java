/*
 * Registration.java
 *
 * Created on 10.02.2010 07:00:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.BitSet;
import java.util.Collection;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushChangeRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushOrderbook;

/**
 * @author oflege
 */
class OrderbookRegistration extends Registration<PushOrderbook> {

    OrderbookRegistration(Quote q) {
        super(q);
    }

    @Override
    public String getRegistrationKey() {
        return PushChangeRequest.ORDERBOOK_PREFIX + super.getRegistrationKey();
    }

    @Override
    protected PushOrderbook createComplete(SnapRecord sr) {
        final PushOrderbook result = PushOrderbookFactory.create(sr);
        result.setVwdCode(this.symbolVwdcode);
        return result;
    }

    @Override
    protected PushOrderbook createDiff(PushOrderbook current, PushOrderbook previous,
            BitSet allowedFields) {
        return PushOrderbookFactory.createDiff(current, previous);
    }

    @Override
    protected boolean isFreshUpdate(OrderedUpdate update) {
        return super.isFreshUpdate();
    }

    @Override
    protected boolean isFreshUpdate(ParsedRecord pr) {
        return super.isFreshUpdate();
    }

    protected void push(Collection<AbstractClient> clients, PushOrderbook orderbook) {
        for (AbstractClient client : clients) {
            client.push(orderbook);
        }
    }
}