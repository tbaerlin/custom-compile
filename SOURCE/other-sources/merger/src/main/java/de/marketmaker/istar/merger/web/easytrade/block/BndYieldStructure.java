/*
 * BndYieldStructure.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.historic.HistoricRequestImpl;
import de.marketmaker.istar.merger.provider.historic.HistoricTerm;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesRequest;

/**
 * Provides historical yield values of benchmark bonds with maturities from 1 year to 10 years for
 * specific countries.
 * <p>
 * Each country has specific bonds with different maturities which serve as benchmarks for other
 * fixed-income instruments. This service provides the historical yields for such benchmark bonds.
 * Interpolation will be applied if some yield values are missing and can be interpolated.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BndYieldStructure extends EasytradeCommandController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HistoricConfigurationMBean historicConfigurationMBean;

    private EasytradeInstrumentProvider instrumentProvider;

    private HistoricTimeseriesProvider historicTimeseriesProvider;

    private HistoricTimeseriesProvider historicTimeseriesProviderEod;

    private IntradayProvider intradayProvider;

    public BndYieldStructure() {
        super(Command.class);
    }

    public void setHistoricTimeseriesProvider(
            HistoricTimeseriesProvider historicTimeseriesProvider) {
        this.historicTimeseriesProvider = historicTimeseriesProvider;
    }

    public void setHistoricTimeseriesProviderEod(
            HistoricTimeseriesProvider historicTimeseriesProviderEod) {
        this.historicTimeseriesProviderEod = historicTimeseriesProviderEod;
    }

    public void setHistoricConfigurationMBean(HistoricConfigurationMBean historicConfigurationMBean) {
        this.historicConfigurationMBean = historicConfigurationMBean;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public static class Command {
        private String[] countryCode;

        private LocalDate date;

        /**
         * @return two character country symbols according to ISO 3166-1 alpha-2.
         * @sample DE, US
         */
        @NotNull
        public String[] getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String[] countryCode) {
            this.countryCode = countryCode;
        }

        /**
         * @return a date till which historical yield values are retrieved.
         */
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    protected ModelAndView doHandle(final HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        // HACK to p
        // Requirements by MKrausch/DSantos 2008/10/29
        final Profile p = ProfileFactory.valueOf(profile.isAllowed(Selector.YIELD_STRUCTURE));
        return RequestContextHolder.callWith(p, () -> doHandle(cmd, request));

    }

    private ModelAndView doHandle(Command cmd, HttpServletRequest request) {
        final LocalDate date = (cmd.getDate() == null)
                ? new LocalDate().minusDays(1) : cmd.getDate();

        final Map<String, List<YieldElement>> elements = new HashMap<>();

        for (final String code : cmd.getCountryCode()) {
            final List<YieldElement> list = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                final String vwdcode = getVwdcode(code, i);
                try {
                    final Quote quote = this.instrumentProvider.identifyQuoteByVwdcode(vwdcode);
                    list.add(new YieldElement(i, vwdcode, quote));
                } catch (Exception e) {
                    list.add(new YieldElement(i, vwdcode, null));
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<doHandle> no quote for " + vwdcode);
                    }
                }
            }
            elements.put(code, list);
        }

        for (final List<YieldElement> yieldElements : elements.values()) {
            for (final YieldElement element : yieldElements) {
                final Quote elementQuote = element.getQuote();
                if (elementQuote == null) {
                    continue;
                }

                final PriceRecord price = this.intradayProvider.getPriceRecords(Collections.singletonList(elementQuote)).get(0);
                final LocalDate from = new LocalDate(date.getYear(), 1, 1);
                final LocalDate to = new LocalDate();

                final boolean eodHistoryEnabled = this.historicConfigurationMBean.isEodHistoryEnabled(elementQuote);
                this.logger.debug("<doHandle> eod history enabled: {}", eodHistoryEnabled);

                final HistoricTimeseries timeseries;
                if (eodHistoryEnabled) {
                    final HistoricRequestImpl htr = new HistoricRequestImpl(elementQuote, from, to)
                        .withPriceRecord(price);
                    htr.addHistoricTerm(HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Rendite));
                    timeseries = this.historicTimeseriesProviderEod.getTimeseries(htr).get(0);
                } else {
                    final HistoricTimeseriesRequest htr = new HistoricTimeseriesRequest(
                        elementQuote, from, to).withPriceRecord(price);
                    htr.addOpenInterest(null);
                    timeseries = this.historicTimeseriesProvider.getTimeseries(htr).get(0);
                }

                if (timeseries == null) {
                    continue;
                }
                int offset = timeseries.getOffset(date);
                LocalDate ymd = new LocalDate(date);
                while (offset > 0 && Double.isNaN(timeseries.getValue(offset))) {
                    offset--;
                    ymd = ymd.minusDays(1);
                }
                final double value = timeseries.getValue(offset);
                if (!Double.isNaN(value)) {
                    element.setValue(BigDecimal.valueOf(value));
                    element.setDate(ymd);
                }
            }

            for (int i = 0; i < yieldElements.size(); i++) {
                final YieldElement element = yieldElements.get(i);

                if (element.getValue() != null) {
                    continue;
                }

                final int from = getIndex(yieldElements, i, false);
                final int to = getIndex(yieldElements, i, true);

                if (from == -1 || to == -1) {
                    continue;
                }

                final BigDecimal fv = yieldElements.get(from).getValue();
                final BigDecimal tv = yieldElements.get(to).getValue();
                final BigDecimal value = fv.add(tv.subtract(fv)
                        .multiply(BigDecimal.valueOf(i - from), Constants.MC)
                        .divide(BigDecimal.valueOf(to - from), Constants.MC));
                element.setValue(value);
                element.setInterpolated(true);
            }
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("elements", elements);
        return new ModelAndView("bndyieldstructure", model);
    }

    private int getIndex(List<YieldElement> elements, int index, boolean up) {
        if (up) {
            for (int i = index + 1; i < elements.size(); i++) {
                final YieldElement element = elements.get(i);
                if (element.getValue() != null && !element.isInterpolated()) {
                    return i;
                }
            }
        }
        else {
            for (int i = index - 1; i >= 0; i--) {
                final YieldElement element = elements.get(i);
                if (element.getValue() != null && !element.isInterpolated()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String getVwdcode(String code, int i) {
        if ("US".equals(code)) {
            return "GBUSD" + i + "J.BONDS";
        }
        else if ("CH".equals(code)) {
            return "CHFGB" + i + "J.BONDS";
        }
        return "BMK" + code + (i < 10 ? "0" : "") + i + "Y.TFI";
    }

    public static class YieldElement implements HasQuote {
        private final int year;

        private final String vwdcode;

        private final Quote quote;

        private BigDecimal value = null;

        private LocalDate date = null;

        private boolean interpolated;

        public YieldElement(int year, String vwdcode, Quote quote) {
            this.year = year;
            this.vwdcode = vwdcode;
            this.quote = quote;
        }

        public int getYear() {
            return year;
        }

        public String getVwdcode() {
            return vwdcode;
        }

        @Override
        public Quote getQuote() {
            return quote;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public boolean isInterpolated() {
            return interpolated;
        }

        public void setInterpolated(boolean interpolated) {
            this.interpolated = interpolated;
        }
    }
}
