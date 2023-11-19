/*
 * TickEventTimeseries.java
 *
 * Created on 08.01.13 12:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.util.Iterator;
import java.util.List;

import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickEvent;

/**
 * @author oflege
 */
public class TickEventTimeseries extends AbstractTickTimeseries<TickEvent> {

    /**
     * Combines Interval and TickEvent, delegates to both
     */
    private static class TickEventWithInterval implements DataWithInterval<TickEvent>, TickEvent {
        private ReadableInterval interval;

        private RawTick rawTick;

        public String toString() {
            return "TickEventWithInterval[" + this.rawTick + "," + this.interval + "]";
        }

        public TickEvent getData() {
            return this.rawTick;
        }

        public ReadableInterval getInterval() {
            return this.interval;
        }

        public long getAskPrice() {
            return rawTick.getAskPrice();
        }

        public long getAskVolume() {
            return rawTick.getAskVolume();
        }

        public long getBidPrice() {
            return rawTick.getBidPrice();
        }

        public long getBidVolume() {
            return rawTick.getBidVolume();
        }

        public int getHeader() {
            return rawTick.getHeader();
        }

        public long getPrice() {
            return rawTick.getPrice();
        }

        public String getSupplement() {
            return rawTick.getSupplement();
        }

        public String getTradeIdentifier() {
            return rawTick.getTradeIdentifier();
        }

        @Override
        public List<SnapField> getAdditionalFields() {
            return rawTick.getAdditionalFields();
        }

        public int getTime() {
            return rawTick.getTime();
        }

        public long getVolume() {
            return rawTick.getVolume();
        }

        public long getYield() {
            return rawTick.getYield();
        }

        public boolean isAsk() {
            return rawTick.isAsk();
        }

        public boolean isAskPresent() {
            return rawTick.isAskPresent();
        }

        public boolean isAskVolumePresent() {
            return rawTick.isAskVolumePresent();
        }

        public boolean isBid() {
            return rawTick.isBid();
        }

        public boolean isBidPresent() {
            return rawTick.isBidPresent();
        }

        public boolean isBidVolumePresent() {
            return rawTick.isBidVolumePresent();
        }

        public boolean isPricePresent() {
            return rawTick.isPricePresent();
        }

        public boolean isSuspendEnd() {
            return rawTick.isSuspendEnd();
        }

        public boolean isSuspendStart() {
            return rawTick.isSuspendStart();
        }

        public boolean isTrade() {
            return rawTick.isTrade();
        }

        public boolean isVolumePresent() {
            return rawTick.isVolumePresent();
        }

        public boolean isWithClose() {
            return rawTick.isWithClose();
        }

        public boolean isWithKassa() {
            return rawTick.isWithKassa();
        }

        public boolean isWithYield() {
            return rawTick.isWithYield();
        }
    }

    private final boolean withAdditionalFields;

    TickEventTimeseries(TickRecordImpl tickRecord, ReadableInterval interval,
            boolean withAdditionalFields) {
        super(tickRecord, interval);
        this.withAdditionalFields = withAdditionalFields;
    }

    public Iterator<DataWithInterval<TickEvent>> iterator() {
        final TickEventWithInterval tewi = new TickEventWithInterval();

        return new RawTickIterator<TickEvent>(this, null, this.withAdditionalFields) {
            protected DataWithInterval<TickEvent> getData(RawTick rt, ReadableInterval i) {
                tewi.rawTick = rt;
                tewi.interval = i;
                return tewi;
            }
        };
    }

}
