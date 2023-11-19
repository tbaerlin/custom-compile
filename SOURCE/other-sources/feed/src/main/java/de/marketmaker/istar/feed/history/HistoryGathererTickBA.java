/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;

import static de.marketmaker.istar.feed.history.TickHistoryContextImpl.GENESIS;

/**
 * @author zzhao
 */
public abstract class HistoryGathererTickBA extends HistoryGathererTickBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // similar to de.marketmaker.istar.feed.tick.TickServer.accessLogger
    private final Logger accessLogger = LoggerFactory.getLogger("[access].[TickHistory]");

    private HistoryGathererMsP<ByteString> delegate;

    public void setDelegate(HistoryGathererMsP<ByteString> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DateTime gatherTicks(AggregatedHistoryTickRecord record, List<String> symbols,
            TickHistoryRequest req) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final DateTime historyEnd;

        synchronized (this.buffer) {
            int count = 0;
            for (String symbol : symbols) {
                count += gatherTicks(record, symbol, req, count);
                if (req.getMinTickNum() > 0 && count >= req.getMinTickNum()) {
                    break;
                }
            }
            historyEnd = getHistoryEnd();
        }

        this.accessLogger.info("{} {} {} in {}", getTickType(), symbols,
                HistoryUtil.toIntervalString(req.getInterval()), tt);
        return historyEnd;
    }

    private DateTime getHistoryEnd() {
        return this.delegate.getMonthReader().getInterval().getEnd();
    }

    private int gatherTicks(AggregatedHistoryTickRecord record, String symbol,
            TickHistoryRequest req, int count) throws IOException {
        final ByteString key = HistoryUtil.getKey(new ByteString(symbol));
        final List<HistoryReader<ByteString>> yearReaders = this.delegate.getYearReaders();
        final HistoryReader<ByteString> monthReader = this.delegate.getMonthReader();

        loadPatchedEntries(key, req.getInterval());
        if (req.isAlignWithStart()) {
            for (int i = yearReaders.size() - 1; i >= 0; i--) {
                final HistoryReader<ByteString> reader = yearReaders.get(i);
                this.buffer.clear();
                reader.loadData(key, req.getInterval(), this.buffer);
                this.buffer.flip();

                if (this.buffer.hasRemaining() || this.patchBuf.hasRemaining()) {
                    count += fillTicks(HistoryUtil.patchIt(this.buffer, reader.getInterval(),
                                    this.patchBuf, MutableTickEntry.class, GENESIS),
                            record, req.getInterval(), req.getMinTickNum() - count,
                            req.isAlignWithStart(), GENESIS
                    );
                }
                if (req.getMinTickNum() > 0 && count >= req.getMinTickNum()) {
                    return count;
                }
            }

            this.buffer.clear();
            monthReader.loadData(key, req.getInterval(), this.buffer);
            this.buffer.flip();
            if (this.buffer.hasRemaining() || this.patchBuf.hasRemaining()) {
                count += fillTicks(HistoryUtil.patchIt(this.buffer, monthReader.getInterval(),
                                this.patchBuf, MutableTickEntry.class, GENESIS),
                        record, req.getInterval(), req.getMinTickNum() - count,
                        req.isAlignWithStart(), GENESIS
                );
            }
        }
        else {
            this.buffer.clear();
            monthReader.loadData(key, req.getInterval(), this.buffer);
            this.buffer.flip();
            if (null != monthReader.getInterval() &&
                    (this.buffer.hasRemaining() || this.patchBuf.hasRemaining())) {
                count += fillTicks(HistoryUtil.patchIt(this.buffer, monthReader.getInterval(),
                                this.patchBuf, MutableTickEntry.class, GENESIS),
                        record, req.getInterval(), req.getMinTickNum() - count,
                        req.isAlignWithStart(), GENESIS
                );
            }

            if (req.getMinTickNum() <= 0 || count < req.getMinTickNum()) {
                for (final HistoryReader<ByteString> reader : yearReaders) {
                    this.buffer.clear();
                    reader.loadData(key, req.getInterval(), this.buffer);
                    this.buffer.flip();

                    if (this.buffer.hasRemaining() || this.patchBuf.hasRemaining()) {
                        count += fillTicks(HistoryUtil.patchIt(this.buffer,
                                        reader.getInterval(), this.patchBuf, MutableTickEntry.class,
                                        GENESIS),
                                record, req.getInterval(), req.getMinTickNum() - count,
                                req.isAlignWithStart(), GENESIS
                        );
                    }
                    if (req.getMinTickNum() > 0 && count >= req.getMinTickNum()) {
                        return count;
                    }
                }
            }
        }

        return count;
    }

    @Override
    protected HistoryReader<ByteString> getPatchReader() {
        return this.delegate.getPatchReader();
    }

    @ManagedOperation(description = "invoke to get storage statistic for a given symbol ")
    @ManagedOperationParameters(
            @ManagedOperationParameter(name = "symbol", description = "a VWD symbol")
    )
    @Override
    public String statistic(String symbol) {
        final TimeTaker tt = new TimeTaker();
        final ByteString key = HistoryUtil.getKey(new ByteString(symbol.toUpperCase()));
        final List<HistoryReader<ByteString>> yearReaders = this.delegate.getYearReaders();
        final HistoryReader<ByteString> monthReader = this.delegate.getMonthReader();

        try {
            synchronized (this.buffer) {
                this.buffer.clear();
                monthReader.loadData(key, this.buffer);
                for (HistoryReader<ByteString> yearReader : yearReaders) {
                    yearReader.loadData(key, this.buffer);
                }
                this.buffer.flip();

                loadPatchedEntries(key);

                final Iterable<MutableTickEntry> it =
                        HistoryUtil.patchIt(this.buffer, calcInterval(), this.patchBuf,
                                MutableTickEntry.class, GENESIS);
                final StringBuilder sb = new StringBuilder();
                int count = 0;
                int bytes = 0;
                for (MutableTickEntry tickEntry : it) {
                    count += tickEntry.getTickNum();
                    bytes += tickEntry.getData().length;
                    sb.append("\n  ").append(HistoryUtil.daysFromBegin2Date(GENESIS, tickEntry.getDays())).append(" ")
                            .append(tickEntry.getTickNum()).append(" ").append(tickEntry.getData().length)
                            .append(" bytes");
                }

                sb.insert(0, symbol + "#" + getTickType() + " in: " + tt + "\n" + count + " ticks, " + bytes + " bytes");
                sb.insert(0, "History end: " + DateUtil.toYyyyMmDd(getHistoryEnd()) + "\n");
                return sb.toString();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private Interval calcInterval() {
        final Interval mi = this.delegate.getMonthReader().getInterval();
        final List<HistoryReader<ByteString>> yearReaders = this.delegate.getYearReaders();
        final Interval yi = yearReaders.isEmpty() ?
                null : yearReaders.get(yearReaders.size() - 1).getInterval();
        if (null != mi && null != yi) {
            return yi.withEnd(mi.getEnd());
        }
        else if (null == yi) {
            return mi;
        }
        else {
            return yi;
        }
    }

    @Override
    public void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException {
        final TimeTaker tt = new TimeTaker();
        synchronized (this.buffer) {
            this.delegate.updateUnits(dir, units);
        }
        this.logger.info("<updateUnits> took: " + tt);
    }
}
