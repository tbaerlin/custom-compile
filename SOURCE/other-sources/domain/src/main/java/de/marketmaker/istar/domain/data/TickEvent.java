/*
 * TickEvent.java
 *
 * Created on 14.12.2005 13:16:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a tick event as received in the data feed. In contrast to a Tick, a Tick event can
 * represent a bid, an ask and a trade at the same time. The main reason to refer to TickEvents
 * is that they preserve the order of trades, bids, and asks, even if they occur in the same
 * second. If you would iterate over the different tick types separately, there would be no
 * way of determining whether an ask was received before, with, or after a bid with the same
 * timestamp.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TickEvent extends TickProperties {
    boolean isPricePresent();

    boolean isBidPresent();

    boolean isAskPresent();

    boolean isTrade();

    boolean isBid();

    boolean isAsk();

    boolean isSuspendStart();

    boolean isSuspendEnd();

    int getTime();

    long getPrice();

    long getVolume();

    boolean isVolumePresent();

    boolean isAskVolumePresent();

    boolean isBidVolumePresent();

    String getSupplement();

    long getBidPrice();

    long getBidVolume();

    long getAskPrice();

    long getAskVolume();

    String getTradeIdentifier();

    List<SnapField> getAdditionalFields();
}
