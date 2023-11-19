/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInterval;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.AggregatedTickDecoder;
import de.marketmaker.istar.feed.tick.AggregatedTickRecordImpl;
import de.marketmaker.istar.feed.tick.RawAggregatedTick;

/**
 * @author zzhao
 */
public abstract class HistoryGathererTickBase implements HistoryGathererTick {

    /**
     * buffer holds tick history aggregate data from year (max. 16m) and month (avg. 16/12=1.33m)
     * for one symbol. see ISTAR-655
     */
    protected final ByteBuffer buffer = ByteBuffer.allocateDirect(18 * 1024 * 1024);

    /**
     * patchBuf holds tick history aggregate data from patch (max. 16m)
     */
    protected final ByteBuffer patchBuf = ByteBuffer.allocateDirect(16 * 1024 * 1024);

    protected abstract HistoryReader<ByteString> getPatchReader();

    protected void loadPatchedEntries(ByteString key, Interval interval) throws IOException {
        this.patchBuf.clear();
        getPatchReader().loadData(key, interval, this.patchBuf);
        this.patchBuf.flip();
    }

    protected void loadPatchedEntries(ByteString key) throws IOException {
        this.patchBuf.clear();
        getPatchReader().loadData(key, this.patchBuf);
        this.patchBuf.flip();
    }

    public static int fillTicks(Iterable<MutableTickEntry> it, AggregatedHistoryTickRecord atr,
            Interval interval, int minTickNum, boolean alignWithStart, LocalDate genesis) {
        final ReadableDuration duration = atr.getAggregation();
        final int daysFrom = HistoryUtil.daysFromBegin(genesis, interval.getStart().toLocalDate());
        final int daysTo = HistoryUtil.daysFromBegin(genesis,
                HistoryUtil.isStartOfDay(interval.getEnd())
                        ? interval.getEnd().toLocalDate()
                        : interval.getEnd().toLocalDate().plusDays(1));
        int count = 0;
        if (!alignWithStart) {
            for (MutableTickEntry tickEntry : it) {
                if (tickEntry.getDays() >= daysFrom && tickEntry.getDays() < daysTo) {
                    count += aggregateTicks(atr, interval, minTickNum, duration, tickEntry, genesis);
                    if (minTickNum > 0 && count >= minTickNum) {
                        break;
                    }
                }
                else {
                    if (tickEntry.getDays() < daysFrom) {
                        break;
                    }
                }
            }
        }
        else {
            final ArrayList<MutableTickEntry> list = new ArrayList<>();
            for (MutableTickEntry tickEntry : it) {
                if (tickEntry.getDays() >= daysFrom && tickEntry.getDays() < daysTo) {
                    list.add(tickEntry.copy());
                }
                else {
                    if (tickEntry.getDays() < daysFrom) {
                        break;
                    }
                }
            }

            for (int i = list.size() - 1; i >= 0; i--) {
                final MutableTickEntry entry = list.get(i);
                count += aggregateTicks(atr, interval, minTickNum, duration, entry, genesis);
                if (minTickNum > 0 && count >= minTickNum) {
                    break;
                }
            }
        }

        return count;
    }

    private static int aggregateTicks(AggregatedHistoryTickRecord atr, Interval interval,
            int minTickNum, ReadableDuration duration, MutableTickEntry tickEntry,
            LocalDate genesis) {
        final int date = HistoryUtil.daysFromBegin2Date(genesis, tickEntry.getDays());
        if (duration.isEqual(TickHistoryRequest.AGGREGATION)) {
            final int size = atr.getItems().size();
            atr.add(date, tickEntry.getDataDecompress(), tickEntry.getTickNum());

            // does not count tick numbers for the first matching day
            // EUR.FXVWD on 20120701 has only ticks after 23 o'clock and more than 5 ticks
            // a query with end datetime 2012-07-01T12:05:00 with numTrades=5 has to be answered
            // with tick data in 2012-06-30. We give back tick data one more day from back end
            // to front end to answer such kind of queries
            return size == 0 ? 0 : tickEntry.getTickNum();
        }
        else {
            final AggregatedHistoryTickRecord record = getATR1M(atr.getTickType());
            record.add(date, tickEntry.getDataDecompress(), tickEntry.getTickNum());
            final AggregatedHistoryTickRecord agg = record.aggregate(duration, interval);
            final AbstractTickRecord.TickItem item = agg.getItem(date);
            if (item != null) {
                atr.add(date, item.getData(), item.getNumTicks());
            }
            if (minTickNum > 0) {
                return numTicks(agg, interval);
            }
            return 0; // if minTickNum is not given, no need to count. Counting takes time.
        }
    }

    private static int numTicks(AggregatedTickRecordImpl atr, Interval interval) {
        final Timeseries<AggregatedTick> timeseries = atr.getTimeseries(interval);
        int count = 0;
        for (DataWithInterval<AggregatedTick> at : timeseries) {
            count++;
        }

        return count;
    }

    private static AggregatedHistoryTickRecord getATR1M(TickType tickType) {
        return new AggregatedHistoryTickRecord(TickHistoryRequest.AGGREGATION, tickType);
    }

    public abstract String statistic(String symbol);

    @ManagedOperation(description = "invoke to ticks for a given symbol and date")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "symbol", description = "a VWD symbol"),
            @ManagedOperationParameter(name = "from", description = "a day yyyyMMdd"),
            @ManagedOperationParameter(name = "to", description = "a day yyyyMMdd")
    })
    public String query(String symbol, int from, int to) {
        final TimeTaker tt = new TimeTaker();
        final DateTime start = (from == 0)
                ? new DateTime().withTimeAtStartOfDay().minusDays(1)
                : DateUtil.yyyymmddToDateTime(from);
        final DateTime end = (to == 0 || from == to)
                ? start.plusDays(1)
                : DateUtil.yyyymmddToDateTime(to);

        final TickHistoryRequest req = new TickHistoryRequest(symbol, new Interval(start, end),
                TickHistoryRequest.AGGREGATION, 0, false, getTickType());
        final AggregatedHistoryTickRecord record = new AggregatedHistoryTickRecord(
                TickHistoryRequest.AGGREGATION, getTickType());
        try {
            DateTime historyEnd = gatherTicks(record, Collections.singletonList(req.getVwdCode()), req);
            return toQueryResult(symbol, start, end, record, tt.toString(), getTickType(), historyEnd);
        } catch (IOException e) {
            return e.getMessage();
        }

    }

    public static String toQueryResult(String symbol, DateTime start, DateTime end,
            AggregatedHistoryTickRecord record, String time, TickType tickType,
            DateTime historyEnd) {
        final StringBuilder sb = new StringBuilder();
        DateTime dt = start;
        int count = 0;
        int size = 0;
        while (dt.isBefore(end)) {
            final int date = DateUtil.toYyyyMmDd(dt);
            final AbstractTickRecord.TickItem item = record.getItem(date);
            if (null != item && null != item.getData()) {
                final List<AggregatedTickImpl> list =
                        fromTickEntry(ByteBuffer.wrap(item.getData()), date, tickType);
                sb.append("\n").append(date).append(" ").append(item.getNumTicks())
                        .append(" ticks, ").append(item.getData().length).append(" bytes");
                count += item.getNumTicks();
                size += item.getData().length;
                for (AggregatedTickImpl at : list) {
                    sb.append("\n");
                    appendTick(sb, at);
                }
            }
            else {
                sb.append("\n").append(date).append(" no ticks");
            }

            dt = dt.plusDays(1);
        }

        sb.insert(0, symbol.toUpperCase() + "#" + tickType
                + HistoryUtil.toIntervalString(start, end) + "\n"
                + count + " ticks, " + size + " bytes in: " + time);
        sb.insert(0, "History end: " + DateUtil.toYyyyMmDd(historyEnd) + "\n");
        return sb.toString();
    }


    public static List<AggregatedTickImpl> fromTickEntry(ByteBuffer bb, int date,
            TickType tickType) {
        final AggregatedTickDecoder decoder = new AggregatedTickDecoder(bb);
        final DateTime dateTime = DateUtil.yyyymmddToDateTime(date);
        final ArrayList<AggregatedTickImpl> list = new ArrayList<>();
        for (RawAggregatedTick tick : decoder) {
            final AggregatedTickImpl at = new AggregatedTickImpl(
                    toInterval(dateTime.plusSeconds(tick.getTime())),
                    PriceCoder.decode(tick.getOpen()), PriceCoder.decode(tick.getHigh()),
                    PriceCoder.decode(tick.getLow()), PriceCoder.decode(tick.getClose()),
                    tick.getVolume(), tick.getNumberOfAggregatedTicks(), tickType);
            list.add(at);
        }

        return list;
    }

    private static ReadableInterval toInterval(DateTime dateTime) {
        return new Interval(dateTime, dateTime.plusMinutes(1));
    }

    private static String toIntervalString(ReadableInterval interval) {
        return HistoryUtil.DTF_MINUTE.print(interval.getStart()) + " ~ "
                + HistoryUtil.DTF_MINUTE.print(interval.getEnd());
    }

    private static void appendTick(StringBuilder sb, AggregatedTickImpl at) {
        sb.append(toIntervalString(at.getInterval()))
                .append(", O: ").append(at.getOpen()).append(", H: ")
                .append(at.getHigh()).append(", L: ").append(at.getLow()).append(", C: ")
                .append(at.getClose()).append(", V: ").append(at.getVolume()).append(", NoAT: ")
                .append(at.getNumberOfAggregatedTicks());
    }
}
