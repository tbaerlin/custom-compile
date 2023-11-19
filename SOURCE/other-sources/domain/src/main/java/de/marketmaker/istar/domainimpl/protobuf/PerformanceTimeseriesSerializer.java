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
public class PerformanceTimeseriesSerializer extends AbstractProtobufSerializer
        implements AggregatedTickSerializer {
    private static final Descriptors.Descriptor DESCRIPTOR
            = TimeseriesProtos.Performance.getDescriptor();

    private final ProtobufDateTimeState dateTime;

    private final ProtobufPriceState percent = new ProtobufPriceState(DESCRIPTOR, "percent");

    public PerformanceTimeseriesSerializer() {
        this(false);
    }

    public PerformanceTimeseriesSerializer(boolean useMillis) {
        dateTime = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
    }

    public ByteBuffer serialize(List<AggregatedTickImpl> ticks) throws IOException {
        for (final AggregatedTickImpl tick : ticks) {
            final TimeseriesProtos.Performance.Builder element
                    = TimeseriesProtos.Performance.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());
            this.percent.update(element, tick.getClose());

            addObject(element);
        }

        return getResult();
    }

    @Override
    public ByteBuffer serializeValues(List<AggregatedValue> ticks) throws IOException {
        for (final AggregatedValue tick : ticks) {
            final TimeseriesProtos.Performance.Builder element
                    = TimeseriesProtos.Performance.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());
            this.percent.update(element, tick.getClose());

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