/*
 * OhlcvTimeseriesSerializer.java
 *
 * Created on 26.11.2009 20:18:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.google.protobuf.Descriptors;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OhlcvTimeseriesSerializer extends AbstractProtobufSerializer {
    private static final Descriptors.Descriptor DESCRIPTOR
            = TimeseriesProtos.Ohlcv.getDescriptor();

    private final ProtobufDateTimeState dateTime;

    private final ProtobufPriceState open = new ProtobufPriceState(DESCRIPTOR, "open");

    private final ProtobufPriceState high = new ProtobufPriceState(DESCRIPTOR, "high");

    private final ProtobufPriceState low = new ProtobufPriceState(DESCRIPTOR, "low");

    private final ProtobufPriceState close = new ProtobufPriceState(DESCRIPTOR, "close");

    public OhlcvTimeseriesSerializer() {
        this(false);
    }

    public OhlcvTimeseriesSerializer(boolean useMillis) {
        this.dateTime = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
    }

    public ByteBuffer serialize(List<AggregatedTickImpl> ticks, boolean withOhl,
            boolean withVolume) throws IOException {

        for (final AggregatedTickImpl tick : ticks) {
            final TimeseriesProtos.Ohlcv.Builder element = TimeseriesProtos.Ohlcv.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());

            if (withOhl) {
                this.open.update(element, tick.getOpen());
                this.high.update(element, tick.getHigh());
                this.low.update(element, tick.getLow());
            }
            this.close.update(element, tick.getClose());

            if (withVolume) {
                final Long v = tick.getVolume();
                if (v != null) {
                    element.setVolume(v);
                }
            }
            element.setCount(tick.getNumberOfAggregatedTicks());

            addObject(element);
        }

        return getResult();
    }
}