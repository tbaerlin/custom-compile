/*
 * TickEventTimeseriesSerializer.java
 *
 * Created on 26.11.2009 20:18:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.protobuf.Descriptors;

import de.marketmaker.istar.domain.data.BidAskTradeTickImpl;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickEventTimeseriesSerializer extends AbstractProtobufSerializer {

    private static final Descriptors.Descriptor DESCRIPTOR
            = TimeseriesProtos.TickEvent.getDescriptor();

    private final boolean withTrade;

    private final ProtobufDateTimeState time;

    private final ProtobufPriceState price = new ProtobufPriceState(DESCRIPTOR, "price");

    private final ProtobufPriceState ask = new ProtobufPriceState(DESCRIPTOR, "ask_price");

    private final ProtobufPriceState bid = new ProtobufPriceState(DESCRIPTOR, "bid_price");

    public TickEventTimeseriesSerializer() {
        this(false, true);
    }

    public TickEventTimeseriesSerializer(boolean useMillis, boolean withTrade) {
        this.time = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
        this.withTrade = withTrade;
    }

    public ByteBuffer serialize(Iterable<TickImpl> ticks) throws IOException {
        for (final TickImpl tick : ticks) {
            final BidAskTradeTickImpl t = (BidAskTradeTickImpl) tick;
            final TimeseriesProtos.TickEvent.Builder element
                    = TimeseriesProtos.TickEvent.newBuilder();

            boolean anyTick = false;

            if (this.withTrade && t.hasVolume()) {
                element.setVolume(t.volume());
                anyTick = true;
            }
            
            if (t.hasBidVolume()) {
                element.setBidVolume(t.bidVolume());
                anyTick = true;
            }

            if (t.hasAskVolume()) {
                element.setAskVolume(t.askVolume());
                anyTick = true;
            }

            anyTick |= (this.withTrade && this.price.update(element, t.getPrice()));
            anyTick |= this.ask.update(element, t.getAskPrice());
            anyTick |= this.bid.update(element, t.getBidPrice());

            if (!anyTick) {
                continue;
            }

            if (this.withTrade) {
                if (tick.getSupplement() != null) {
                    element.setSupplement(tick.getSupplement());
                }
                if (tick.getTradeIdentifier() != null) {
                    element.setTradeIdentifier(tick.getTradeIdentifier());
                }
            }
            this.time.update(element, tick.getDateTime());

            if (t.hasFields()) {
                for (SnapField sf : t.getFields()) {
                    element.addFields(SerializationUtil.serialize(sf, this.time.isUseMillis()));
                }
            }

            addObject(element);
        }

        return getResult();
    }
}
