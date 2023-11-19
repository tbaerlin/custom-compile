/*
 * ImgRenditestruktur.java
 *
 * Created on 28.08.2006 16:29:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.validation.BindException;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;
import de.marketmaker.istar.chart.data.TimeSeries;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.chart.data.TimeSeriesFactory;
import de.marketmaker.istar.chart.data.TimeperiodDefinitionBuilder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgRenditestruktur extends AbstractImgChart {

    private static class DataPoint implements Comparable<DataPoint> {
        private final Quote quote;

        private final int term; // laufzeit

        private BigDecimal value;

        public DataPoint(Quote quote, int term) {
            this.quote = quote;
            this.term = term;
        }

        @Override
        public int compareTo(DataPoint that) {
            return this.term - that.term;
        }

        public String toString() {
            return quote + "/" + term + "=>" + value;
        }
    }

    private interface Strategy {
        boolean canHandle(String countryCode);

        String getVwdCode(String countryCode, int term);
    }

    private static final String[] COUNTRY_CODES = new String[]{
            "DE", "GB", "US", "JP", "AU", "IT"
    };

    private static final int LABELS[] = new int[]{2, 5, 10, 20, 30};

    private static final int[] TERMS = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30};

    private static final int MAX = LABELS[LABELS.length - 1] + LABELS[0];

    private final Strategy tulletStrategy = new Strategy() {
        @Override
        public boolean canHandle(String countryCode) {
            return countryCode != null && countryCode.length() == 2;
        }

        @Override
        public String getVwdCode(String countryCode, int term) {
            return String.format("BMK%s%02dY.TFI", countryCode, term);
        }
    };

    private final Strategy defaultStrategy = new Strategy() {

        private final Map<String, String> countryCodeMappings = new HashMap<>();

        {
            this.countryCodeMappings.put("DE", "DEM");
            this.countryCodeMappings.put("GB", "GBP");
            this.countryCodeMappings.put("US", "USD");
            this.countryCodeMappings.put("JP", "JPY");
            this.countryCodeMappings.put("IT", "ITL");
            this.countryCodeMappings.put("CH", "CHF");
        }

        @Override
        public boolean canHandle(String countryCode) {
            return this.countryCodeMappings.containsKey(countryCode);
        }

        @Override
        public String getVwdCode(String countryCode, int term) {
            return "GB" + this.countryCodeMappings.get(countryCode) + term + "J.BONDS";
        }
    };

    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public ImgRenditestruktur() {
        super(ImgRenditestrukturCommand.class);
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {
        final ImgRenditestrukturCommand cmd = (ImgRenditestrukturCommand) object;

        final ChartModelAndView result = createChartModelAndView(cmd);
        final LocalDate from = new LocalDate();

        final Strategy strategy = getStrategy();
        final String[] countryCodes = getCountryCodes(cmd);

        for (String cc : countryCodes) {
            final List<DataPoint> dataPoints = getDataPoints(strategy, cc);
            if (dataPoints.isEmpty()) {
                continue;
            }
            assignYields(dataPoints);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<createChartModelAndView> " + cc + ": " + dataPoints);
            }

            result.addObject(cc, getTimeSeriesCollection(from, cc, dataPoints));
        }

        // to render the different terms, we use a chart that spans x days starting at from
        // each data point for term y is at from + y days.
        final TimeperiodDefinitionBuilder tsb
                = new TimeperiodDefinitionBuilder(from, from.plusDays(MAX), false);
        for (int label : LABELS) {
            tsb.addLabel(Integer.toString(label), label / (double) MAX);
        }

        result.withTimeperiod(tsb);

        return result;
    }

    private List<DataPoint> getDataPoints(Strategy strategy, String countryCode) {
        if (!strategy.canHandle(countryCode)) {
            return Collections.emptyList();
        }

        final List<String> vwdCodes = getVwdCodes(strategy, countryCode);
        final List<Quote> quotes = identifyQuotes(vwdCodes);

        final List<DataPoint> result = new ArrayList<>();
        for (int i = 0; i < TERMS.length; i++) {
            if (quotes.get(i) != null) {
                result.add(new DataPoint(quotes.get(i), TERMS[i]));
            }
            else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getDataPoints> no quote for " + vwdCodes.get(i));
                }
            }
        }
        return result;
    }

    private List<String> getVwdCodes(Strategy strategy, String countryCode) {
        final List<String> result = new ArrayList<>(TERMS.length);
        for (int term : TERMS) {
            result.add(strategy.getVwdCode(countryCode, term));
        }
        return result;
    }

    private List<Quote> identifyQuotes(List<String> vwdCodes) {
        return instrumentProvider.identifyQuotes(vwdCodes, SymbolStrategyEnum.VWDCODE, null, null);
    }

    private String[] getCountryCodes(ImgRenditestrukturCommand cmd) {
        return (cmd.getCountryCodes() != null) ? cmd.getCountryCodes() : COUNTRY_CODES;
    }

    private TimeSeriesCollection getTimeSeriesCollection(LocalDate from, String id,
            List<DataPoint> dps) {
        final TimeSeriesCollection result = new TimeSeriesCollection(id, "", LineDrawStyle.STYLE_KEY);
        result.add(id, getTimeSeries(from, id, dps));
        return result;
    }

    private TimeSeries getTimeSeries(LocalDate from, String id, List<DataPoint> dps) {
        dps.sort(null);

        final double[] values = new double[dps.size()];
        final long[] times = new long[dps.size()];

        for (int i = 0; i < dps.size(); i++) {
            final DataPoint point = dps.get(i);
            values[i] = point.value.doubleValue() * 100;
            times[i] = from.plusDays(point.term).toDateTimeAtStartOfDay().getMillis();
        }
        return TimeSeriesFactory.simple(id, "", values, times, 0);
    }

    private Strategy getStrategy() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile.isAllowed(Selector.TULLET)) {
            return this.tulletStrategy;
        }
        return this.defaultStrategy;
    }

    private void assignYields(final List<DataPoint> dataPoints) throws Exception {
        final List<PriceRecord> prices = getPrices(dataPoints);
        for (int i = 0; i < dataPoints.size(); i++) {
            dataPoints.get(i).value = prices.get(i).getYield();
        }
    }

    private List<PriceRecord> getPrices(final List<DataPoint> dataPoints) throws Exception {
        return RequestContextHolder.callWith(ProfileFactory.valueOf(true),
                new Callable<List<PriceRecord>>() {
                    public List<PriceRecord> call() throws Exception {
                        return intradayProvider.getPriceRecords(extractQuotes(dataPoints));
                    }
                });
    }

    private List<Quote> extractQuotes(List<DataPoint> dataPoints) {
        final ArrayList<Quote> result = new ArrayList<>(dataPoints.size());
        for (DataPoint dp : dataPoints) {
            result.add(dp.quote);
        }
        return result;
    }
}