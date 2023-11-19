/*
 * OrderedDelayer.java
 *
 * Created on 04.10.12 11:25
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.nio.ByteOrder;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.delay.Delayer;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
public class OrderedDelayer extends Delayer implements OrderedUpdateBuilder {
    public OrderedDelayer() {
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        final int delayInSeconds = getDelayInSeconds(data, update);
        if (delayInSeconds < 0) {
            return;
        }
        append(delayInSeconds, update.asMessageWithLength());
    }

    private int getDelayInSeconds(FeedData data, OrderedUpdate update) {
        final int nominalDelay = (this.delayProvider != null)
                ? this.delayProvider.getDelayInSeconds(data)
                : update.getDelayInSeconds();
        if (this.ignoreMarketTime || nominalDelay <= 0) {
            return nominalDelay;
        }
        return adjustDelayForMarketTime(nominalDelay, getMarketTime(update));
    }

    private int getMarketTime(OrderedUpdate update) {
        final FieldData fd = update.getFieldData();
        int result = -1;
        NEXT: for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
            switch (id) {
                case VwdFieldOrder.ORDER_ADF_ZEIT:
                case VwdFieldOrder.ORDER_ADF_BOERSENZEIT:
                    result = fd.getInt();
                    break;
                default:
                    break NEXT;
            }
        }
        return result < 0 ? update.getTime() : MdpsFeedUtils.toSecondOfDay(result);
    }
}
