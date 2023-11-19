/*
 * MscTickData.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickList;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.domainimpl.data.DataWithIntervalImpl;
import de.marketmaker.istar.domainimpl.protobuf.OhlcvTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.TickEventTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.TickTimeseriesSerializer;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import de.marketmaker.istar.merger.web.easytrade.util.IntradayUtil;

import static de.marketmaker.istar.domain.data.TickImpl.Type.BID_ASK;
import static de.marketmaker.istar.domain.data.TickImpl.Type.BID_ASK_TRADE;

/**
 * Returns intraday tick data, a tick may be a trade, an ask, or a bid.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscTickData extends AbstractTicksBlock {

    private static final int MAXIMUM_NUMBER_OF_TRADES = 5000;

    private static final int PROTOBUF_FACTOR = 20;

    private static final int MAXIMUM_NUMBER_OF_DAYS = 7;

    private static final Duration DURATION_ONE_DAY = Duration.standardDays(1);

    public static class Command extends TickDataCommand {
        private int numTrades;

        private int cacheTTL;

        private boolean alignWithStart = false;

        private boolean timeAsMillis = false;

        /**
         * @return whether time values in protobuf datastructures and tick-by-tick xml output
         * are given as milliseconds or seconds; default is <tt>false</tt>
         */
        public boolean isTimeAsMillis() {
            return timeAsMillis;
        }

        public void setTimeAsMillis(boolean timeAsMillis) {
            this.timeAsMillis = timeAsMillis;
        }

        /**
         * requested number of ticks; for xml, no more than 5000 ticks will be returned,
         * for protobuf, no more than 100000.
         */
        @Range(min = 0, max = 999999) // do not change, mmf actually uses values > 100000...
        public int getNumTrades() {
            return numTrades;
        }

        public void setNumTrades(int numTrades) {
            this.numTrades = numTrades;
        }

        @MmInternal
        public int getCacheTTL() {
            return this.cacheTTL;
        }

        public void setCacheTTL(int cacheTTL) {
            this.cacheTTL = cacheTTL;
        }

        /**
         * If this returns true, the ticks returned will be aligned with the value of
         * {@link #getStart()}, i.e., the first {@link #getNumTrades()} ticks between start and end will be
         * returned; if these ticks exceed the maximum number of allowed ticks, the ticks will be
         * truncated and the result model will contain a date value for the key "nextRequestStart"
         * that describes how the succeeding ticks can be requested (by setting <tt>start</tt>
         * in the next request to that value).<p>
         * If this returns false (default), the ticks returned will be aligned with the value of
         * {@link #getEnd()}, i.e., the last {@link #getNumTrades()} ticks between start and end will be
         * returned; if the number of ticks exceeds maximum, the result model will contain a date
         * value for the key "nextRequestEnd" that describes how the previous ticks can be requested.
         * <p>
         * To avoid overlapping ticks in multiple responses with <tt>nextRequestStart/End</tt>, the
         * response will almost always contain all ticks for all seconds that are part of the response.
         * The only exception is for <tt>numTrades=1</tt>, which will never return more than one tick.
         * @return true iff align with start
         */
        public boolean isAlignWithStart() {
            return alignWithStart;
        }

        public void setAlignWithStart(boolean alignWithStart) {
            this.alignWithStart = alignWithStart;
        }

        /**
         * {@inheritDoc}
         * <p>
         * The type of the returned ticks. If <tt>aggregation</tt> is undefined, this
         * value is ignored and TICK is used, otherwise the valid values are CLOSE, OHLC, and OHLCV
         * </p>
         */
        @Override
        @RestrictedSet("TICK,CLOSE,OHLC,OHLCV")
        public ElementDataType getType() {
            return super.getType();
        }
    }

    protected MscTickData(Class aClass) {
        super(aClass);
    }

    public MscTickData() {
        super(Command.class);
    }

    @Override
    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        EnumEditor.register(TickDataCommand.Format.class, binder);
        EnumEditor.register(TickDataCommand.ElementDataType.class, binder);
        EnumEditor.register(TickImpl.Type.class, binder);
    }

    protected void validateCommand(Command cmd) {
        final int days = new Period(cmd.getStart(), cmd.getEnd(), PeriodType.days()).getDays();
        if (days > getMaximumDays(cmd)) {
            throw new BadRequestException("too many days from start to end (allowed: "
                    + getMaximumDays(cmd) + "): " + days);
        }
        if (cmd.isWithAggregation()) {
            Duration d = cmd.getAggregation().toStandardDuration();
            if (!AbstractTickRecord.isIntradayAggregationDuration(d)) {
                throw new BadRequestException("invalid aggregation: " + cmd.getAggregation());
            }
        }
    }

    private int getMaximumDays(Command cmd) {
        if (IntradayUtil.canUseTickHistory(cmd.getDuration(), cmd.getTickTypeChicago())) {
            return 366;
        }
        return MAXIMUM_NUMBER_OF_DAYS * (!cmd.isWithAggregation() ? 1 : 2)
                * (cmd.getFormat() == TickDataCommand.Format.PROTOBUF ? 2 : 1);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws IOException {

        final Command cmd = (Command) o;
        validateCommand(cmd);

        final Quote quote = getQuote(cmd);
        checkAuthorization(cmd, quote);

        return new ModelAndView(getTemplateName(cmd.isWithAggregation()), createModel(cmd, quote));
    }

    protected Map<String, Object> createModel(Command cmd, Quote quote) throws IOException {
        final BitSet allowedFields = getAllowedFields(quote);
        checkTickType(cmd, quote, allowedFields);
        final TickList.FieldPermissions permissions = createPermissions(allowedFields);

        cmd.setEnd(adaptEnd(quote, cmd.getEnd()));

        final TickImpl.Type tickType = cmd.getTickType();

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("alignWithStart", cmd.isAlignWithStart());
        model.put("tickType", tickType);

        if (cmd.isWithAggregation()) {
            model.put("type", cmd.getType());
            model.put("aggregated", true);
            model.put("aggregation", cmd.getAggregation());

            if (cmd.isAlignWithStart()) {
                addAggregatedTradesAlignedWithStart(cmd, quote, model, permissions);
            }
            else {
                addAggregatedTradesAlignedWithEnd(cmd, quote, model, permissions);
            }

            return model;
        }
        else {
            model.put("type", TickDataCommand.ElementDataType.TICK);
            if (tickType == BID_ASK_TRADE) {
                model.put("bidasktrade", Boolean.TRUE);
            }
            else if (tickType == BID_ASK) {
                model.put("bidask", Boolean.TRUE);
            }
            model.put("aggregated", false);
            model.put("aggregation", "PT0S");
            addTrades(cmd, quote, model, allowedFields, permissions);
            return model;
        }
    }

    void checkTickType(Command cmd, Quote q, BitSet allowedFields) {
        switch (cmd.getTickType()) {
            case TRADE:
                checkFields(q, true, allowedFields, VwdFieldDescription.ADF_Bezahlt);
                break;
            case BID:
                checkFields(q, true, allowedFields, VwdFieldDescription.ADF_Geld);
                break;
            case ASK:
                checkFields(q, true, allowedFields, VwdFieldDescription.ADF_Brief);
                break;
            case BID_ASK:
                checkFields(q, false, allowedFields, VwdFieldDescription.ADF_Geld,
                        VwdFieldDescription.ADF_Brief);
                break;
            case BID_ASK_TRADE:
                checkFields(q, false, allowedFields, VwdFieldDescription.ADF_Geld,
                        VwdFieldDescription.ADF_Brief, VwdFieldDescription.ADF_Bezahlt);
                break;
            case SYNTHETIC_TRADE:
                checkFields(q, true, allowedFields, VwdFieldDescription.ADF_Geld,
                        VwdFieldDescription.ADF_Brief);
        }
    }

    void putEmpty(Map<String, Object> model, TickDataCommand.Format format) {
        switch (format) {
            case XML:
                model.put("trades", Collections.<TickImpl>emptyList());
                break;
            case PROTOBUF:
                model.put("numProtobufObjects", 0);
                model.put("numProtobufBytes", 0);
                model.put("numProtobufBytesBase64", 0);
                model.put("protobufBytesBase64", null);
                break;
        }
    }

    private void addAggregatedTrades(Command cmd, List<AggregatedTickImpl> trades,
            Map<String, Object> model) throws IOException {
        switch (cmd.getFormat()) {
            case XML:
                model.put("trades", trades);
                break;
            case PROTOBUF:
                addToModel(model, trades, cmd.getType());
                break;
        }
    }

    private void addAggregatedTradesAlignedWithEnd(Command cmd,
            Quote quote,
            Map<String, Object> model,
            TickList.FieldPermissions permissions)
            throws IOException {

        final List<Interval> intervals = cmd.getTickIntervals();
        if (intervals.isEmpty()) {
            putEmpty(model, cmd.getFormat());
            return;
        }

        // iterate in reverse order so we do not request ticks for days that will be ignored anyway
        Collections.reverse(intervals);

        final TickDataCommand.Format format = cmd.getFormat();
        final int maximumNumberOfTrades = getMaximumNumberOfTicks(format);
        final int numTrades = cmd.getNumTrades() > 0 && cmd.getNumTrades() < maximumNumberOfTrades
                ? cmd.getNumTrades() : maximumNumberOfTrades;
        final Duration duration = cmd.getAggregation().toStandardDuration();

        final List<AggregatedTickImpl> result = new ArrayList<>(numTrades);

        LOOP:
        for (Interval interval : intervals) {
            final List<AggregatedTickImpl> tickList = this.intradayProvider.getAggregatedTrades(
                    quote, interval, duration, cmd.getTickTypeChicago(), 1 + numTrades - result.size(), false);
            // iterate in reverse order as we do not know how many ticks we're going to find
            for (int j = tickList.size(); j-- > 0; ) {
                result.add(TickList.applyPermissions(tickList.get(j), permissions));
                if (result.size() > numTrades) {
                    break LOOP;
                }
            }
        }
        if (result.size() > numTrades) {
            final AggregatedTickImpl previous = result.remove(result.size() - 1);
            if (result.size() == maximumNumberOfTrades) {
                model.put("nextRequestEnd", previous.getInterval().getEnd());
            }
        }
        // since we added ticks in reverse order, reverse result to be in order again
        Collections.reverse(result);
        addAggregatedTrades(cmd, result, model);
    }

    private void addAggregatedTradesAlignedWithStart(Command cmd,
            Quote quote,
            Map<String, Object> model,
            TickList.FieldPermissions permissions)
            throws IOException {
        final TickDataCommand.Format format = cmd.getFormat();
        final int maximumNumberOfTrades = getMaximumNumberOfTicks(format);
        final int numTrades = cmd.getNumTrades() > 0 && cmd.getNumTrades() < maximumNumberOfTrades
                ? cmd.getNumTrades() : maximumNumberOfTrades;
        final Duration duration = cmd.getAggregation().toStandardDuration();

        final List<Interval> intervals = cmd.getTickIntervals();
        if (intervals.isEmpty()) {
            putEmpty(model, cmd.getFormat());
            return;
        }

        final List<AggregatedTickImpl> result = new ArrayList<>(numTrades + 1);

        for (Interval interval : intervals) {
            final List<AggregatedTickImpl> tickList = this.intradayProvider.getAggregatedTrades(
                    quote, interval, duration, cmd.getTickTypeChicago(), 1 + numTrades - result.size(), true);
            // iterate in reverse order as we do not know how many ticks we're going to find
            int toAdd = Math.min(1 + numTrades - result.size(), tickList.size());
            result.addAll(TickList.applyPermissions(tickList.subList(0, toAdd), permissions));
            if (result.size() > numTrades) {
                break;
            }
        }
        if (result.size() > numTrades) {
            final AggregatedTickImpl next = result.remove(result.size() - 1);
            if (result.size() == maximumNumberOfTrades) {
                model.put("nextRequestStart", next.getInterval().getStart());
            }
        }
        addAggregatedTrades(cmd, result, model);
    }

    static void addToModel(Map<String, Object> model,
            List<AggregatedTickImpl> trades,
            TickDataCommand.ElementDataType type) throws IOException {
        final OhlcvTimeseriesSerializer serializer = new OhlcvTimeseriesSerializer();
        final ByteBuffer bytes = serializer.serialize(trades, withOpenHighLow(type), withVolume(type));
        addToModel(model, bytes, serializer.getNumObjects());
    }

    private static boolean withVolume(TickDataCommand.ElementDataType type) {
        return type == TickDataCommand.ElementDataType.OHLCV;
    }

    private static boolean withOpenHighLow(TickDataCommand.ElementDataType type) {
        return type == TickDataCommand.ElementDataType.OHLC || withVolume(type);
    }

    TickRecord getTickRecord(Command cmd, Quote quote, Interval interval) {
        if (cmd.getNumTrades() < 0) {
            return null; // MSC_TickData.fixed.numTrades=-1 effectively disables this atom
        }
        final List<IntradayData> datas = this.intradayProvider.getIntradayData(
                Collections.singletonList(quote), interval, cmd.getCacheTTL());
        return datas.get(0).getTicks();
    }

    int getMaximumNumberOfTicks(TickDataCommand.Format format) {
        return format == TickDataCommand.Format.PROTOBUF
                ? MAXIMUM_NUMBER_OF_TRADES * PROTOBUF_FACTOR
                : MAXIMUM_NUMBER_OF_TRADES;
    }

    private void addTrades(Command cmd,
            Quote quote,
            Map<String, Object> model,
            BitSet allowedFields,
            TickList.FieldPermissions permissions) throws IOException {

        final BitSet requestedFields = fieldsAsBitSet(cmd, allowedFields, quote);
        final TickImpl.Type tickType = cmd.getTickType();
        if (requestedFields == null && tickType == TickImpl.Type.ADDITIONAL_FIELDS) {
            putEmpty(model, cmd.getFormat());
            return;
        }
        final List<Interval> intervals = cmd.getTickIntervals();
        if (intervals.isEmpty()) {
            putEmpty(model, cmd.getFormat());
            return;
        }

        final int maximumNumberOfTrades = getMaximumNumberOfTicks(cmd.getFormat());
        final int numTrades = cmd.getNumTrades() > 0 && cmd.getNumTrades() < maximumNumberOfTrades
                ? cmd.getNumTrades() : maximumNumberOfTrades;

        final boolean alignWithStart = cmd.isAlignWithStart();
        if (!alignWithStart) {
            // align with end, start with latest interval
            Collections.reverse(intervals);
        }

        if (numTrades == 1) {
            addTickToModel(cmd, quote, intervals, requestedFields, permissions, model);
            return;
        }

        int count = 0;
        TickRecord tr = null;
        Interval interval = null;
        final Iterator<Interval> it = intervals.iterator();

        // request ticks day by day to avoid loading many days of ticks only to discard most
        while (it.hasNext() && count < numTrades) {
            final Interval day = it.next();
            final TickRecord tmp = getTickRecord(cmd, quote, day);
            if (tmp == null) {
                continue;
            }

            final Timeseries<TickEvent> timeseries = (requestedFields != null)
                    ? tmp.getTimeseriesWithAdditionalFields(day)
                    : tmp.getTimeseries(day);
            final int n = getCount(timeseries, tickType, requestedFields, permissions);
            if (n > 0) {
                count += n;
                if (tr == null) {
                    tr = tmp;
                    interval = day;
                }
                else {
                    tr = tr.merge(tmp);
                    interval = alignWithStart
                            ? interval.withEnd(day.getEnd())
                            : interval.withStart(day.getStart());
                }
            }
        }

        if (tr == null) {
            putEmpty(model, cmd.getFormat());
            return;
        }

        final Timeseries<TickEvent> timeseries = (requestedFields != null)
                ? tr.getTimeseriesWithAdditionalFields(interval)
                : tr.getTimeseries(interval);

        final boolean mayNeedNextRequest
                = cmd.getNumTrades() == 0 || cmd.getNumTrades() > maximumNumberOfTrades;

        TickList ticks;
        if (alignWithStart) {
            DateTime end = getEndDate(numTrades, timeseries, tickType, requestedFields, permissions);
            if (end != null && mayNeedNextRequest) {
                model.put("nextRequestStart", end.plusSeconds(1).withMillisOfSecond(0));
            }
            ticks = TickList.withEnd(timeseries, tickType, end);
        }
        else {
            final int numToSkip = count - numTrades;
            final DateTime startDate;
            if (numToSkip > 0) {
                // we have more ticks available than requested: to be able to return the last
                // numTrades ticks, we have to skip the first numToSkip ticks, but still want all
                // those that start in the same second. Problem: some ticks that arrived later may
                // have a tick timestamp that is before startDate and would not make it into the
                // ticks list. // TODO: figure out how to handle this w/o having to sort all ticks
                // and without having to instantiate all ticks beforehand.
                startDate = getStartDate(numToSkip, timeseries, tickType, requestedFields, permissions).withMillisOfSecond(0);
                if (mayNeedNextRequest) {
                    model.put("nextRequestEnd", startDate.minusSeconds(1));
                }
            }
            else {
                // hack to fix R-75288 (most of the time): if numTrades is unspecified or less than
                // maximumNumberOfTrades, do not use tick times at all, just use the interval
                startDate = interval.getStart();
            }
            ticks = TickList.withStart(timeseries, tickType, startDate);
        }

        ticks = ticks.withAdditionalFields(requestedFields).withPermissions(permissions);

        addTicksToModel(cmd, ticks, model);
    }

    private void addTickToModel(Command cmd, Quote quote, List<Interval> intervals,
            BitSet fields, TickList.FieldPermissions permissions,
            Map<String, Object> model) throws IOException {
        DataWithInterval<TickEvent> event = null;
        for (Interval day : intervals) {
            final TickRecord tmp = getTickRecord(cmd, quote, day);
            if (tmp == null) {
                continue;
            }

            final Timeseries<TickEvent> timeseries = (fields != null)
                    ? tmp.getTimeseriesWithAdditionalFields(day)
                    : tmp.getTimeseries(day);
            if (cmd.isAlignWithStart()) {
                event = getFirst(timeseries, cmd.getTickType(), fields, permissions);
            }
            else {
                event = getLast(timeseries, cmd.getTickType(), fields, permissions);
            }
            if (event != null) {
                break;
            }
        }
        if (event == null) {
            putEmpty(model, cmd.getFormat());
            return;
        }
        TickList ticks;
        if (cmd.isAlignWithStart()) {
            ticks = TickList.withStart(Collections.singletonList(event), cmd.getTickType(), event.getInterval().getStart());
        }
        else {
            ticks = TickList.withEnd(Collections.singletonList(event), cmd.getTickType(), event.getInterval().getStart());
        }
        ticks = ticks.withAdditionalFields(fields).withPermissions(permissions);

        addTicksToModel(cmd, ticks, model);
    }

    private void addTicksToModel(Command cmd, Iterable<TickImpl> ticks, Map<String, Object> model)
            throws IOException {
        if (cmd.getFormat() == TickDataCommand.Format.XML) {
            model.put("trades", ticks);
            model.put("timeFormat", cmd.isTimeAsMillis() ? "ms" : "");
        }
        else if (cmd.getFormat() == TickDataCommand.Format.PROTOBUF) {
            if (cmd.getTickType() == BID_ASK_TRADE || cmd.getTickType() == BID_ASK) {
                final TickEventTimeseriesSerializer serializer
                        = new TickEventTimeseriesSerializer(cmd.isTimeAsMillis(), cmd.getTickType() == BID_ASK_TRADE);
                final ByteBuffer bytes = serializer.serialize(ticks);
                addToModel(model, bytes, serializer.getNumObjects());
            }
            else {
                final TickTimeseriesSerializer serializer
                        = new TickTimeseriesSerializer(cmd.isTimeAsMillis());
                final ByteBuffer bytes = serializer.serialize(ticks);
                addToModel(model, bytes, serializer.getNumObjects());
            }

        }
    }

    protected BitSet fieldsAsBitSet(Command cmd, BitSet allowedFields, Quote quote) {
        return null;
    }

    static void addToModel(Map<String, Object> model, ByteBuffer bytes, int numObjects) throws IOException {
        model.put("numProtobufObjects", numObjects);
        model.put("numProtobufBytes", bytes.remaining());
        final String bytesBase64 = ByteUtil.toBase64(bytes, true);
        model.put("numProtobufBytesBase64", bytesBase64.length());
        model.put("protobufBytesBase64", bytesBase64);
    }

    int getCount(Timeseries<TickEvent> timeseries, TickImpl.Type tickType, BitSet fields,
            TickList.FieldPermissions permissions) {
        int count = 0;
        for (final DataWithInterval<TickEvent> dwi : timeseries) {
            if (TickImpl.hasTickType(tickType, permissions, dwi.getData(), fields)) {
                count++;
            }
        }
        return count;
    }

    private DataWithInterval<TickEvent> getFirst(Timeseries<TickEvent> timeseries,
            TickImpl.Type tickType, BitSet fields, TickList.FieldPermissions permissions) {
        for (final DataWithInterval<TickEvent> dwi : timeseries) {
            if (TickImpl.hasTickType(tickType, permissions, dwi.getData(), fields)) {
                return dwi;
            }
        }
        return null;
    }

    private DataWithInterval<TickEvent> getLast(Timeseries<TickEvent> timeseries,
            TickImpl.Type tickType, BitSet fields, TickList.FieldPermissions permissions) {
        DataWithInterval<TickEvent> last = null;
        for (final DataWithInterval<TickEvent> dwi : timeseries) {
            if (TickImpl.hasTickType(tickType, permissions, dwi.getData(), fields)) {
                try {
                    last = new DataWithIntervalImpl<>((TickEvent) ((RawTick) dwi.getData()).clone(), dwi.getInterval());
                } catch (CloneNotSupportedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return last;
    }

    DateTime getStartDate(int start, Timeseries<TickEvent> timeseries,
            TickImpl.Type tickType, BitSet fields, TickList.FieldPermissions permissions) {
        // find start second to deliver all ticks in this second independent from MAXIMUM_NUMBER_OF_TRADES
        int count = 0;
        for (final DataWithInterval<TickEvent> dwi : timeseries) {
            if (!TickImpl.hasTickType(tickType, permissions, dwi.getData(), fields)) {
                continue;
            }

            if (count++ == start) {
                return dwi.getInterval().getStart();
            }
        }
        return null;
    }

    DateTime getEndDate(int max, Timeseries<TickEvent> timeseries, TickImpl.Type tickType,
            BitSet fields, TickList.FieldPermissions permissions) {
        // find end second to deliver all ticks in this second independent from MAXIMUM_NUMBER_OF_TRADES
        int count = 0;
        DateTime dt = null;
        for (final DataWithInterval<TickEvent> dwi : timeseries) {
            if (!TickImpl.hasTickType(tickType, permissions, dwi.getData(), fields)) {
                continue;
            }
            ++count;
            if (count == max) {
                dt = dwi.getInterval().getStart();
            }
            else if (count > max) {
                if (dwi.getInterval().getStart().isAfter(dt)) {
                    return dt;
                }
            }
        }
        return null;
    }

    protected String getTemplateName(boolean aggregated) {
        return "msctickdata";
    }
}
