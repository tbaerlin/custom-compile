/*
 * RawTickIterator.java
 *
 * Created on 04.03.13 14:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickType;

import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

/**
* @author oflege
*/
abstract class RawTickIterator<K> implements Iterator<DataWithInterval<K>> {

    private final Iterator<AbstractTickRecord.TickItem> itemIt;

    private DataWithInterval<K> current;

    private MutableInterval mi = new MutableInterval();

    private Iterator<RawTick> rti;

    private int endSec;

    private long startMillis;

    private boolean checkLast;

    private boolean seenLast;

    private final boolean ascendingTickTimes;

    private final boolean withAdditionalFields;

    private final TickType type;

    /**
     * Used to detect out of order ticks. In general, a tick's timestamp must be equal to or
     * greater than the previous tick's timestamp. Otherwise, it will be dropped (sorting the
     * ticks is not an option as the number of ticks per day may be 500000 or more). A
     * common reason for out-of-order ticks in Xetra based systems is that bid/ask ticks are
     * generated on a different system than trade ticks, so we may see aks/bids and then
     * a trade that was generated 20s earlier. If the ticks are filtered by type, this is
     * no problem and all we need is this field that contains the timestamp of the previous
     * tick of the same type.
     */
    private int lastTime;

    /**
     * If ticks are not filtered by type, this iterator returns TickEvent data objects. For
     * TickEvents, out-of-order timestamps are allowed as long as the tick types for those
     * events do not overlap. If a TickEvent is a trade, its timestamp will be assigned
     * to this field. The timestamp of the next trade-only TickEvent is acceptable if it
     * is not smaller than the value of this field.
     */
    private int lastTradeTime;

    private int startSec;

    private AbstractTickTimeseries tickTimeseries;

    private long prevTransitionMillis;

    private long transitionOffset;

    RawTickIterator(AbstractTickTimeseries tickTimeseries) {
        this(tickTimeseries, null);
    }

    RawTickIterator(AbstractTickTimeseries tickTimeseries, TickType type) {
        this(tickTimeseries, type, false);
    }

    RawTickIterator(AbstractTickTimeseries tickTimeseries, TickType type, boolean withAdditionalFields) {
        this(tickTimeseries, type, withAdditionalFields, false);
    }

    private RawTickIterator(AbstractTickTimeseries tickTimeseries, TickType type,
            boolean withAdditionalFields, boolean ascendingTickTimes) {
        this.tickTimeseries = tickTimeseries;
        this.type = type;
        this.ascendingTickTimes = ascendingTickTimes;
        this.withAdditionalFields = withAdditionalFields;
        this.itemIt = tickTimeseries.tickRecord.getItems().iterator();
    }

    private DataWithInterval<K> advance() {
        final AbstractTickRecord.TickItem item;
        if (rti == null) {
            item = nextItem();
            if (item == null) {
                return null;
            }

            Interval i = item.getInterval();
            ackTransition(i);

            this.startSec = tickTimeseries.getStartSec(i);
            this.endSec = tickTimeseries.getEndSec(i);
            this.startMillis = i.getStartMillis();
            this.checkLast = (endSec != SECONDS_PER_DAY)
                    && item.contains(tickTimeseries.tickRecord.getLastTickDateTime());

            this.rti = item.createIterator(withAdditionalFields);

            this.lastTime = startSec;
            this.lastTradeTime = startSec;
        }

        while (rti.hasNext()) {
            final RawTick rawTick = rti.next();

            if (this.ascendingTickTimes) {
                if (!isAcceptable(rawTick)) {
                    continue;
                }

                if (rawTick.getTime() > this.endSec) {
                    this.seenLast = true;
                    return null;
                }

                this.lastTime = rawTick.getTime();
                if (this.type == null && rawTick.isTrade()) {
                    this.lastTradeTime = rawTick.getTime();
                }

                this.seenLast = this.checkLast && tickTimeseries.tickRecord.isLast(rawTick, this.endSec);
            }
            else {
                if (!isNewAcceptable(rawTick)) {
                    assert rawTick.getTime() >= 0 && rawTick.getTime() < 86400 : rawTick.toString();
                    continue;
                }
            }

            final long l = getTimeInMillis(rawTick.getTime() * 1000 + rawTick.getMillis());
            this.mi.setInterval(l, l);
            return getData(rawTick, this.mi);
        }
        this.rti = null;
        return advance();
    }

    private long getTimeInMillis(long millisInDay) {
        final long ms = this.startMillis + millisInDay;
        if (this.prevTransitionMillis > 0L && ms > this.prevTransitionMillis) {
            return ms + this.transitionOffset;
        }
        return ms;
    }

    private void ackTransition(Interval i) {
        long nextTransition = i.getStart().getZone().nextTransition(i.getStartMillis());
        if (nextTransition > i.getStartMillis() && nextTransition < i.getEndMillis()) {
            this.prevTransitionMillis = i.getEnd().getZone().previousTransition(i.getEndMillis());
            this.transitionOffset = (new DateTime(prevTransitionMillis).getSecondOfDay()
                    - new DateTime(nextTransition).getSecondOfDay() + 1) * DateTimeConstants.MILLIS_PER_SECOND;
        }
        else {
            this.prevTransitionMillis = 0L;
        }
    }

    private AbstractTickRecord.TickItem nextItem() {
        while (itemIt.hasNext()) {
            final AbstractTickRecord.TickItem tickItem = itemIt.next();
            if (tickItem.getInterval().overlaps(tickTimeseries.interval)) {
                return tickItem;
            }
        }
        return null;
    }

    public boolean hasNext() {
        if (this.current == null && !this.seenLast) {
            this.current = advance();
        }
        return current != null;
    }

    public DataWithInterval<K> next() {
        if (this.current == null) {
            throw new NoSuchElementException();
        }
        final DataWithInterval<K> result = this.current;
        this.current = null;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private boolean isNewAcceptable(RawTick rawTick) {
        return (this.type == null || rawTick.isRequiredType(type))
                && rawTick.getTime() >= this.startSec && rawTick.getTime() <= this.endSec;
    }

    protected boolean isAcceptable(RawTick tick) {
        if (this.type != null) {
            return tick.isRequiredType(type) && tick.getTime() >= this.lastTime;
        }
        if (tick.isAsk() || tick.isBid()) {
            return tick.getTime() >= this.lastTime;
        }
        if (tick.isTrade()) {
            return tick.getTime() >= this.lastTradeTime;
        }
        // default: suspend etc.
        return tick.getTime() >= this.lastTime;
    }

    protected abstract DataWithInterval<K> getData(RawTick rt, ReadableInterval i);

    public static void main(String[] args) {
        for (LocalDate ld: new LocalDate[] {new LocalDate(2014, 3, 30), new LocalDate(2014, 10, 26)}) {
            Interval i = ld.toInterval();
            long nt = i.getStart().getZone().nextTransition(i.getStartMillis());
            if (nt > i.getStartMillis() && nt < i.getEndMillis()) {
                long pt = i.getEnd().getZone().previousTransition(i.getEndMillis());
                int secOfDay = (int) ((nt - i.getStartMillis()) / DateTimeConstants.MILLIS_PER_SECOND);
                System.out.println(secOfDay);

                int nts = new DateTime(nt).getSecondOfDay();
                int pts = new DateTime(pt).getSecondOfDay();

                System.out.println(nt + " " + new DateTime(nt) + " " + nts);
                System.out.println(pt + " " + new DateTime(pt) + " " + pts);
                System.out.println(pts - nts + 1);
            }
            System.out.println("---------------");
        }
    }
}
