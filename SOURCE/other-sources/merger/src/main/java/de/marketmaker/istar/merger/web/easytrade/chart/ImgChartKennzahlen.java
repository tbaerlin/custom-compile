/*
 * ImgChartKennzahlen.java
 *
 * Created on 28.08.2006 16:29:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;

import de.marketmaker.istar.domain.data.BasicBalanceFigures;
import de.marketmaker.istar.domain.data.ProfitAndLoss;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.merger.provider.CompanyFundamentalsProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.chart.data.TimeSeriesFactory;
import de.marketmaker.istar.chart.data.TimeperiodDefinitionBuilder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartKennzahlen extends AbstractImgChart {

    private final BigDecimal ONE_MILLION = BigDecimal.valueOf(1000000, 0);

    private CompanyFundamentalsProvider companyFundamentalsProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setCompanyFundamentalsProvider(
            CompanyFundamentalsProvider companyFundamentalsProvider) {
        this.companyFundamentalsProvider = companyFundamentalsProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public ImgChartKennzahlen() {
        super(ImgChartKennzahlenCommand.class);
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {

        final ImgChartKennzahlenCommand cmd = (ImgChartKennzahlenCommand) object;

        Interval interval = null;
        if (cmd.getPeriod() != null) {
            interval = new Interval(cmd.getPeriod(), new DateTime());
        }

        final List<ValueAndYear> elements = getElements(cmd);

        elements.sort(null);

        if (interval != null) {
            final LocalDate start = interval.getStart().toLocalDate();
            while (!elements.isEmpty() && elements.get(0).getLocalDate().isBefore(start)) {
                elements.remove(0);
            }
        }
        else {
            if (!elements.isEmpty()) {
                interval = new Interval(elements.get(0).getLocalDate().minusDays(1).toDateTimeAtStartOfDay(),
                        new DateTime());
            }
            else {
                interval = new Interval(new DateTime().minusYears(1), new DateTime());
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<createChartModelAndView> " + elements);
        }

        final ChartModelAndView result = createChartModelAndView(cmd);
        final Map<String, Object> model = result.getModel();

        final TimeperiodDefinitionBuilder tsb =
                new TimeperiodDefinitionBuilder(interval.getStart().toLocalDate(),
                        interval.getEnd().toLocalDate().plusDays(1), false);
        result.withTimeperiod(tsb);

        final double[] values = new double[elements.size()];
        final long[] times = new long[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            final ValueAndYear vay = elements.get(i);
            values[i] = vay.getValue().doubleValue();
            times[i] = vay.getLocalDate().toDateTimeAtStartOfDay().getMillis();
        }

        final TimeSeriesCollection main =
                new TimeSeriesCollection("main", "close", LineDrawStyle.STYLE_KEY);
        main.add("close", TimeSeriesFactory.simple("close", "", values, times, 0));
        model.put("main", main);

        return result;
    }

    private List<ValueAndYear> getElements(ImgChartKennzahlenCommand cmd) {
        final List<ValueAndYear> result = new ArrayList<>();

        final Instrument instrument = this.instrumentProvider.identifyInstrument(cmd);

        if ("umsatz".equals(cmd.getTyp())) {
            final List<ProfitAndLoss> figures =
                    this.companyFundamentalsProvider.getProfitAndLosses(instrument.getId());
            for (final ProfitAndLoss paf : figures) {
                if (paf.getSales() != null) {
                    result.add(new ValueAndYear(paf.getReference().getInterval().getEnd().getYear(),
                            paf.getSales().multiply(ONE_MILLION)));
                }
            }
        }
        else if ("gewinn".equals(cmd.getTyp())) {
            final List<ProfitAndLoss> figures =
                    this.companyFundamentalsProvider.getProfitAndLosses(instrument.getId());
            for (final ProfitAndLoss paf : figures) {
                if (paf.getProfitYear() != null) {
                    result.add(new ValueAndYear(paf.getReference().getInterval().getEnd().getYear(),
                            paf.getProfitYear().multiply(ONE_MILLION)));
                }
            }
        }
        else if ("dividende".equals(cmd.getTyp())) {
            final List<ProfitAndLoss> figures =
                    this.companyFundamentalsProvider.getProfitAndLosses(instrument.getId());
            for (final ProfitAndLoss paf : figures) {
                    result.add(new ValueAndYear(paf.getReference().getInterval().getEnd().getYear(),
                            paf.getDividend() != null ? paf.getDividend() : BigDecimal.ZERO));
            }
        }
        else if ("mitarbeiter".equals(cmd.getTyp())) {
            final List<BasicBalanceFigures> figures =
                    this.companyFundamentalsProvider.getBalanceFigures(instrument.getId());
            for (final BasicBalanceFigures bbf : figures) {
                if (bbf.getNumberOfEmployees() != null) {
                    result.add(new ValueAndYear(bbf.getReference().getInterval().getEnd().getYear(),
                            new BigDecimal(bbf.getNumberOfEmployees())));
                }
            }
        }
        return result;
    }

    private static class ValueAndYear implements Comparable<ValueAndYear> {
        private final int year;

        private final BigDecimal value;

        public ValueAndYear(int year, BigDecimal value) {
            this.year = year;
            this.value = value;
        }

        public LocalDate getLocalDate() {
            return new LocalDate(this.year, 1, 1);
        }

        public BigDecimal getValue() {
            return this.value;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final ValueAndYear that = (ValueAndYear) o;
            return year == that.year;
        }

        public int hashCode() {
            return year;
        }

        public int compareTo(ValueAndYear o) {
            return this.year - o.year;
        }

        public String toString() {
            return "ValueAndYear[" + this.year + "=>" + this.value + "]";
        }
    }
}
