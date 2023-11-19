/*
 * MscPerformanceCalculator.java
 *
 * Created on 3/21/14 4:48 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.Min;
import de.marketmaker.istar.common.validator.NotZero;
import de.marketmaker.istar.domain.data.IntervalPerformance;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.IntervalUnit;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import org.joda.time.Interval;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Stefan Willenbrock
 */
public class MscPerformanceCalculator extends EasytradeCommandController {

    protected final static int NUM_INTERVALS = 5;

    protected final static IntervalUnit INTERVAL_UNIT = IntervalUnit.YEARS;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected HistoricRatiosProvider historicRatiosProvider;

    protected String templateName = "mscperformancecalculator";

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public static class Command extends DefaultSymbolCommand {
        private BigDecimal investment;
        private BigDecimal agio;
        private BigDecimal orderCharge;
        private BigDecimal comission;
        private BigDecimal depositCharge;
        private BigDecimal miscCosts;

        /**
         * @return Value spent for the security.
         * @sample 2000
         */
        @Min(value = 0)
        @NotZero
        public BigDecimal getInvestment() {
            return investment;
        }

        public void setInvestment(BigDecimal investment) {
            this.investment = investment;
        }

        /**
         * @return Value added on top of the inner investment.
         * @sample 2.5
         */
        @Min(value = 0)
        public BigDecimal getAgio() {
            return agio;

        }

        public void setAgio(BigDecimal agio) {
            this.agio = agio;
        }

        /**
         * @return Value paid once for the brokerage.
         * @sample 1.25
         */
        @Min(value = 0)
        public BigDecimal getOrderCharge() {
            return orderCharge;
        }

        public void setOrderCharge(BigDecimal orderCharge) {
            this.orderCharge = orderCharge;
        }

        /**
         * @return Value paid once for the stock market.
         * @sample 0.75
         */
        @Min(value = 0)
        public BigDecimal getComission() {
            return comission;
        }

        public void setComission(BigDecimal comission) {
            this.comission = comission;
        }

        /**
         * @return Value paid per year for the depot.
         * @sample 19.90
         */
        @Min(value = 0)
        public BigDecimal getDepositCharge() {
            return depositCharge;
        }

        public void setDepositCharge(BigDecimal depositCharge) {
            this.depositCharge = depositCharge;
        }

        /**
         * @return Absolute costs only paid once.
         * @sample 49.90
         */
        @Min(value = 0)
        public BigDecimal getMiscCosts() {
            return miscCosts;
        }

        public void setMiscCosts(BigDecimal miscCosts) {
            this.miscCosts = miscCosts;
        }
    }

    public MscPerformanceCalculator() {
        super(Command.class);
    }

    public <T extends Command> MscPerformanceCalculator(Class<T> aClass) {
        super(aClass);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) {
        checkPermission(Selector.DZ_WERTENTICKLUNGSRECHNER);

        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        if (quote == null) {
            errors.reject("quote.unknown", "no quote found");
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);

        if (cmd.getInvestment() == null) {
            // Return quote only. Enables snippet to use only one dmxml request type for quote resolution and calculation.
            model.put("data", Collections.EMPTY_LIST);
            return new ModelAndView(this.templateName, model);
        }

        final List<IntervalPerformance> intervalPerformances = this.historicRatiosProvider.getIntervalPerformances(SymbolQuote.create(quote), INTERVAL_UNIT, NUM_INTERVALS);
        intervalPerformances.removeIf(ip -> ip.getPerformance() == null);

        if (intervalPerformances.isEmpty()) {
            errors.reject("performances.missing", "no performance available");
        } else {
            sortAscendingMillisOrder(intervalPerformances);
            final List<PerformanceCalculation> resultData = getData(cmd, intervalPerformances);
            sortDescendingMillisOrder(resultData);
            model.put("data", resultData);
        }

        return new ModelAndView(this.templateName, model);
    }

    private void sortAscendingMillisOrder(List<IntervalPerformance> intervalPerformances) {
        intervalPerformances.sort(null);
    }

    /**
     * This is just to leave the list order ascending like before. No API change because MMF already uses this block.
     */
    private void sortDescendingMillisOrder(List<PerformanceCalculation> calculationData) {
        calculationData.sort(null);
        Collections.reverse(calculationData);
    }

    List<PerformanceCalculation> getData(Command cmd, List<IntervalPerformance> intervalPerformances) {
        final List<PerformanceCalculation> data = new ArrayList<>();
        BigDecimal investment = cmd.getInvestment();
        final BigDecimal agio = getValid(cmd.getAgio());
        final BigDecimal orderCharge = getValid(cmd.getOrderCharge());
        final BigDecimal comission = getValid(cmd.getComission());
        final BigDecimal depositCharge = getValid(cmd.getDepositCharge());
        final BigDecimal miscCosts = getValid(cmd.getMiscCosts());
        boolean first = true;
        for (IntervalPerformance p : intervalPerformances) {
            PerformanceCalculation c = new PerformanceCalculation(
                    p.getInterval(),
                    p.getPerformance(),
                    investment,
                    depositCharge);
            if (first) {
                c.calculate(agio, orderCharge, comission, miscCosts);
                first = false;
            } else {
                c.calculate();
            }
            investment = c.getEndInvestment();
            data.add(c);
        }
        return data;
    }

    private BigDecimal getValid(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public class PerformanceCalculation implements Comparable<PerformanceCalculation> {
        private Interval interval;
        private BigDecimal startInvestment;
        private BigDecimal performanceBrutto;
        private BigDecimal performanceBruttoAbs;
        private BigDecimal costsAbsOnce;
        private BigDecimal costsAbsPerYear;
        private BigDecimal investmentMinusCosts;
        private BigDecimal endInvestment;
        private BigDecimal performanceNettoAbs;
        private BigDecimal performanceNetto;

        public PerformanceCalculation(Interval interval,
                                      BigDecimal performance,
                                      BigDecimal investment,
                                      BigDecimal depositCharge) {
            this.interval = interval;
            this.startInvestment = investment;
            this.performanceBrutto = performance;
            this.costsAbsPerYear = depositCharge;
        }

        void calculate(BigDecimal agio, BigDecimal orderCharge, BigDecimal comission, BigDecimal miscCosts) {
            final BigDecimal sumRel = agio.add(orderCharge).add(comission);
            final BigDecimal sumAbs = sumRel.divide(new BigDecimal(100), Constants.MC).multiply(startInvestment, Constants.MC);
            this.costsAbsOnce = sumAbs.add(miscCosts);
            common();
        }

        void calculate() {
            this.costsAbsOnce = BigDecimal.ZERO;
            common();
        }

        private void common() {
            performanceBruttoAbs = startInvestment.multiply(performanceBrutto, Constants.MC);
            investmentMinusCosts = startInvestment.subtract(costsAbsOnce).subtract(costsAbsPerYear);
            endInvestment = investmentMinusCosts.add(performanceBruttoAbs);
            performanceNettoAbs = endInvestment.subtract(startInvestment);
            performanceNetto = performanceNettoAbs.divide(startInvestment, Constants.MC);
        }

        public Interval getInterval() {
            return interval;
        }

        public BigDecimal getStartInvestment() {
            return startInvestment;
        }

        public BigDecimal getPerformanceBrutto() {
            return performanceBrutto;
        }

        public BigDecimal getPerformanceBruttoAbs() {
            return performanceBruttoAbs;
        }

        public BigDecimal getCostsAbsOnce() {
            return costsAbsOnce;
        }

        public BigDecimal getCostsAbsPerYear() {
            return costsAbsPerYear;
        }

        public BigDecimal getInvestmentMinusCosts() {
            return investmentMinusCosts;
        }

        public BigDecimal getEndInvestment() {
            return endInvestment;
        }

        public BigDecimal getPerformanceNettoAbs() {
            return performanceNettoAbs;
        }

        public BigDecimal getPerformanceNetto() {
            return performanceNetto;
        }

        @Override
        public String toString() {
            return "PerformanceCalculation["
                    + "start=" + interval.getStart()
                    + ", end=" + interval.getEnd()
                    + ", startInvestment=" + startInvestment
                    + ", performanceBrutto=" + performanceBrutto
                    + ", performanceBruttoAbs=" + performanceBruttoAbs
                    + ", costsAbsOnce=" + costsAbsOnce
                    + ", costsAbsPerYear=" + costsAbsPerYear
                    + ", investmentMinusCosts=" + investmentMinusCosts
                    + ", endInvestment=" + endInvestment
                    + ", performanceNettoAbs=" + performanceNettoAbs
                    + ", performanceNetto=" + performanceNetto
                    + "]";
        }

        @Override
        /**
         * Compares two intervals after the start date in ascending millisecond instance order.
         *
         * @see org.joda.time.base.AbstractInstant#compareTo(org.joda.time.ReadableInstant)
         */
        public int compareTo(PerformanceCalculation o) {
            if (this.interval == null || this.interval.getStart() == null) {
                throw new IllegalStateException("interval error");
            }
            if (o.getInterval() == null || o.getInterval().getStart() == null) {
                throw new IllegalArgumentException("interval error in argument");
            }
            return this.interval.getStart().compareTo(o.getInterval().getStart());
        }

    }
}
