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
class TickState extends AbstractDeserializerState {
    private TimeseriesProtos.Tick.Builder builder
            = TimeseriesProtos.Tick.newBuilder();

    TimeseriesProtos.Tick merge(TimeseriesProtos.Tick update) {
        if (update.hasTime()) {
            this.builder.setTime(this.builder.getTime() + update.getTime());
        }
        
        if (update.hasVolume()) {
            this.builder.setVolume(update.getVolume());
        }
        else {
            builder.clearVolume();
        }

        if (update.hasSupplement()) {
            this.builder.setSupplement(update.getSupplement());
        }
        else {
            this.builder.clearSupplement();
        }

        if (update.hasTradeIdentifier()) {
            this.builder.setTradeIdentifier(update.getTradeIdentifier());
        }
        else {
            this.builder.clearTradeIdentifier();
        }

        if (update.hasPrice()) {
            if (update.hasExponentPrice()) {
                final int n = update.getExponentPrice();
                this.builder.setPrice(rescale(this.builder.getPrice(), this.builder.getExponentPrice(), n));
                this.builder.setExponentPrice(n);
            }
            this.builder.setPrice(this.builder.getPrice() + update.getPrice());
        }

        this.builder.clearFields();
        if (update.getFieldsCount() > 0) {
            for (TimeseriesProtos.Field field : update.getFieldsList()) {
                this.builder.addFields(field);
            }
        }

        final TimeseriesProtos.Tick.Builder resultBuilder = this.builder.clone();
        if (!update.hasPrice()) {
            resultBuilder.clearPrice();
        }
        return resultBuilder.build();
    }
}
