/*
 * FundPriceTimeseriesSerializer.java
 *
 * Created on 26.11.2009 20:18:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.math.BigDecimal;
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
public class VolumeAggregationTimeseriesSerializer extends AbstractProtobufSerializer
        implements AggregatedTickSerializer {
    private static final Descriptors.Descriptor DESCRIPTOR
            = TimeseriesProtos.VolumeAggregation.getDescriptor();

    private final ProtobufDateTimeState dateTime;

    private final ProtobufPriceState totalVolumeCall = new ProtobufPriceState(DESCRIPTOR, "total_volume_call");

    private final ProtobufPriceState totalVolumePut = new ProtobufPriceState(DESCRIPTOR, "total_volume_put");

    private final ProtobufPriceState totalBlockTradeVolumeCall = new ProtobufPriceState(DESCRIPTOR, "total_block_trade_volume_call");

    private final ProtobufPriceState totalBlockTradeVolumePut = new ProtobufPriceState(DESCRIPTOR, "total_block_trade_volume_put");

    private final ProtobufPriceState totalVolumeFutures = new ProtobufPriceState(DESCRIPTOR, "total_volume_futures");

    private final ProtobufPriceState totalBlockTradeVolumeFutures = new ProtobufPriceState(DESCRIPTOR, "total_block_trade_volume_futures");

    public VolumeAggregationTimeseriesSerializer() {
        this(false);
    }

    public VolumeAggregationTimeseriesSerializer(boolean useMillis) {
        this.dateTime = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
    }

    public ByteBuffer serialize(List<AggregatedTickImpl> ticks) throws IOException {
        for (final AggregatedTickImpl tick : ticks) {
            final TimeseriesProtos.VolumeAggregation.Builder element
                    = TimeseriesProtos.VolumeAggregation.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());

            this.totalVolumeCall.update(element, tick.getOpen());
            this.totalVolumePut.update(element, tick.getClose());
            this.totalBlockTradeVolumeCall.update(element, tick.getLow());
            this.totalBlockTradeVolumePut.update(element, new BigDecimal(tick.getNumberOfAggregatedTicks()));
            this.totalVolumeFutures.update(element, tick.getHigh());
            this.totalBlockTradeVolumeFutures.update(element, new BigDecimal(tick.getVolume()));

            addObject(element);
        }

        return getResult();
    }

    @Override
    public ByteBuffer serializeValues(List<AggregatedValue> ticks) throws IOException {
        for (final AggregatedValue tick : ticks) {
            final TimeseriesProtos.VolumeAggregation.Builder element
                    = TimeseriesProtos.VolumeAggregation.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());

            this.totalVolumeCall.update(element, toBigDecimal(tick.getTotalVolumeCall()));
            this.totalVolumePut.update(element, toBigDecimal(tick.getTotalVolumePut()));
            this.totalBlockTradeVolumeCall.update(element, toBigDecimal(tick.getTotalBlockTradeVolumeCall()));
            this.totalBlockTradeVolumePut.update(element, toBigDecimal(tick.getTotalBlockTradeVolumePut()));
            this.totalVolumeFutures.update(element, toBigDecimal(tick.getTotalVolumeFutures()));
            this.totalBlockTradeVolumeFutures.update(element, toBigDecimal(tick.getTotalBlockTradeVolumeFutures()));

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