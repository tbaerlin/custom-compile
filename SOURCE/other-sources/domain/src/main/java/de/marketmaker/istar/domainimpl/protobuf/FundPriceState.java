/*
 * FundPriceState.java
 *
 * Created on 15.09.2010 11:56:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

/**
 * @author oflege
 */
class FundPriceState extends AbstractDeserializerState {
    private TimeseriesProtos.FundPrice.Builder builder
            = TimeseriesProtos.FundPrice.newBuilder();

    TimeseriesProtos.FundPrice merge(TimeseriesProtos.FundPrice update) {
        if (update.hasTime()) {
            this.builder.setTime(this.builder.getTime() + update.getTime());
        }

        mergeIssuePrice(update);
        mergeRepurchasingPrice(update);

        final TimeseriesProtos.FundPrice.Builder resultBuilder = this.builder.clone();
        if (!update.hasIssuePrice()) {
            resultBuilder.clearIssuePrice();
        }
        if (!update.hasRepurchasingPrice()) {
            resultBuilder.clearRepurchasingPrice();
        }
        return resultBuilder.build();
    }

    private void mergeIssuePrice(TimeseriesProtos.FundPrice update) {
        if (update.hasIssuePrice()) {
            if (update.hasExponentIssuePrice()) {
                final int n = update.getExponentIssuePrice();
                this.builder.setIssuePrice(rescale(this.builder.getIssuePrice(), this.builder.getExponentIssuePrice(), n));
                this.builder.setExponentIssuePrice(n);
            }
            this.builder.setIssuePrice(this.builder.getIssuePrice() + update.getIssuePrice());
        }
    }

    private void mergeRepurchasingPrice(TimeseriesProtos.FundPrice update) {
        if (update.hasRepurchasingPrice()) {
            if (update.hasExponentRepurchasingPrice()) {
                final int n = update.getExponentRepurchasingPrice();
                this.builder.setRepurchasingPrice(rescale(this.builder.getRepurchasingPrice(), this.builder.getExponentRepurchasingPrice(), n));
                this.builder.setExponentRepurchasingPrice(n);
            }
            this.builder.setRepurchasingPrice(this.builder.getRepurchasingPrice() + update.getRepurchasingPrice());
        }
    }
}
