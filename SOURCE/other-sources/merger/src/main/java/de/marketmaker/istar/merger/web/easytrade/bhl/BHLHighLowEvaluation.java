/*
 * BHLHighLowEvaluation.java
 *
 * Created on 15.02.2011 11:34:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.bhl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MscHistoricData;
import de.marketmaker.istar.merger.web.easytrade.block.MscHistoricDataMethod;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;

/**
 * Sample: curl -o response.csv -F "request=@bhl-request.csv" http://localhost/dmxml-1/bew/bhl-high-low-evaluation.csv
 * @author Thomas Kiesgen
 */
@Controller
public class BHLHighLowEvaluation {
    public static class Command {

        private MultipartFile request;

        public void setRequest(MultipartFile request) {
            this.request = request;
        }

        public MultipartFile getRequest() {
            return request;
        }
    }

    private static class HighLowData {
        private BigDecimal high = BigDecimal.ZERO;

        private String highMarket = null;

        private BigDecimal low = new BigDecimal(Long.MAX_VALUE);

        private String lowMarket = null;

        private void update(BigDecimal atHigh, BigDecimal atLow, String market) {
            if (atHigh != null && atHigh.compareTo(high) > 0) {
                high = atHigh;
                highMarket = market;
            }
            if (atLow != null && atLow.compareTo(low) < 0) {
                low = atLow;
                lowMarket = market;
            }
        }

        private boolean isValid() {
            return this.highMarket != null || this.lowMarket != null;
        }
    }

    private static final DateTimeFormatter DIR_NAME_FORMATTER
            = DateTimeFormat.forPattern("'bhl-hle-'yyyyMMdd_HHmmss");

    private static final List<QuoteFilter> QUOTE_FILTERS
            = Arrays.asList(QuoteFilters.WITH_VWDSYMBOL, QuoteFilters.WITH_PRICES);

    private static final BigDecimal ONEHUNDRED = new BigDecimal(100);

    private static final String ZEROS = "00000000000000000000000000";

    private static final String SPACES = "                         ";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;

    private HistoricTimeseriesProvider historicTimeseriesProvider;

    private IntradayProvider intradayProvider;

    private ProfileProvider profileProvider;

    private File workDir;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricTimeseriesProvider(
            HistoricTimeseriesProvider historicTimeseriesProvider) {
        this.historicTimeseriesProvider = historicTimeseriesProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
        if (!this.workDir.isDirectory() && !this.workDir.mkdirs()) {
            throw new IllegalArgumentException("invalid workDir " + this.workDir.getAbsolutePath());
        }
    }

    @RequestMapping("/bew/bhl-high-low-evaluation.csv")
    public void handle(HttpServletRequest request,
            HttpServletResponse response, Command c) throws Exception {
        if (c.getRequest() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            this.logger.warn("<handle> no request file for " + request.getRequestURI());
            return;
        }

        final File taskDir = getTaskDir();
        if (!taskDir.mkdirs()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.logger.error("<handle> failed to create directory " + taskDir.getAbsolutePath());
            return;
        }
        this.logger.info("<handle> created " + taskDir.getAbsolutePath());

        final File requestFile = new File(taskDir, "request.csv");
        try {
            c.getRequest().transferTo(requestFile);
        } catch (Exception e2) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to store request ", e2);
            return;
        }

//        final ProfileRequest profileRequest = new ProfileRequest("vwd-ent:ByVwdId", "1526");
//        profileRequest.setApplicationId("7");
        final ProfileRequest profileRequest = ProfileRequest.byVwdId("120035", "34");
        final ProfileResponse pr = this.profileProvider.getProfile(profileRequest);
        if (!pr.isValid()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> invalid response for " + profileRequest);
            return;
        }
        final Profile profile = pr.getProfile();
        RequestContextHolder.setRequestContext(new RequestContext(profile,
                MarketStrategy.STANDARD));

        final File result = handle(requestFile, profile);
        if (result != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain;charset=UTF-8");
            FileCopyUtils.copy(new FileInputStream(result), response.getOutputStream());
        }
        else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private File handle(File requestFile, Profile profile) {
        final File response = new File(requestFile.getParentFile(), "response.csv");
        final File problems = new File(requestFile.getParentFile(), "problems.txt");
        PrintWriter writer = null;
        PrintWriter errWriter = null;
        Scanner scanner = null;
        try {
            writer = new PrintWriter(response, "UTF-8");
            errWriter = new PrintWriter(problems, "UTF-8");
            scanner = new Scanner(requestFile);
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                final String[] tokens = line.split(";");
                try {
                    writer.println(prepareLine(profile, tokens, errWriter));
                } catch (Throwable e) {
                    this.logger.warn("<handle> prepareLine failed for " + line, e);
                    errWriter.println(tokens[0] + ": " + e);
                    writer.println(tokens[0]);
                }
                RequestContextHolder.getRequestContext().clearIntradayContext();
            }
            return response;
        } catch (Throwable e) {
            this.logger.error("<handle> failed", e);
            e.printStackTrace(errWriter);
            return null;
        } finally {
            RequestContextHolder.setRequestContext(null);
            IoUtils.close(writer);
            IoUtils.close(errWriter);
            if (problems.length() == 0L && !problems.delete()) {
                this.logger.warn("<handle> failed to delete " + problems.getAbsolutePath());
            }
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private String prepareLine(Profile profile, String[] tokens,
            PrintWriter errWriter) throws IOException {
        final String isin = tokens[0];

        final Instrument instrument = getInstrument(isin);
        if (instrument == null) {
            errWriter.println(isin + ": no instrument");
            return isin;
        }

        final LocalDate date = DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(tokens[1].trim()));
        final String currency = tokens[2].trim();
        final String type = tokens[3].trim();
        final String restriction = (tokens.length > 4 && StringUtils.hasText(tokens[4]))
                ? tokens[4].trim() : null;

        HighLowData data = getData(profile, instrument, isin, type, currency, date, restriction, errWriter);

        if (data == null || !data.isValid()) {
            errWriter.println(isin + ": trying default strategy for date  " + date);

            final List<Quote> allowedQuotes
                    = ProfiledInstrument.quotesWithPrices(instrument, profile);

            final List<Quote> quotes = filterQuotes(currency, null, allowedQuotes, false);
            if (quotes.isEmpty()) {
                errWriter.println(isin + ": no quotes with prices in " + currency);
                return isin;
            }

            data = getHighLowData(date, quotes);
        }

        if (!data.isValid()) {
            errWriter.println(isin + ": no valid data on " + date);
            return isin;
        }

        return buildResult(isin, tokens[1], currency, type, restriction, data);
    }

    private HighLowData getData(Profile profile, Instrument instrument, String isin, String type,
            String currency, LocalDate date, String restriction, PrintWriter errWriter) throws IOException {
        HighLowData data;

        if ("D".equals(type)) {
            // german market price
            final List<Quote> quotes = getGermanQuotes(instrument, currency);
            if (quotes.isEmpty()) {
                errWriter.println(isin + ": no german quotes");
                return null;
            }

            data = getHighLowData(date, quotes);
        }
        else if ("F".equals(type)) {
            // fund price
            final MarketStrategy builder = new MarketStrategy.Builder()
                    .withFilters(QUOTE_FILTERS)
                    .withFilter(new QuoteFilters.CurrencyFilter(currency))
                    .withSelector(QuoteSelectors.byMarket("FONDS")).build();
            final Quote quote = getQuote(instrument, builder);
            if (quote == null) {
                errWriter.println(isin + ": no fund quote");
                return null;
            }
            data = getFundHighLow(date, quote);
        }
        else if ("A".equals(type)) {
            final List<Quote> allowedQuotes
                    = ProfiledInstrument.quotesWithPrices(instrument, profile);
            final List<Quote> quotes = filterQuotes(currency, restriction, allowedQuotes, true);
            if (quotes.isEmpty()) {
                errWriter.println(isin + ": no quotes with prices in " + currency);
                return null;
            }

            data = getHighLowData(date, quotes);

            if (restriction != null && !data.isValid()) {
                quotes.clear();
                quotes.addAll(filterQuotes(currency, null, allowedQuotes, true));
                data = getHighLowData(date, quotes);
            }
            if ("EUR".equals(currency) && !data.isValid()) {
                quotes.clear();
                quotes.addAll(filterQuotes(currency, null, allowedQuotes, false));
                data = getHighLowData(date, quotes);
            }
        }
        else {
            errWriter.println(isin + ": invalid type '" + type + "'");
            return null;
        }

        return data;
    }

    private List<Quote> getGermanQuotes(Instrument instrument, String currency) {
        final List<QuoteFilter> filters = Arrays.asList(QuoteFilters.WITH_VWDSYMBOL,
                QuoteFilters.WITH_PRICES, new QuoteFilters.CurrencyFilter(currency),
                QuoteFilters.MARKET_COUNTRY_DE);

        List<Quote> result = new ArrayList<>(instrument.getQuotes());
        for (final QuoteFilter filter : filters) {
            try {
                result = filter.apply(result);
            } catch (Exception e1) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<handle> empty result result for all filters");
                }
                result.clear();
            }
            if (result.isEmpty()) {
                break;
            }
        }
        return result;
    }

    private HighLowData getHighLowData(LocalDate date, List<Quote> quotes) throws IOException {
        final HighLowData data = new HighLowData();
        for (final Quote quote : quotes) {
            evalHighLow(date, data, quote);
        }
        return data;
    }

    private String buildResult(String isin, String date, String currency, String type,
            String restriction, HighLowData data) {
        if (data.isValid()) {
            final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
            df.applyLocalizedPattern("0,000");

            return format(12, false, isin) + ";" + format(8, false, date) + ";" + format(3, false, currency)
                    + ";" + format(1, false, type) + ";" + format(2, false, restriction)
                    + ";" + format(11, true, data.highMarket != null ? df.format(data.high) : "")
                    + ";" + format(11, true, data.lowMarket != null ? df.format(data.low) : "")
                    + ";" + format(8, false, data.highMarket != null ? "H." + data.highMarket : "")
                    + ";" + format(8, false, data.lowMarket != null ? "L." + data.lowMarket : "");
        }

        return isin;
    }


    private String format(int length, boolean zeroFiller, String s) {
        final String filler = zeroFiller ? ZEROS : SPACES;
        if (s == null) {
            return filler.substring(0, length);
        }
        if (s.length() <= length) {
            final String fill = filler.substring(0, length - s.length());
            return zeroFiller ? fill + s : s + fill;
        }
        return s.substring(0, length);
    }

    private void evalHighLow(LocalDate date, HighLowData data, Quote quote) throws IOException {
        final List<AggregatedTickImpl> timeseries = getTimeseries(date, quote);
        if (timeseries != null) {
            for (int i = timeseries.size(); i-- > 0; ) {
                final AggregatedTickImpl at = timeseries.get(i);
                final BigDecimal atHigh = at.getHigh();
                final BigDecimal atLow = at.getLow();

                if (at.getInterval().getStart().isBefore(date.toDateTimeAtStartOfDay())) {
                    // TODO: is this correct? all but last tick are ignored!
                    break;
                }

                final boolean convert = "GBX".equals(quote.getCurrency().getSymbolIso());
                final BigDecimal h = convert ? atHigh.divide(ONEHUNDRED, Constants.MC) : atHigh;
                final BigDecimal l = convert ? atLow.divide(ONEHUNDRED, Constants.MC) : atLow;
                data.update(h, l, quote.getSymbolVwdfeedMarket());
            }
        }
    }

    private List<Quote> filterQuotes(String currency, String restriction,
            List<Quote> allowedQuotes, boolean withoutGermany) {
        final List<Quote> quotes = new ArrayList<>();
        for (final Quote quote : allowedQuotes) {
            final String country = quote.getMarket().getCountry().getSymbolIso();
            if (withoutGermany) {
                if ("DE".equals(country) && !"DE".equals(restriction)) {
                    continue;
                }
            }

            if (restriction != null && !restriction.equals(country)) {
                continue;
            }

            if (!("GBP".equals(currency) && "GBX".equals(quote.getCurrency().getSymbolIso()))) {
                if (!currency.equals(quote.getCurrency().getSymbolIso())) {
                    continue;
                }
            }

            quotes.add(quote);
        }
        return quotes;
    }

    private Quote getQuote(Instrument instrument, MarketStrategy builder) {
        try {
            return builder.getQuote(instrument);
        } catch (Exception e) {
            this.logger.debug("<getQuote> failed", e);
        }
        return null;
    }

    private Instrument getInstrument(String isin) {
        try {
            return this.instrumentProvider.identifyByIsinOrWkn(isin);
        } catch (Exception e1) {
            this.logger.warn("<handle> get instrument failed:" + isin);
        }
        return null;
    }

    private HighLowData getFundHighLow(LocalDate date, Quote quote) throws IOException {
        final HighLowData data = new HighLowData();
        final List<AggregatedTickImpl> fundTicks = getTimeseries(date, quote);
        if (fundTicks != null) {
            for (int i = fundTicks.size(); i-- > 0; ) {
                final AggregatedTickImpl at = fundTicks.get(i);
                final BigDecimal rp = at.getClose();
                final BigDecimal ip = at.getHigh();
                final DateTime dateTime = at.getInterval().getStart();

                if (dateTime.isBefore(date.toDateTimeAtStartOfDay())) {
                    // TODO: is this correct? all but last tick are ignored!
                    break;
                }

                if (rp != null || ip != null) {
                    data.update(ip != null ? ip : rp, rp != null ? rp : ip, "FONDS");
                    break;
                }
            }
        }
        return data;
    }

    private List<AggregatedTickImpl> getTimeseries(LocalDate date, Quote quote) throws IOException {
        final MscHistoricData.Command hc = new MscHistoricData.Command();
        hc.setBlendCorporateActions(false);
        hc.setBlendDividends(false);
        hc.setInferTickType(false);
        hc.setTickType(TickImpl.Type.TRADE);
        hc.setStart(date.toDateTimeAtStartOfDay().minusWeeks(1));
        hc.setEnd(date.toDateTimeAtStartOfDay());
        hc.setType(InstrumentUtil.isVwdFund(quote)
                ? TickDataCommand.ElementDataType.FUND : TickDataCommand.ElementDataType.OHLCV);
        final Map<String, Object> hm
                = new MscHistoricDataMethod(this.historicTimeseriesProvider, quote, hc, this.intradayProvider).invoke();
        final String type = InstrumentUtil.isVwdFund(quote) ? "fundTs" : "trades";
        //noinspection unchecked
        return (List<AggregatedTickImpl>) hm.get(type);
    }

    private File getTaskDir() throws InterruptedException {
        while (true) {
            final File result = new File(this.workDir, DIR_NAME_FORMATTER.print(new DateTime()));
            if (!result.exists()) {
                return result;
            }
            this.logger.info("<getTaskDir> sleeping 1s, taskDir exists: " + result.getAbsolutePath());
            TimeUnit.SECONDS.sleep(1);
        }
    }
}

