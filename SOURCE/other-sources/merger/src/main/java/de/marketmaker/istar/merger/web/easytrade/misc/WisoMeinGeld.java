/*
 * WisoMeinGeld.java
 *
 * Created on 26.04.13 07:58
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.historic.HistoricRequestImpl;
import de.marketmaker.istar.merger.provider.historic.HistoricTerm;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProviderCombi;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesRequest;
import de.marketmaker.istar.merger.web.AuditTrailFilter;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.HistoricConfigurationMBean;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelector;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Provides historic prices for Wiso MeinGeld/Börse and also exchange rate infos.
 *
 * @author oflege
 */
@Controller
public class WisoMeinGeld {

    private static final String XXP = "XXP";

    public static class Command {
        // internal, includes vwdcode of selected quote in result
        private boolean debug;

        private String request;

        public String getRequest() {
            return request;
        }

        public void setRequest(String request) {
            this.request = request;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    private static class QuoteWithTimeseries {
        final Quote quote;

        final HistoricTimeseries ts;

        private QuoteWithTimeseries(Quote quote, HistoricTimeseries ts) {
            this.quote = quote;
            this.ts = ts;
        }
    }

    private final static List<QuoteSelector> SELECTORS = Arrays.asList(
            LbbwMarketStrategy.LBBW_FONDS,
            LbbwMarketStrategy.MSCI,
            LbbwMarketStrategy.EUWAX_OR_FFMST,
            LbbwMarketStrategy.LBBW_PREFERRED_EXCHANGES,
            QuoteSelectors.HOME_EXCHANGE,
            QuoteSelectors.SELECT_FIRST
    );

    private final static QuoteFilter FONDS_FILTER
            = quote -> "FONDS".equals(quote.getSymbolVwdfeedMarket());

    /**
     * error id for "no service available"
     */
    private static final int ERR_NO_SERVICE = 1000;

    /**
     * error id for "no data for requested security"
     */
    private static final int ERR_NO_DATA = 2000;

    /**
     * error id for "request was not well formed"
     */
    private static final int ERR_ILLEGAL_REQUEST = 3000;

    /**
     * max. number of securities for which data can be requested
     */
    private static final int REQUEST_LIMIT = 100;

    private static final int MAX_DAYS = 69;

    private static final int DEFAULT_DAYS = 30;

    /**
     * formats the date in close items
     */
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yy");

    private static final DateTimeFormatter GENERATED = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * used to format prices
     */
    private static final DecimalFormat PRICE_FORMAT =
            (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);

    /**
     * used to format currency prices
     */
    private static final DecimalFormat CURRENCY_FORMAT =
            (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);

    static {
        PRICE_FORMAT.applyLocalizedPattern("########0,00##");
        CURRENCY_FORMAT.applyLocalizedPattern("########0,0000##");
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HistoricConfigurationMBean historicConfigurationMBean;

    private EasytradeInstrumentProvider instrumentProvider;

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    private HistoricTimeseriesProvider historicTimeseriesProvider;

    private HistoricTimeseriesProviderCombi historicTimeseriesProviderEod;

    public void setHistoricConfigurationMBean(HistoricConfigurationMBean historicConfigurationMBean) {
        this.historicConfigurationMBean = historicConfigurationMBean;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    public void setHistoricTimeseriesProvider(
            HistoricTimeseriesProvider historicTimeseriesProvider) {
        this.historicTimeseriesProvider = historicTimeseriesProvider;
    }

    public void setHistoricTimeseriesProviderEod(
            HistoricTimeseriesProviderCombi historicTimeseriesProviderEod) {
        this.historicTimeseriesProviderEod = historicTimeseriesProviderEod;
    }

    @RequestMapping("/wiso/mghistory.xml")
    protected void mghistory(HttpServletRequest request, HttpServletResponse response, Command cmd)
            throws Exception {
        if (!StringUtils.hasText(cmd.getRequest())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Element resultRoot = new Element("result");
        Document result = new Document(resultRoot);

        final SAXBuilder builder = new SAXBuilder();
        final Document document;
        try {
            document = builder.build(new StringReader(cmd.getRequest()));
        } catch (Exception e) {
            this.logger.warn("<handleHistory> failed to build request document for: " + request, e);
            resultRoot.addContent(getError(ERR_ILLEGAL_REQUEST, e.getMessage()));
            writeResponse(response, result);
            return;
        }

        final Element root = document.getRootElement();
        final String daysStr = root.getAttributeValue("days");
        int days = Math.max(1, Math.min(MAX_DAYS, daysStr != null ? Integer.parseInt(daysStr) : DEFAULT_DAYS));

        final List<Element> items = root.getChildren("item");
        AuditTrailFilter.Trail trail = AuditTrailFilter.getTrail(request);

        try {
            resultRoot.setContent(getHistoricData(request, items, days, cmd.isDebug(), trail));
        } catch (Exception e) {
            this.logger.warn("<handleHistory> getHistoricData failed", e);
            resultRoot.setContent(getError(ERR_NO_SERVICE, "Datenservice nicht verfuegbar"));
        }

        writeResponse(response, result);
    }

    @RequestMapping("/wiso/exchangerate.xml")
    protected void exchangerate(HttpServletResponse response, @RequestParam("symbol") String s) throws Exception {
        if (!StringUtils.hasText(s)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String symbol = s.toUpperCase().trim();
        Element data = new Element("data");
        Document result = new Document(data);
        data.addContent(createElement("generated", GENERATED.print(new DateTime())));
        data.addContent(createElement("symbol", symbol));
        data.addContent(getExchangerate(symbol));
        writeResponse(response, result);
    }

    private Element getExchangerate(String symbol) {
        if (StringUtils.hasText(symbol) && symbol.length() == 6) {
            try {
                return createElement("exchangerate", format(CURRENCY_FORMAT,
                        getCurrencyConversion(symbol.substring(0, 3), symbol.substring(3, 6), new LocalDate())));
            } catch (NoDataException e) {
                // ignore, return null
            }
        }
        return createElement("exchangerate", null);
    }

    private Element createElement(final String name, final String text) {
        final Element result = new Element(name);
        if (StringUtils.hasText(text)) {
            result.setText(text);
        }
        return result;
    }

    private QuoteWithTimeseries getQuote(HttpServletRequest request, String isin, String wkn, String currency,
            LocalDate yesterday, AuditTrailFilter.Trail trail) {

        Instrument instrument = getInstrument(isin, wkn);
        if (instrument == null) {
            return null;
        }

        final List<Quote> quotes = getQuotes(instrument, currency);
        if (quotes.isEmpty()) {
            return null;
        }

        trail.add("quotes for isin=" + isin + ", wkn=" + wkn, quotes);

        LocalDate maxDate = yesterday.minusYears(1);
        QuoteWithTimeseries result = null;

        for (QuoteSelector selector : SELECTORS) {
            final Quote selected = selector.select(instrument, quotes);
            if (selected == null) {
                continue;
            }
            quotes.remove(selected);
            if (selected.getSymbolMmwkn() == null) {
                continue;
            }
            final HistoricTimeseries ht = getTimeseries(request, selected, currency, yesterday);
            if (ht == null) {
                continue;
            }
            LocalDate ld = getLastDayWithValue(ht);
            if (ld == null) {
                continue;
            }
            trail.add("quote", selected.toString() + ": " + ld);
            if (ld.equals(yesterday)) {
                return new QuoteWithTimeseries(selected, ht);
            }
            else if (ld.isAfter(maxDate)) {
                result = new QuoteWithTimeseries(selected, ht);
                maxDate = ld;
            }
            if (quotes.isEmpty()) {
                break;
            }
        }
        return result;
    }

    private Instrument getInstrument(String isin, String wkn) {
        if (IsinUtil.isIsin(isin)) {
            return getInstrument(isin, SymbolStrategyEnum.ISIN);
        }
        if (StringUtils.hasText(wkn)) {
            final Instrument instrument = getInstrument(wkn, SymbolStrategyEnum.WKN);
            return (instrument != null) ? instrument : getInstrument(wkn, SymbolStrategyEnum.MMWKN);
        }
        return null;
    }

    private LocalDate getLastDayWithValue(HistoricTimeseries ht) {
        for (int i = ht.size(); i-- > 0; ) {
            if (!Double.isNaN(ht.getValue(i))) {
                return ht.getStartDay().plusDays(i);
            }
        }
        return null;
    }

    private List<Quote> getQuotes(Instrument instrument, String currency) {
        final Profile p = RequestContextHolder.getRequestContext().getProfile();
        List<Quote> result = ProfiledInstrument.quotesWithPrices(instrument.getQuotes(), p);

        if (instrument.getInstrumentType() == InstrumentTypeEnum.FND) {
            final List<Quote> fonds = FONDS_FILTER.apply(result);
            if (!fonds.isEmpty()) {
                result = fonds;
            }
        }

        return result;
    }

    private HistoricTimeseries getTimeseries(HttpServletRequest request,Quote q, String currency, LocalDate yesterday) {
        final LocalDate then = yesterday.minusDays(MAX_DAYS * 2);
        try {
            final List<HistoricTimeseries> ts = getHistoricTimeseries(request, q, then, yesterday, currency);
            return (ts != null && ts.size() > 0) ? ts.get(0) : null;
        } catch (Exception e) {
            this.logger.warn("<getTimeseries> failed for " + q, e);
            return null;
        }
    }

    private List<HistoricTimeseries> getHistoricTimeseries(HttpServletRequest request, Quote q, LocalDate from, LocalDate to,
            String currency) {
        if (this.historicConfigurationMBean.isEodHistoryEnabled(q)) {
            this.logger.info("<getHistoricTimeseries> use eod history");
            HistoricRequestImpl req = new HistoricRequestImpl(q, from, to)
                    .withSplit(true)
                    .withDividend(q.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND);
            if (StringUtils.hasText(currency) && !currency.equals(q.getCurrency().getSymbolIso())) {
                req = req.withCurrency(currency);
            }
            if (InstrumentUtil.isVwdFund(q)) {
                req.addHistoricTerm(HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Ruecknahme));
            }
            else {
                req.addClose(null);
            }

            return this.historicTimeseriesProviderEod.getTimeseries(req);
        }
        else {
            HistoricTimeseriesRequest req = new HistoricTimeseriesRequest(q, from, to)
                    .withYieldBasedFromQuote()
                    .withSplit(true)
                    .withDividend(q.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND);
            if (StringUtils.hasText(currency) && !currency.equals(q.getCurrency().getSymbolIso())) {
                req = req.withCurrency(currency);
            }

            if (InstrumentUtil.isVwdFund(q)) {
                req.addFundRepurchaingPrice(null);
            }
            else {
                req.addClose(null);
            }

            return this.historicTimeseriesProvider.getTimeseries(req);
        }

    }

    private Instrument getInstrument(String symbol, SymbolStrategyEnum ss) {
        try {
            return this.instrumentProvider.identifyInstrument(symbol, ss);
        } catch (UnknownSymbolException e) {
            return null;
        } catch (Exception e) {
            this.logger.warn("<getInstrument> failed", e);
            return null;
        }
    }

    private List<Element> getHistoricData(HttpServletRequest request, List<Element> items, int days,
            boolean debug,
            AuditTrailFilter.Trail trail) {
        List<Element> result = new ArrayList<>(Math.min(items.size(), REQUEST_LIMIT));

        for (int i = 0; i < items.size() && result.size() < REQUEST_LIMIT; i++) {
            result.add(getElement(request, items.get(i), days, debug, trail));
        }
        return result;
    }

    private Element getElement(HttpServletRequest request, Element item, int days, boolean debug,
            AuditTrailFilter.Trail trail) {
        Element result = new Element("item");

        final String isin = getAttribute(item, "isin");
        final String wkn = getAttribute(item, "wkn");
        final String tmpCurrency = getAttribute(item, "currency");
        final String currency = XXP.equals(tmpCurrency) ? null : tmpCurrency;

        final LocalDate yesterday = getLastWorkDay();

        final QuoteWithTimeseries quoteWithTimeseries = getQuote(request, isin, wkn, currency, yesterday, trail);

        if (quoteWithTimeseries == null) {
            setAttribute(result, "wkn", wkn);
            setAttribute(result, "isin", isin);
            setAttribute(result, "currency", currency);
            result.setContent(getError(ERR_NO_DATA, "Keine Daten, Wkn/Isin moeglicherweise inkorrekt"));
            return result;
        }

        Quote q = quoteWithTimeseries.quote;
        HistoricTimeseries ht = quoteWithTimeseries.ts;
        final boolean withCurrencyConversion
                = StringUtils.hasText(currency) && !currency.equals(q.getCurrency().getSymbolIso());

        setAttribute(result, "wkn", wkn, q.getInstrument().getSymbolWkn());
        setAttribute(result, "isin", isin, q.getInstrument().getSymbolIsin());
        setAttribute(result, "currency", (currency != null) ? currency : q.getCurrency().getSymbolIso());
        if (debug) {
            setAttribute(result, "vwdcode", q.getSymbolVwdcode());
        }

        List<Element> values = new ArrayList<>(days + 1);
        int n = 0;
        for (LocalDate ld = yesterday; ld.isAfter(ht.getStartDay()) && n < days; ld = ld.minusDays(1)) {
            final double value = ht.getValue(ld);
            if (Double.isNaN(value)) {
                continue;
            }
            if (n == 0 && withCurrencyConversion) {
                try {
                    Element e = new Element("exchangerate");
                    e.setAttribute("date", DTF.print(ld));
                    e.setAttribute("value", format(CURRENCY_FORMAT,
                            getCurrencyConversion(q.getCurrency().getSymbolIso(), currency, ld)));
                    values.add(e);
                } catch (NoDataException e1) {
                    result.setContent(getError(ERR_NO_DATA, "Waehrung '" + currency + "' nicht verfuegbar"));
                    return result;
                }
            }
            Element close = new Element("close");
            close.setAttribute("date", DTF.print(ld));
            close.setAttribute("value", format(PRICE_FORMAT, value));
            values.add(close);
            n++;
        }
        result.setContent(values);

        return result;
    }

    private LocalDate getLastWorkDay() {
        final LocalDate today = new LocalDate();
        switch (today.getDayOfWeek()) {
            case DateTimeConstants.MONDAY:
                return today.minusDays(3);
            case DateTimeConstants.SUNDAY:
                return today.minusDays(2);
            default:
                return today.minusDays(1);
        }
    }

    private String getAttribute(Element item, final String name) {
        final String value = item.getAttributeValue(name);
        return (value != null) ? value.toUpperCase().trim() : null;
    }

    private double getCurrencyConversion(String from, String to, LocalDate ld) {
        final IsoCurrencyConversionProviderImpl.ConversionResult conversion
                = this.isoCurrencyConversionProvider.getConversion(from, to, ld);
        return conversion.getFactor().doubleValue();
    }

    private void setAttribute(Element el, String name, String value, String defaultValue) {
        el.setAttribute(name, StringUtils.hasText(value)
                ? value : (defaultValue != null ? defaultValue : ""));
    }

    private void setAttribute(Element el, String name, String value) {
        setAttribute(el, name, value, "");
    }

    private void writeResponse(HttpServletResponse response, Document result) throws IOException {
        response.setContentType("text/xml");
        response.setStatus(HttpServletResponse.SC_OK);
        final XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
        xmlOutputter.output(result, response.getWriter());
    }

    private Element getError(int id, String message) {
        Element e = new Element("error");
        e.setAttribute("id", "" + id);
        e.addContent(new CDATA(message));
        return e;
    }

    private String format(NumberFormat nf, double val) {
        synchronized (nf) {
            return nf.format(val);
        }
    }
}
