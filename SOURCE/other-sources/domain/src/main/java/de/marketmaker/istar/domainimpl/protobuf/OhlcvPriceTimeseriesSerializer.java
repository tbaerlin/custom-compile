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
import de.marketmaker.istar.domain.data.AggregatedValue;
import de.marketmaker.istar.domain.data.SnapField;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OhlcvPriceTimeseriesSerializer extends AbstractProtobufSerializer
        implements AggregatedTickSerializer {
    private static final Descriptors.Descriptor DESCRIPTOR
            = TimeseriesProtos.Ohlcv.getDescriptor();

    private final ProtobufDateTimeState dateTime;

    private final ProtobufPriceState open = new ProtobufPriceState(DESCRIPTOR, "open");

    private final ProtobufPriceState high = new ProtobufPriceState(DESCRIPTOR, "high");

    private final ProtobufPriceState low = new ProtobufPriceState(DESCRIPTOR, "low");

    private final ProtobufPriceState close = new ProtobufPriceState(DESCRIPTOR, "close");

    public OhlcvPriceTimeseriesSerializer() {
        this(false);
    }

    public OhlcvPriceTimeseriesSerializer(boolean useMillis) {
        this.dateTime = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
    }

    @Override
    public ByteBuffer serialize(List<AggregatedTickImpl> ticks) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ByteBuffer serializeValues(List<AggregatedValue> ticks) throws IOException {
        for (final AggregatedValue tick : ticks) {
            final TimeseriesProtos.Ohlcv.Builder element = TimeseriesProtos.Ohlcv.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());

            this.open.update(element, tick.getOpen());
            this.high.update(element, tick.getHigh());
            this.low.update(element, tick.getLow());
            this.close.update(element, tick.getClose());

            if (null != tick.getVolume()) {
                element.setVolume(tick.getVolume().longValue());
            }

            if (null != tick.getContract()) {
                element.setCount(tick.getContract().intValue());
            }

            if (null != tick.getAdditionalFields()) {
                for (SnapField snapField : tick.getAdditionalFields()) {
                    element.addFields(SerializationUtil.serialize(snapField, false));
                }
            }

            addObject(element);
        }

        return getResult();
    }
}