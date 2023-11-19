/*
 * TickTimeseriesSerializer.java
 *
 * Created on 26.11.2009 20:18:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.protobuf.Descriptors;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 * 710000.ETR, 20091127
 * ticks.length = 606327
 * # objects    = 9555 (trades)
 * serialized bytes = 65776
 * compressed ser. bytes in base64 = 41192
 *
 */
public class TickTimeseriesSerializer extends AbstractProtobufSerializer {
    private static final Descriptors.Descriptor DESCRIPTOR
            = TimeseriesProtos.Tick.getDescriptor();

    private final ProtobufDateTimeState dateTime;

    private final ProtobufPriceState price = new ProtobufPriceState(DESCRIPTOR, "price");

    public TickTimeseriesSerializer() {
        this(false);
    }

    public TickTimeseriesSerializer(boolean useMillis) {
        this.dateTime = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
    }

    public ByteBuffer serialize(Iterable<TickImpl> ticks) throws IOException {
        for (final TickImpl tick : ticks) {
            final TimeseriesProtos.Tick.Builder element = TimeseriesProtos.Tick.newBuilder();

            this.dateTime.update(element, tick.getDateTime());
            this.price.update(element, tick.getPrice());
            if (tick.hasVolume()) {
                element.setVolume(tick.volume());
            }
            if (tick.getSupplement() != null) {
                element.setSupplement(tick.getSupplement());
            }
            if (tick.getTradeIdentifier() != null) {
                element.setTradeIdentifier(tick.getTradeIdentifier());
            }

            if (tick.hasFields()) {
                for (SnapField sf : tick.getFields()) {
                    element.addFields(SerializationUtil.serialize(sf, this.dateTime.isUseMillis()));
                }
            }

            addObject(element);
        }

        return getResult();
    }
}
