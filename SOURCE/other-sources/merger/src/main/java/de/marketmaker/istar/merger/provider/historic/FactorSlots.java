/*
 * Factors.java
 *
 * Created on 12.08.13 12:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * @author zzhao
 */
class FactorSlots {

    private final List<Slot> factorSlots;

    private final List<Slot> splitFactorSlots;

    public FactorSlots(List<Slot> factorSlots, List<Slot> splitFactorSlots) {
        this.factorSlots = factorSlots;
        this.splitFactorSlots = splitFactorSlots;
    }

    public FactorSlots inverse() {
        return new FactorSlots(inverseList(this.factorSlots), inverseList(this.splitFactorSlots));
    }

    private List<Slot> inverseList(List<Slot> list) {
        final ArrayList<Slot> ret = new ArrayList<>(list.size());
        for (Slot slot : list) {
            ret.add(slot.inverse());
        }
        return ret;
    }

    public HistoricTimeseries applySplit(HistoricTimeseries ht) {
        return apply(this.splitFactorSlots, ht, 1.0D, null);
    }

    private HistoricTimeseries apply(List<Slot> slots, HistoricTimeseries ht, double fixFactor,
            double[] varFactors) {
        if (null == ht || ht.size() == 0) {
            return ht;
        }

        final LocalDate startDay = ht.getStartDay();
        int startDays = HistoricTimeseriesUtils.daysFromBegin(startDay);

        final double[] baseValues = ht.getValues();
        final double[] values = new double[ht.size()];

        int next = startDays;
        for (final Slot slot : slots) {
            next = slot.apply(next, values, startDays, baseValues, fixFactor, varFactors);
        }
        while (next < baseValues.length) {
            values[next - startDays] = baseValues[next - startDays];
            next++;
        } // in case that corporate action reference date is before close time series end date

        return new HistoricTimeseries(values, startDay);
    }

    public HistoricTimeseries apply(HistoricTimeseries ht, BigDecimal factor) {
        return apply(this.factorSlots, ht, null == factor ? 1.0D : factor.doubleValue(), null);
    }

    public HistoricTimeseries apply(HistoricTimeseries ht, BigDecimal factor,
            HistoricTimeseries ccs) {
        final HistoricTimeseries varFactors = ccs.alignTo(ht);
        return apply(this.factorSlots, ht, null == factor ? 1.0D : factor.doubleValue(), varFactors.getValues());
    }

    static final class Slot {

        static final int N_INF = -1;

        static final int P_INF = Integer.MAX_VALUE;

        private final int lowerBound;

        private final int upperBound;

        private double factor;

        Slot(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        int getUpperBound() {
            return upperBound;
        }

        void setFactor(double factor) {
            this.factor = factor;
        }

        Slot inverse() {
            final Slot slot = new Slot(this.lowerBound, this.upperBound);
            slot.setFactor(1.0D / this.factor);
            return slot;
        }

        public int apply(int next, double[] values, int startDays, double[] baseValues,
                double fixFactor, double[] varFactors) {
            int cursor = Math.max(this.lowerBound, next);
            while (cursor < this.upperBound) {
                final int idx = cursor - startDays;
                if (idx >= baseValues.length) {
                    break;
                }
                values[idx] = baseValues[idx] * this.factor * fixFactor;
                if (null != varFactors && idx < varFactors.length) {
                    values[idx] *= varFactors[idx];
                }
                cursor++;
            }

            return cursor;
        }
    }
}
