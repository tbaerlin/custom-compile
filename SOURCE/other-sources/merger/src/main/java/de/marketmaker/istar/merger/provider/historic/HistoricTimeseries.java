/*
 * HistoricTimeseries.java
 *
 * Created on 31.08.2006 15:59:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.Serializable;
import java.util.Arrays;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.merger.Constants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
public class HistoricTimeseries implements Serializable {
    static final long serialVersionUID = -7250843217942178290L;

    private final double[] values;

    private final LocalDate startDay;

    /**
     * If not NaN, the value that was valid before startDay
     */
    private double previousValue = Double.NaN;

    /**
     * If not null, the date of previousValue;
     */
    private LocalDate previousDay;

    public HistoricTimeseries(double[] values, LocalDate startDay) {
        this.values = values;
        this.startDay = startDay;
    }

    public int size() {
        return this.values.length;
    }

    public double getValue(int i) {
        if (i < 0 || i >= size()) {
            return Double.NaN;
        }
        return this.values[i];
    }

    public double[] getValues() {
        return this.values;
    }

    public double getValue(LocalDate ld) {
        final int offset = getOffset(ld);
        return (offset != -1) ? this.values[offset] : Double.NaN;
    }

    public void setPreviousValue(double previousValue, LocalDate previousDay) {
        this.previousValue = previousValue;
        this.previousDay = previousDay;
    }

    public boolean hasPreviousValue() {
        return !Double.isNaN(this.previousValue) && this.previousDay != null;
    }

    public double getPreviousValue() {
        return this.previousValue;
    }

    public LocalDate getPreviousDay() {
        return this.previousDay;
    }

    /**
     * Tries to find a defined (not NaN) value at date or around date. first, the value at date is
     * evaluated, then the values for days prior to date, and finally the days after date; whenever
     * a defined value is found it will be returned.
     *
     * @param date day for which value is needed
     * @return value for day or NaN if none can be found
     */
    public double getValueAtOrBeforeOrAfter(LocalDate date) {
        if (date == null) {
            return Double.NaN;
        }
        final int offset = getOffsetOrLast(date);
        for (int i = offset; i >= 0; i--) {
            if (!Double.isNaN(this.values[i])) {
                return this.values[i];
            }
        }
        if (hasPreviousValue() && this.previousDay.isBefore(date)) {
            return this.previousValue;
        }
        if (offset > 0) { // try 'after' values
            for (int i = offset; i < size(); i++) {
                if (!Double.isNaN(this.values[i])) {
                    return this.values[i];
                }
            }
        }
        return Double.NaN;
    }

    private int getOffsetOrLast(LocalDate date) {
        int offset = getOffset(date);
        if (offset < 0 && date.isAfter(this.startDay)) {
            return this.values.length - 1;
        }
        return offset;
    }

    public int getOffset(LocalDate ld) {
        if (ld.isBefore(this.startDay)) {
            return -1;
        }
        final int offset = DateUtil.daysBetween(this.startDay, ld);
        return (offset >= 0 && offset < size()) ? offset : -1;
    }

    public LocalDate getStartDay() {
        return this.startDay;
    }

    /**
     * Returns a HistoricTimeseries that spans exactly the same interval as ht. Values not defined
     * in the original timeseries will be NaN in the aligned timeseries.
     *
     * @param ht reference timeseries
     * @return aligned timeseries
     */
    public HistoricTimeseries alignTo(HistoricTimeseries ht) {
        if (isAlignedWith(ht)) {
            return this;
        }

        final double[] tmp = ht.createEmptyValues();

        int srcPos = 0;
        int destPos = 0;
        if (this.startDay.isBefore(ht.startDay)) {
            srcPos = DateUtil.daysBetween(this.startDay, ht.startDay);
        }
        else if (this.startDay.isAfter(ht.startDay)) {
            destPos = DateUtil.daysBetween(ht.startDay, this.startDay);
        }
        final int length = Math.min(this.values.length - srcPos, ht.values.length - destPos);
        if (length < 0) { // values do not overlap
            return new HistoricTimeseries(tmp, ht.startDay);
        }

        System.arraycopy(this.values, srcPos, tmp, destPos, length);
        return new HistoricTimeseries(tmp, ht.startDay);
    }

    private boolean isAlignedWith(HistoricTimeseries ht) {
        return this.getStartDay().equals(ht.getStartDay()) && size() == ht.size();
    }

    public HistoricTimeseries multiply(double d) {
        final double[] tmp = new double[this.values.length];
        for (int n = this.values.length; n-- != 0; ) {
            tmp[n] = Double.isNaN(this.values[n]) ? Double.NaN : this.values[n] * d;
        }

        final HistoricTimeseries result = new HistoricTimeseries(tmp, this.startDay);
        if (this.hasPreviousValue()) {
            result.setPreviousValue(this.previousValue * d, this.previousDay);
        }
        return result;
    }

    private double[] createEmptyValues() {
        final double[] result = new double[this.values.length];
        Arrays.fill(result, Double.NaN);
        return result;
    }

    public HistoricTimeseries add(HistoricTimeseries ht) {
        final HistoricTimeseries source = isAlignedWith(ht) ? ht : ht.alignTo(this);
        final double[] tmp = new double[this.values.length];

        for (int n = this.values.length; n-- != 0; ) {
            tmp[n] = this.values[n] + source.values[n];
        }

        final HistoricTimeseries result = new HistoricTimeseries(tmp, this.startDay);
        if (this.hasPreviousValue() && ht.hasPreviousValue() &&
                this.previousDay.equals(ht.previousDay)) {
            result.setPreviousValue(this.previousValue + ht.previousValue, this.previousDay);
        }
        return result;
    }

    public HistoricTimeseries aggregate(Period period, Aggregation aggregation) {
        return aggregate(this.startDay, period, aggregation);
    }

    public HistoricTimeseries aggregate(LocalDate from, Period period, Aggregation aggregation) {
        if (period == null || period.equals(Constants.ONE_DAY)) {
            return this;
        }
        final LocalDateSequence lds = LocalDateSequence.create(from, period);
        final double[] others = getAggregatedValues(lds, aggregation);
        return new HistoricTimeseries(others, this.startDay);
    }

    private double[] getAggregatedValues(LocalDateSequence lds, Aggregation aggregation) {
        final double[] others = createEmptyValues();
        LocalDate ld = lds.getNext();
        int i = 0;
        do {
            if (!ld.isBefore(this.startDay)) {
                int x = Math.min(this.values.length, DateUtil.daysBetween(this.startDay, ld));
                others[i] = aggregation.aggregate(this.values, i, x);
                i = x;
            }
            ld = lds.getNext();
        } while (i < this.values.length);
        return others;
    }
}
