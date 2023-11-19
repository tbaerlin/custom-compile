package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickList;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.domainimpl.AggregatedTickEvent;
import de.marketmaker.istar.domainimpl.AggregatedTickEventIterable;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import de.marketmaker.istar.merger.web.easytrade.util.IntradayUtil;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Only for zones IM and VNS. Provides tick aggregations by number.
 */
public class MscNumTicks extends MscTickData {

    public static class Command extends MscTickData.Command {

        private int numTicks;

        /**
         * Number of ticks to aggregate.
         */
        @RestrictedSet("1,5,10,20,50,100")
        public int getNumTicks() {
            return numTicks;
        }

        public void setNumTicks(int numTicks) {
            this.numTicks = numTicks;
        }

        /**
         * Disabled for {@link MscNumTicks}
         */
        @MmInternal
        public Period getAggregation() {
            return null;
        }
    }

    private final String templateName = "mscticks";

    public MscNumTicks() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws IOException {
        final Command cmd = (Command) o;
        validateCommand(cmd);

        final Quote quote = getQuote(cmd);
        checkAuthorization(cmd, quote);

        return new ModelAndView(this.templateName, createModel(cmd, quote));
    }

    protected Map<String, Object> createModel(Command cmd, Quote quote) throws IOException {
        final BitSet allowedFields = getAllowedFields(quote);
        checkTickType(cmd, quote, allowedFields);
        final TickList.FieldPermissions permissions = createPermissions(allowedFields);

        cmd.setEnd(adaptEnd(quote, cmd.getEnd()));

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("alignWithStart", cmd.isAlignWithStart());
        model.put("tickType", cmd.getTickType());
        model.put("type", cmd.getType());
        model.put("aggregated", cmd.getNumTicks() > 0);
        model.put("aggregation", cmd.getNumTicks());

        final List<Interval> intervals = cmd.getTickIntervals();
        if (intervals.isEmpty()) {
            putEmpty(model, cmd.getFormat());
        } else {
            // iterate in reverse order so we do not request ticks for days that will be ignored anyway
            if (!cmd.isAlignWithStart()) {
                Collections.reverse(intervals);
            }
            addTrades(cmd, quote, model, getAllowedFields(quote), permissions);
        }

        return model;
    }

    void addTrades(Command cmd,
                   Quote quote,
                   Map<String, Object> model,
                   BitSet allowedFields,
                   TickList.FieldPermissions permissions) throws IOException {

        final BitSet requestedFields = fieldsAsBitSet(cmd, allowedFields, quote);
        final TickImpl.Type tickType = cmd.getTickType();

        /**
         * Only allowed for tick history compatible types:
         * {@link IntradayUtil#ALLOWED_TICK_TYPES}
         */
        if (requestedFields == null && tickType == TickImpl.Type.ADDITIONAL_FIELDS) {
            putEmpty(model, cmd.getFormat());
            return;
        }

        /**
         * (Daily) intervals to be requested.
         */
        final List<Interval> intervals = cmd.getTickIntervals();
        if (intervals.isEmpty()) {
            putEmpty(model, cmd.getFormat());
            return;
        }

        /**
         * Trades to request
         */
        final int maximumNumberOfTrades = getMaximumNumberOfTicks(cmd.getFormat());
        final int numTrades = cmd.getNumTrades() > 0 && cmd.getNumTrades() < maximumNumberOfTrades
                ? cmd.getNumTrades() : maximumNumberOfTrades;

        final boolean alignWithStart = cmd.isAlignWithStart();
        if (!alignWithStart) {
            Collections.reverse(intervals);
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

        final boolean mayNeedNextRequest = cmd.getNumTrades() == 0 || cmd.getNumTrades() > maximumNumberOfTrades;

        Iterable<AggregatedTickEvent> ticks;

        if (alignWithStart) {
            DateTime end = getEndDate(numTrades, timeseries, tickType, requestedFields, permissions);
            if (end != null && mayNeedNextRequest) {
                model.put("nextRequestStart", end.plusSeconds(1).withMillisOfSecond(0));
            }

            ticks = AggregatedTickEventIterable.withEnd(timeseries, tickType, cmd.getNumTicks(), end);
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
            ticks = AggregatedTickEventIterable.withStart(timeseries, tickType, cmd.getNumTicks(), startDate);
        }

        addTicksToModel(cmd, ticks, model);
    }

    private void addTicksToModel(MscTickData.Command cmd, Iterable<AggregatedTickEvent> ticks, Map<String, Object> model)
            throws IOException {
        if (cmd.getFormat() == TickDataCommand.Format.XML) {
            model.put("trades", ticks);
            model.put("timeFormat", cmd.isTimeAsMillis() ? "ms" : "");
        }
        else if (cmd.getFormat() == TickDataCommand.Format.PROTOBUF) {
            throw new BadRequestException("PROTOBUF format not supported");
        }
    }
}
