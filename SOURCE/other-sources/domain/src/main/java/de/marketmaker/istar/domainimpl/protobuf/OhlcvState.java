/*
 * OhlcvState.java
 *
 * Created on 15.09.2010 11:56:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

/**
 * @author oflege
 */
class OhlcvState extends AbstractDeserializerState {
    private TimeseriesProtos.Ohlcv.Builder builder
            = TimeseriesProtos.Ohlcv.newBuilder();
    
    TimeseriesProtos.Ohlcv merge(TimeseriesProtos.Ohlcv update) {
        if (update.hasTime()) {
            this.builder.setTime(this.builder.getTime() + update.getTime());
        }
        
        if (update.hasVolume()) {
            this.builder.setVolume(update.getVolume());
        }
        else if (builder.hasVolume()) {
            builder.clearVolume();
        }
        
        if (update.hasCount()) {
            this.builder.setCount(update.getCount());
        }
        else if (builder.hasCount()) {
            builder.clearCount();
        }

        mergeOpen(update);
        mergeHigh(update);
        mergeLow(update);
        mergeClose(update);

        final TimeseriesProtos.Ohlcv.Builder resultBuilder = this.builder.clone();
        if (!update.hasOpen()) {
            resultBuilder.clearOpen();
        }
        if (!update.hasHigh()) {
            resultBuilder.clearHigh();
        }
        if (!update.hasLow()) {
            resultBuilder.clearLow();
        }
        if (!update.hasClose()) {
            resultBuilder.clearClose();
        }
        return resultBuilder.build();
    }

    private void mergeOpen(TimeseriesProtos.Ohlcv update) {
        if (update.hasOpen()) {
            if (update.hasExponentOpen()) {
                final int n = update.getExponentOpen();
                this.builder.setOpen(rescale(this.builder.getOpen(), this.builder.getExponentOpen(), n));
                this.builder.setExponentOpen(n);
            }
            this.builder.setOpen(this.builder.getOpen() + update.getOpen());
        }
    }

    private void mergeHigh(TimeseriesProtos.Ohlcv update) {
        if (update.hasHigh()) {
            if (update.hasExponentHigh()) {
                final int n = update.getExponentHigh();
                this.builder.setHigh(rescale(this.builder.getHigh(), this.builder.getExponentHigh(), n));
                this.builder.setExponentHigh(n);
            }
            this.builder.setHigh(this.builder.getHigh() + update.getHigh());
        }
    }

    private void mergeLow(TimeseriesProtos.Ohlcv update) {
        if (update.hasLow()) {
            if (update.hasExponentLow()) {
                final int n = update.getExponentLow();
                this.builder.setLow(rescale(this.builder.getLow(), this.builder.getExponentLow(), n));
                this.builder.setExponentLow(n);
            }
            this.builder.setLow(this.builder.getLow() + update.getLow());
        }
    }

    private void mergeClose(TimeseriesProtos.Ohlcv update) {
        if (update.hasClose()) {
            if (update.hasExponentClose()) {
                final int n = update.getExponentClose();
                this.builder.setClose(rescale(this.builder.getClose(), this.builder.getExponentClose(), n));
                this.builder.setExponentClose(n);
            }
            this.builder.setClose(this.builder.getClose() + update.getClose());
        }
    }

}
