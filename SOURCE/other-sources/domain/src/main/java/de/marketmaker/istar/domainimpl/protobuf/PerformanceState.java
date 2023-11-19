/*
 * TickState.java
 *
 * Created on 15.09.2010 11:56:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

/**
 * @author oflege
 */
class PerformanceState extends AbstractDeserializerState {
    private TimeseriesProtos.Performance.Builder builder
            = TimeseriesProtos.Performance.newBuilder();

    TimeseriesProtos.Performance merge(TimeseriesProtos.Performance update) {
        if (update.hasTime()) {
            this.builder.setTime(this.builder.getTime() + update.getTime());
        }

        if (update.hasPercent()) {
            if (update.hasExponentPercent()) {
                final int n = update.getExponentPercent();
                this.builder.setPercent(rescale(this.builder.getPercent(), this.builder.getExponentPercent(), n));
                this.builder.setExponentPercent(n);
            }
            this.builder.setPercent(this.builder.getPercent() + update.getPercent());
        }

        final TimeseriesProtos.Performance.Builder resultBuilder = this.builder.clone();
        if (!update.hasPercent()) {
            resultBuilder.clearPercent();
        }
        return resultBuilder.build();
    }
}
