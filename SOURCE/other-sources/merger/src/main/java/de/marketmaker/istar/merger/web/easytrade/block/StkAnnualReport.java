/*
 * StkAnalystenschaetzung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataRequest;
import de.marketmaker.istar.merger.provider.stockdata.StockDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * Provides annual reports for the given symbol.
 * The annual reports contain assets, liabilities, balance sheets and key figures.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkAnnualReport extends EasytradeCommandController {
    private StockDataProvider stockDataProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public static class Command extends DefaultSymbolCommand {
        private Period period;

        /**
         * Only annual reports are returned that were published between the given period before now and now.
         * The period must be specified in <a href="http://en.wikipedia.org/wiki/ISO_8601#Durations" target="_blank">ISO-8601</a> format.
         *
         * Example: If period is P1Y, only annual reports are returned that were published between a year ago and now.
         *
         * @sample P1Y
         */
        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }
    }

    public StkAnnualReport() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final StockDataRequest sdr = new StockDataRequest(Arrays.asList(quote.getInstrument().getId()),
                RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales()).withCompanyProfile().withAnnualReportData();
        final StockDataResponse stockDataResponse = this.stockDataProvider.getStockData(sdr);

        final List<AnnualReportData> reports = isValidData(stockDataResponse)
                ? new ArrayList<>(stockDataResponse.getAnnualReportsDatas().get(0))
                : Collections.<AnnualReportData>emptyList();

        if (cmd.getPeriod() != null) {
            final Interval interval = new Interval(new DateTime().minus(cmd.getPeriod()), new DateTime());

            for (Iterator it = reports.iterator(); it.hasNext();) {
                final AnnualReportData ard = (AnnualReportData) it.next();
                if (!interval.overlaps(ard.getReference())) {
                    it.remove();
                }
            }
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("profile", stockDataResponse.getCompanyProfiles().get(0));
        model.put("reports", reports);
        return new ModelAndView("stkannualreport", model);
    }

    private boolean isValidData(StockDataResponse stockDataResponse) {
        return stockDataResponse.getAnnualReportsDatas() != null && stockDataResponse.getAnnualReportsDatas().get(0) != null;
    }
}