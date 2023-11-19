/*
 * ChartUtil.java
 *
 * Created on 10.10.2006 08:33:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Interval;
import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.chart.data.TimeSeries;
import de.marketmaker.istar.chart.data.TimeSeriesFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class ChartUtil {

    enum Aggregation { DAILY, WEEKLY, MONTHLY }

    enum Consolidation { FIRST, MIN, MAX, LAST, SUM }

    private static class Aggregate {
        double value = Double.NaN;
        long start;
        long end;

        public Aggregate(long start, long end) {
            this.start = start;
            this.end = end;
        }

        void add(Consolidation c, double in) {
            if (Double.isNaN(in)) {
                return;
            }
            switch(c) {
                case FIRST:
                    value = Double.isNaN(value) ? in : value;
                    break;
                case LAST:
                    value = in;
                    break;
                case MAX:
                    value = Double.isNaN(value) ? in : Math.max(value, in);
                    break;
                case MIN:
                    value = Double.isNaN(value) ? in : Math.min(value, in);
                    break;
                case SUM:
                    value = Double.isNaN(value) ? in : value + in;
                    break;
                default:
                    // empty;
            }
        }
    }

    static Interval getInterval(TimeSeries ts) {
        if (ts == null) {
            return null;
        }
        DateTime start = null;
        for (int i = 0, n = ts.size(); i < n; i++) {
            if (Double.isNaN(ts.getItem(i).getY().doubleValue())) {
                continue;
            }
            start = ts.getItem(i).getInterval().getStart();
            break;
        }
        if (start == null) {
            return null;
        }
        DateTime end = null;
        for (int i = ts.size(); i-- > 0; ) {
            if (Double.isNaN(ts.getItem(i).getY().doubleValue())) {
                continue;
            }
            end = ts.getItem(i).getInterval().getStart();
            break;
        }
        return new Interval(start, end);
    }

    static TimeSeries toTimeSeries(HistoricTimeseries ht, String id, String label,
            Aggregation agg, Consolidation cons) {
        if (agg == Aggregation.DAILY) {
            return TimeSeriesFactory.daily(id, label, ht.getValues(), ht.getStartDay());
        }

        final List<Aggregate> as = new ArrayList<>(ht.size() / 7);

        int n = 0;
        final DateTime end = ht.getStartDay().plusDays(ht.size()).toDateTimeAtStartOfDay();

        DateTime from = ht.getStartDay().toDateTimeAtStartOfDay();
        DateTime to;
        int next;
        if (agg == Aggregation.MONTHLY) {
            to = from.plusMonths(1).property(DateTimeFieldType.dayOfMonth()).withMinimumValue();
            next = new Period(from, to, PeriodType.days()).getDays();
        }
        else {
            next = DateTimeConstants.SUNDAY - from.getDayOfWeek() + 1;
            to = from.plusDays(next);
        }

        while (n < ht.size()) {
            Aggregate a = new Aggregate(from.getMillis(), to.getMillis());

            for (int j = n; j < next; j++) {
                a.add(cons, ht.getValue(j));
            }
            if (!Double.isNaN(a.value)) {
                as.add(a);
            }
            n = next;
            from = to;
            to = (agg == Aggregation.MONTHLY) ? to.plusMonths(1) : to.plusWeeks(1);
            if (to.isAfter(end)) {
                to = end;
            }
            next = Math.min(ht.size(), next + new Period(from, to, PeriodType.days()).getDays());
        }

        return toTimeSeries(id, label, as);
    }

    private static TimeSeries toTimeSeries(String id, String label, List<Aggregate> a) {
        final double[] values = new double[a.size()];
        final long[][] millis = new long[2][values.length];
        for (int i = 0; i < values.length; i++) {
            final Aggregate agg = a.get(i);
            values[i] = agg.value;
            millis[0][i] = agg.start;
            millis[1][i] = agg.end;
        }
        return TimeSeriesFactory.simple(id, label, values, millis);
    }

    public static int size(Timeseries<AggregatedTick> ts) {
        int result = 0;
        for (DataWithInterval<AggregatedTick> dataWithInterval : ts) {
            result++;
        }
        return result;
    }

}
