/*
 * TickEventState.java
 *
 * Created on 15.09.2010 11:56:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

/**
 * @author oflege
 */
class TickEventState extends AbstractDeserializerState {
    private TimeseriesProtos.TickEvent.Builder builder
            = TimeseriesProtos.TickEvent.newBuilder();
    
    TimeseriesProtos.TickEvent merge(TimeseriesProtos.TickEvent update) {
        if (update.hasTime()) {
            this.builder.setTime(this.builder.getTime() + update.getTime());
        }
        
        if (update.hasVolume()) {
            this.builder.setVolume(update.getVolume());
        }
        else {
            builder.clearVolume();
        }

        if (update.hasAskVolume()) {
            this.builder.setAskVolume(update.getAskVolume());
        }
        else {
            builder.clearAskVolume();
        }

        if (update.hasBidVolume()) {
            this.builder.setBidVolume(update.getBidVolume());
        }
        else {
            this.builder.clearBidVolume();
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

        mergePrice(update);
        mergeBidPrice(update);
        mergeAskPrice(update);

        this.builder.clearFields();
        if (update.getFieldsCount() > 0) {
            for (TimeseriesProtos.Field field : update.getFieldsList()) {
                this.builder.addFields(field);
            }
        }

        final TimeseriesProtos.TickEvent.Builder resultBuilder = this.builder.clone();
        if (!update.hasPrice()) {
            resultBuilder.clearPrice();
        }
        if (!update.hasAskPrice()) {
            resultBuilder.clearAskPrice();
        }
        if (!update.hasBidPrice()) {
            resultBuilder.clearBidPrice();
        }
        return resultBuilder.build();
    }

    private void mergePrice(TimeseriesProtos.TickEvent update) {
        if (update.hasPrice()) {
            if (update.hasExponentPrice()) {
                final int n = update.getExponentPrice();
                this.builder.setPrice(rescale(this.builder.getPrice(), this.builder.getExponentPrice(), n));
                this.builder.setExponentPrice(n);
            }
            this.builder.setPrice(this.builder.getPrice() + update.getPrice());
        }
    }

    private void mergeBidPrice(TimeseriesProtos.TickEvent update) {
        if (update.hasBidPrice()) {
            if (update.hasExponentBidPrice()) {
                final int n = update.getExponentBidPrice();
                this.builder.setBidPrice(rescale(this.builder.getBidPrice(), this.builder.getExponentBidPrice(), n));
                this.builder.setExponentBidPrice(n);
            }
            this.builder.setBidPrice(this.builder.getBidPrice() + update.getBidPrice());
        }
    }

    private void mergeAskPrice(TimeseriesProtos.TickEvent update) {
        if (update.hasAskPrice()) {
            if (update.hasExponentAskPrice()) {
                final int n = update.getExponentAskPrice();
                this.builder.setAskPrice(rescale(this.builder.getAskPrice(), this.builder.getExponentAskPrice(), n));
                this.builder.setExponentAskPrice(n);
            }
            this.builder.setAskPrice(this.builder.getAskPrice() + update.getAskPrice());
        }
    }
}
