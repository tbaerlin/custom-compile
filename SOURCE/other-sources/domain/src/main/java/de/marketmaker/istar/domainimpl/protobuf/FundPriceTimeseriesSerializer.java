/*
 * FundPriceTimeseriesSerializer.java
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
public class FundPriceTimeseriesSerializer extends AbstractProtobufSerializer implements AggregatedTickSerializer {

    private static final Descriptors.Descriptor DESCRIPTOR = TimeseriesProtos.FundPrice.getDescriptor();

    private final ProtobufDateTimeState dateTime;

    private final ProtobufPriceState issuePrice = new ProtobufPriceState(DESCRIPTOR, "issue_price");

    private final ProtobufPriceState repurchasingPrice = new ProtobufPriceState(DESCRIPTOR, "repurchasing_price");

    private final ProtobufPriceState netAssetValue = new ProtobufPriceState(DESCRIPTOR, "net_asset_value");

    public FundPriceTimeseriesSerializer() {
        this(false);
    }

    public FundPriceTimeseriesSerializer(boolean useMillis) {
        this.dateTime = new ProtobufDateTimeState(DESCRIPTOR, useMillis);
    }

    @Override
    public ByteBuffer serialize(List<AggregatedTickImpl> ticks) throws IOException {
        for (final AggregatedTickImpl tick : ticks) {
            final TimeseriesProtos.FundPrice.Builder element = TimeseriesProtos.FundPrice.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());

            this.issuePrice.update(element, tick.getHigh());
            this.repurchasingPrice.update(element, tick.getClose());

            if (null != tick.getVolume()) {
                element.setVolume(tick.getVolume());
            }

            addObject(element);
        }

        return getResult();
    }

    @Override
    public ByteBuffer serializeValues(List<AggregatedValue> ticks) throws IOException {
        for (final AggregatedValue tick : ticks) {
            final TimeseriesProtos.FundPrice.Builder element = TimeseriesProtos.FundPrice.newBuilder();

            this.dateTime.update(element, tick.getInterval().getStart());

            this.issuePrice.update(element, tick.getIssuePrice());
            this.repurchasingPrice.update(element, tick.getRedemptionPrice());
            this.netAssetValue.update(element, tick.getNetAssetValue());

            if (null != tick.getVolume()) {
                element.setVolume(tick.getVolume());
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