/*
 * Factors.java
 *
 * Created on 12.08.13 12:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.CorporateAction;

/**
 * @author zzhao
 */
class PriceFactors {

    static final PriceFactors EMPTY = new PriceFactors(0);

    private final List<GeneralFactor> factors;

    private final List<GeneralFactor> splitFactors;

    public PriceFactors(int count) {
        this.splitFactors = new ArrayList<>(count);
        this.factors = new ArrayList<>(count);
    }

    public void addFactor(CorporateAction.Type type, LocalDate exDay, double factor) {
        final int days = HistoricTimeseriesUtils.daysFromBegin(exDay);
        cumulateFactors(this.factors, days, factor);

        if (type == CorporateAction.Type.FACTOR) {
            cumulateFactors(this.splitFactors, days, factor);
        }
    }

    private void cumulateFactors(List<GeneralFactor> factors, int days, double factor) {
        if (factors.size() > 0 && factors.get(factors.size() - 1).getDays() == days) {
            // if split and dividend on same day, split applied on existing factors
            final GeneralFactor gf = factors.get(factors.size() - 1);
            factors.set(factors.size() - 1, gf.withFactor(gf.getFactor() * factor));
        }
        else {
            factors.add(new GeneralFactor(days, factor));
        }
    }

    public FactorSlots withBaseDate(LocalDate baseDate) {
        final int baseDays = null == baseDate
                ? HistoricTimeseriesUtils.daysTillToday()
                : HistoricTimeseriesUtils.daysFromBegin(baseDate);
        return new FactorSlots(generateSlots(this.factors, baseDays),
                generateSlots(this.splitFactors, baseDays));

    }

    private List<FactorSlots.Slot> generateSlots(List<GeneralFactor> factors, int baseDays) {
        final ArrayList<Double> values = new ArrayList<>(factors.size() + 1);
        final ArrayList<FactorSlots.Slot> slots = new ArrayList<>(factors.size() + 1);

        int baseIndex = factors.size(); // index of slot which contains baseDays
        int lowerBound = FactorSlots.Slot.N_INF;
        for (int i = 0; i < factors.size(); i++) {
            final GeneralFactor factor = factors.get(i);
            final FactorSlots.Slot slot = new FactorSlots.Slot(lowerBound, factor.days);
            values.add(factor.getFactor());
            slots.add(slot);

            if (baseDays < slot.getUpperBound()) {
                baseIndex = i;
            }
            lowerBound = slot.getUpperBound();
        }

        // add cap slot
        values.add(1.0D);
        slots.add(new FactorSlots.Slot(lowerBound, FactorSlots.Slot.P_INF));

        if (baseIndex < factors.size()) {
            final Double last = values.remove(values.size() - 1);
            values.add(baseIndex, last);
            for (int i = baseIndex + 1; i < values.size(); i++) {
                values.set(i, 1.0D / values.get(i));
            }
        }

        for (int i = 0; i < slots.size(); i++) {
            if (i < baseIndex) {
                slots.get(i).setFactor(calculateFactor(values.subList(i, baseIndex)));
            }
            else if (i > baseIndex) {
                slots.get(i).setFactor(calculateFactor(values.subList(baseIndex + 1, i + 1)));
            }
            else {
                slots.get(i).setFactor(values.get(baseIndex));
            }
        }

        return slots;
    }

    private double calculateFactor(List<Double> vals) {
        double ret = 1.0D;
        for (Double val : vals) {
            ret *= val;
        }
        return ret;
    }

    static final class GeneralFactor {

        private final int days;

        private final double factor;

        private GeneralFactor(int days, double factor) {
            this.days = days;
            this.factor = factor;
        }

        int getDays() {
            return days;
        }

        double getFactor() {
            return factor;
        }

        GeneralFactor withFactor(double factor) {
            return new GeneralFactor(this.days, factor);
        }
    }
}
