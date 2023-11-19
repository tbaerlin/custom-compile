package de.marketmaker.istar.domainimpl;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickList.FieldPermissions;
import java.math.BigDecimal;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

public class AggregatedTickEvent {

    static class Builder {

        private enum TickPosition {
            BOTH, START, END, NONE
        }

        private long open;
        private long high = Long.MIN_VALUE;
        private long low = Long.MIN_VALUE;
        private long close;
        private int numberOfAggregatedTicks = 0;
        private long volume;

        private long startMillis = Long.MIN_VALUE;
        private long endMillis = Long.MIN_VALUE;

        private final FieldPermissions permissions;
        private final TickImpl.Type type;

        /**
         * Optional parameters for future validation and extension purposes.
         */
        public Builder(FieldPermissions permissions, TickImpl.Type type) {
            this.permissions = permissions;
            this.type = type;
        }

        public void add(DataWithInterval<TickEvent> tick) {
            final TickEvent tickEvent = tick.getData();
            final ReadableInterval interval = tick.getInterval();
            final TickPosition tickPosition = mergeInterval(interval.getStartMillis(), interval.getEndMillis());

            incNumberOfAggregatedTicks(1);
            incVolume(getVolume(tickEvent));

            final long price = getPrice(tickEvent);

            if (tickPosition == TickPosition.BOTH || tickPosition == TickPosition.START) {
                this.open = price;
            }

            setHighAsMax(price);
            setLowAsMin(price);

            if (tickPosition == TickPosition.BOTH || tickPosition == TickPosition.END) {
                this.close = price;
            }
        }

        private long getVolume(TickEvent tickEvent) {
            // align with de.marketmaker.istar.domain.data.TickList.toTick
            switch (this.type) {
                case ASK:
                    return (this.permissions == null || this.permissions.isAskVolume())
                        ? tickEvent.getAskVolume()
                        : Long.MIN_VALUE;
                case BID:
                    return (this.permissions == null || this.permissions.isBidVolume())
                        ? tickEvent.getBidVolume()
                        : Long.MIN_VALUE;
                default: // this aligns with implementation before, but may be wrong except for TRADE
                    return (this.permissions == null || this.permissions.isTradeVolume())
                        ? tickEvent.getVolume()
                        : Long.MIN_VALUE;
            }
        }

        private long getPrice(TickEvent tickEvent) {
            switch (this.type) {
                case ASK:
                    return tickEvent.getAskPrice();
                case BID:
                    return tickEvent.getBidPrice();
                default: // this aligns with implementation before, but may be wrong except for TRADE
                    return tickEvent.getPrice();
            }
        }

        private void incNumberOfAggregatedTicks(int amount) {
            this.numberOfAggregatedTicks += amount;
        }

        private void incVolume(long amount) {
            this.volume += (Long.MIN_VALUE == amount) ? 0 : amount;
        }

        private void setHighAsMax(long high) {
            this.high = this.high == Long.MIN_VALUE ? high : PriceCoder.max(this.high, high);
        }

        private void setLowAsMin(long low) {
            this.low = this.low == Long.MIN_VALUE ? low : PriceCoder.min(this.low, low);
        }

        private TickPosition mergeInterval(long startMillis, long endMillis) {
            if (this.startMillis == Long.MIN_VALUE && this.endMillis == Long.MIN_VALUE) {
                this.startMillis = startMillis;
                this.endMillis = endMillis;
                return TickPosition.BOTH;
            }

            TickPosition result = TickPosition.NONE;
            if (this.startMillis == Long.MIN_VALUE || this.startMillis > startMillis) {
                this.startMillis = startMillis;
                result = TickPosition.START;
            }
            if (this.startMillis == Long.MIN_VALUE || this.endMillis < endMillis) {
                this.endMillis = endMillis;
                result = result == TickPosition.START ? TickPosition.BOTH : TickPosition.END;
            }

            return result;
        }

        public int getNumberOfAggregatedTicks() {
            return numberOfAggregatedTicks;
        }

        public AggregatedTickEvent build() {
            Interval interval = new Interval(this.startMillis, this.endMillis);
            return new AggregatedTickEvent(this.type, interval,
                    PriceCoder.decode(this.open), PriceCoder.decode(this.high), PriceCoder.decode(this.low), PriceCoder.decode(this.close),
                    this.volume, this.numberOfAggregatedTicks);
        }
    }

    private final TickImpl.Type type;

    private final Interval interval;

    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final int numberOfAggregatedTicks;
    private final long volume;

    /**
     * Start new aggregation beginning at startTime, first price and volume as parameters
     */
    public AggregatedTickEvent(TickImpl.Type type, Interval interval,
                               BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume,
                               int numberOfAggregatedTicks) {
        this.type = type;
        this.interval = interval;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.numberOfAggregatedTicks = numberOfAggregatedTicks;
    }

    // see AggregatedTickImpl:

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public Long getVolume() {
        return volume;
    }

    public int getNumberOfAggregatedTicks() {
        return numberOfAggregatedTicks;
    }

    public TickImpl.Type getType() {
        return type;
    }

    public ReadableInterval getInterval() {
        return this.interval;
    }
}
