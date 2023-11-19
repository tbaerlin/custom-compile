/*
 * KwtSmartclient.java
 *
 * Created on 07.04.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.Warrant;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.QuoteRatiosBND;
import de.marketmaker.istar.ratios.frontend.QuoteRatiosFND;
import de.marketmaker.istar.ratios.frontend.QuoteRatiosSTK;
import de.marketmaker.istar.ratios.frontend.QuoteRatiosWNT;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class KwtSmartclient {
    public static class Command {
        private String[] wkns = new String[31];
        private String[] isins = new String[31];
        private String name;
        private String sym;

        public void setWkn(String wkn) {
            this.wkns[0] = wkn;
        }

        public void setWkn1(String wkn1) {
            this.wkns[1] = wkn1;
        }

        public void setWkn2(String wkn2) {
            this.wkns[2] = wkn2;
        }

        public void setWkn3(String wkn3) {
            this.wkns[3] = wkn3;
        }

        public void setWkn4(String wkn4) {
            this.wkns[4] = wkn4;
        }

        public void setWkn5(String wkn5) {
            this.wkns[5] = wkn5;
        }

        public void setWkn6(String wkn6) {
            this.wkns[6] = wkn6;
        }

        public void setWkn7(String wkn7) {
            this.wkns[7] = wkn7;
        }

        public void setWkn8(String wkn8) {
            this.wkns[8] = wkn8;
        }

        public void setWkn9(String wkn9) {
            this.wkns[9] = wkn9;
        }

        public void setWkn10(String wkn10) {
            this.wkns[10] = wkn10;
        }

        public void setWkn11(String wkn11) {
            this.wkns[11] = wkn11;
        }

        public void setWkn12(String wkn12) {
            this.wkns[12] = wkn12;
        }

        public void setWkn13(String wkn13) {
            this.wkns[13] = wkn13;
        }

        public void setWkn14(String wkn14) {
            this.wkns[14] = wkn14;
        }

        public void setWkn15(String wkn15) {
            this.wkns[15] = wkn15;
        }

        public void setWkn16(String wkn16) {
            this.wkns[16] = wkn16;
        }

        public void setWkn17(String wkn17) {
            this.wkns[17] = wkn17;
        }

        public void setWkn18(String wkn18) {
            this.wkns[18] = wkn18;
        }

        public void setWkn19(String wkn19) {
            this.wkns[19] = wkn19;
        }

        public void setWkn20(String wkn20) {
            this.wkns[20] = wkn20;
        }

        public void setWkn21(String wkn21) {
            this.wkns[21] = wkn21;
        }

        public void setWkn22(String wkn22) {
            this.wkns[22] = wkn22;
        }

        public void setWkn23(String wkn23) {
            this.wkns[23] = wkn23;
        }

        public void setWkn24(String wkn24) {
            this.wkns[24] = wkn24;
        }

        public void setWkn25(String wkn25) {
            this.wkns[25] = wkn25;
        }

        public void setWkn26(String wkn26) {
            this.wkns[26] = wkn26;
        }

        public void setWkn27(String wkn27) {
            this.wkns[27] = wkn27;
        }

        public void setWkn28(String wkn28) {
            this.wkns[28] = wkn28;
        }

        public void setWkn29(String wkn29) {
            this.wkns[29] = wkn29;
        }

        public void setWkn30(String wkn30) {
            this.wkns[30] = wkn30;
        }

        public void setIsin(String isin) {
            this.isins[0] = isin;
        }


        public void setIsin1(String isin1) {
            this.isins[1] = isin1;
        }

        public void setIsin2(String isin2) {
            this.isins[2] = isin2;
        }

        public void setIsin3(String isin3) {
            this.isins[3] = isin3;
        }

        public void setIsin4(String isin4) {
            this.isins[4] = isin4;
        }

        public void setIsin5(String isin5) {
            this.isins[5] = isin5;
        }

        public void setIsin6(String isin6) {
            this.isins[6] = isin6;
        }

        public void setIsin7(String isin7) {
            this.isins[7] = isin7;
        }

        public void setIsin8(String isin8) {
            this.isins[8] = isin8;
        }

        public void setIsin9(String isin9) {
            this.isins[9] = isin9;
        }

        public void setIsin10(String isin10) {
            this.isins[10] = isin10;
        }

        public void setIsin11(String isin11) {
            this.isins[11] = isin11;
        }

        public void setIsin12(String isin12) {
            this.isins[12] = isin12;
        }

        public void setIsin13(String isin13) {
            this.isins[13] = isin13;
        }

        public void setIsin14(String isin14) {
            this.isins[14] = isin14;
        }

        public void setIsin15(String isin15) {
            this.isins[15] = isin15;
        }

        public void setIsin16(String isin16) {
            this.isins[16] = isin16;
        }

        public void setIsin17(String isin17) {
            this.isins[17] = isin17;
        }

        public void setIsin18(String isin18) {
            this.isins[18] = isin18;
        }

        public void setIsin19(String isin19) {
            this.isins[19] = isin19;
        }

        public void setIsin20(String isin20) {
            this.isins[20] = isin20;
        }

        public void setIsin21(String isin21) {
            this.isins[21] = isin21;
        }

        public void setIsin22(String isin22) {
            this.isins[22] = isin22;
        }

        public void setIsin23(String isin23) {
            this.isins[23] = isin23;
        }

        public void setIsin24(String isin24) {
            this.isins[24] = isin24;
        }

        public void setIsin25(String isin25) {
            this.isins[25] = isin25;
        }

        public void setIsin26(String isin26) {
            this.isins[26] = isin26;
        }

        public void setIsin27(String isin27) {
            this.isins[27] = isin27;
        }

        public void setIsin28(String isin28) {
            this.isins[28] = isin28;
        }

        public void setIsin29(String isin29) {
            this.isins[29] = isin29;
        }

        public void setIsin30(String isin30) {
            this.isins[30] = isin30;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSym() {
            return sym;
        }

        public void setSym(String sym) {
            this.sym = sym;
        }

        List<String> getWkns() {
            return Arrays.stream(this.wkns)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        List<String> getIsins() {
            return Arrays.stream(this.isins)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }
    }

    private static final DecimalFormat NUMBERFORMAT0 = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
    private static final DecimalFormat NUMBERFORMAT2 = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
    private static final DecimalFormat NUMBERFORMAT23 = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
    private static final DecimalFormat NUMBERFORMAT4 = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);

    static {
        NUMBERFORMAT0.applyLocalizedPattern("0");
        NUMBERFORMAT2.applyLocalizedPattern("0,00");
        NUMBERFORMAT23.applyLocalizedPattern("0,00#");
        NUMBERFORMAT4.applyLocalizedPattern("0,0000");
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;
    private IntradayProvider intradayProvider;
    private RatiosProvider ratiosProvider;
    private StockDataProvider stockDataProvider;
    private IsoCurrencyConversionProvider currencyConversionProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    public void setCurrencyConversionProvider(IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    @RequestMapping(value = "/fusion/kwt_smartclient.html")
    protected void handle(HttpServletRequest request,
                                  HttpServletResponse response, Command cmd) throws Exception {
        final List<String> wkns = cmd.getWkns();
        final List<Quote> quotes = new ArrayList<>(this.instrumentProvider.identifyQuotes(wkns, SymbolStrategyEnum.WKN, null, null));
        final List<String> requestMarkets = new ArrayList<>(Collections.<String>nCopies(quotes.size(), null));
        if (StringUtils.hasText(cmd.getName())) {
            final SimpleSearchCommand command = new SimpleSearchCommand(cmd.getName(), 0, 50, 50, true);
            final SearchResponse searchResponse = this.instrumentProvider.simpleSearch(command);
            if (searchResponse.isValid() && !searchResponse.getInstruments().isEmpty()) {
                for (final Instrument instrument : searchResponse.getInstruments()) {
                    quotes.add(this.instrumentProvider.getQuote(instrument, null, null));
                    requestMarkets.add(null);
                }
            }
        }
        if (StringUtils.hasText(cmd.getSym())) {
            quotes.add(this.instrumentProvider.identifyQuote(cmd.getSym(), SymbolStrategyEnum.VWDCODE, null, null));
            requestMarkets.add(null);
        }
        for (final String isin : cmd.getIsins()) {
            if (isin != null && (IsinUtil.isIsin(isin) || isin.indexOf("|") > 0)) {
                final String[] strings = isin.split("\\|");

                try {
                    quotes.add(this.instrumentProvider.identifyQuote(strings[0],
                            SymbolStrategyEnum.ISIN, strings.length == 1 ? null : strings[1], null));
                    requestMarkets.add(strings.length > 1 ? strings[1] : null);
                }
                catch (Exception exception) {
                    this.logger.info("<handle> failed for " + strings[0], exception);
                }
            }
        }

        CollectionUtils.removeNulls(quotes, requestMarkets);

        if (quotes.isEmpty()) {
            this.logger.info("<handle> no quotes, parameter map is: " + request.getParameterMap());
        }

        final Map<Long, Item> items = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final String requestMarket = requestMarkets.get(i);

            final Item item = new Item(quote.getId());
            item.setWkn(quote.getInstrument().getSymbolWkn());
            item.setIsin(quote.getInstrument().getSymbolIsin());
            item.setName(quote.getInstrument().getName());
            item.setMarket(quote.getSymbolVwdfeedMarket());
            item.setCurrency(quote.getCurrency().getSymbolIso());
            item.setRequestMarket(requestMarket);

            if (quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.STK) {
                final MasterDataStock data =
                    this.stockDataProvider.getMasterData(quote.getInstrument().getId(),
                        RequestContextHolder.getRequestContext().getProfile());
                item.setDividend(data.getDividend());
                item.setDividendCurrency(data.getDividendCurrency());
                item.setDividendDate(data.getDividendExDay() == null ? null : data.getDividendExDay().toDateTimeAtStartOfDay());
            }
            else if (quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.WNT) {
                final Warrant warrant = (Warrant) quote.getInstrument();
                item.setSubscriptionRatio(warrant.getSubscriptionRatio());
            }

            items.put(quote.getId(), item);
        }

        final Map<String, IsoCurrencyConversionProviderImpl.ConversionResult> currencies
                = new HashMap<>();

        final List<PriceRecord> prs = this.intradayProvider.getPriceRecords(quotes);
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord pr = prs.get(i);

            final Item item = items.get(quote.getId());
            if (pr.getPrice() != null) {
                item.setPrice(pr.getPrice().getValue());
                item.setDate(pr.getPrice().getDate());
            }

            final String currency = quote.getCurrency().getSymbolIso();
            if (!"EUR".equals(currency)) {
                IsoCurrencyConversionProviderImpl.ConversionResult cr = currencies.get(currency);

                if (cr == null) {
                    cr = this.currencyConversionProvider.getConversion(currency, "EUR");
                    currencies.put(currency, cr);
                }

                item.setRateToEur(cr.getRate().getValue());
                item.setRateDate(cr.getRate().getDate());
            }
            else {
                item.setRateToEur(BigDecimal.ONE);
            }
        }

        doFieldsSTK(quotes, items);
        doFieldsWNT(quotes, items);
        doFieldsFND(quotes, items);
        doFieldsBND(quotes, items);

        response.setContentType("text/xml;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        try (OutputStreamWriter w = new OutputStreamWriter(response.getOutputStream(), "UTF-8")) {
            write(w, quotes, items);
        }
    }

    private void write(Writer writer, List<Quote> quotes, Map<Long, Item> items) throws Exception {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        writer.write("<Kursinfo>");
        for (final Quote quote : quotes) {
            final Item item = items.get(quote.getId());
            writer.write(tag("WKN", item.getWkn()));
            writer.write(tag("AnfrageBPL", item.getRequestMarket() != null ? item.getRequestMarket() : ""));
            writer.write(tag("ISIN", item.getIsin()));
            writer.write(tag("WpName", item.getName()));
            writer.write(tag("AktuellerKurs", item.getPrice(),
                    quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.CUR ? NUMBERFORMAT4 : NUMBERFORMAT23));
            writer.write(tag("KursOrt", item.getMarket()));
            writer.write(tag("KursDatum", item.getDate() == null ? null : item.getDate().toString("dd.MM.yyyy")));
            writer.write(tag("KursUhrzeit", item.getDate() == null ? null : item.getDate().toString("HH:mm:ss")));
            writer.write(tag("KursWaehrung", item.getCurrency()));
            writer.write(tag("DevisenUmrechnungsKurs", item.getRateToEur(), NUMBERFORMAT4));
            writer.write(tag("DevisenKursDatum", item.getRateDate() == null ? null : item.getRateDate().toString("dd.MM.yyyy")));
            writer.write(tag("DevisenKursUhrzeit", item.getRateDate() == null ? null : item.getRateDate().toString("HH:mm:ss")));
            writer.write(tag("KGV", item.getKgv(), NUMBERFORMAT2));
            writer.write(tag("KCV", item.getKcv(), NUMBERFORMAT2));
            writer.write(tag("Volatilitaet30T", item.getVolatility1m(), true));
            writer.write(tag("Volatilitaet1J", item.getVolatility1y(), true));
            writer.write(tag("Beta1W", item.getBeta1w(), NUMBERFORMAT2));
            writer.write(tag("Beta1M", item.getBeta1m(), NUMBERFORMAT2));
            writer.write(tag("Beta3M", item.getBeta3m(), NUMBERFORMAT2));
            writer.write(tag("Beta6M", item.getBeta6m(), NUMBERFORMAT2));
            writer.write(tag("Beta1J", item.getBeta1y(), NUMBERFORMAT2));
            writer.write(tag("Beta3J", item.getBeta3y(), NUMBERFORMAT2));
            writer.write(tag("Beta5J", item.getBeta5y(), NUMBERFORMAT2));
            writer.write(tag("Beta7J", item.getBeta7y(), NUMBERFORMAT2));
            writer.write(tag("Beta10J", item.getBeta10y(), NUMBERFORMAT2));
            writer.write(tag("Korrelation52W", item.getCorrelation1y(), NUMBERFORMAT2));
            writer.write(tag("FondArt", item.getFundtype()));
            writer.write(tag("FondsVolumen", item.getFundvolume(), NUMBERFORMAT0));
            writer.write(tag("KAG", item.getIssuername()));
            writer.write(tag("SharpeRatio", item.getSharpeRatio3y(), NUMBERFORMAT2));
            writer.write(tag("TreynorRatio", item.getTreynorRatio3y(), NUMBERFORMAT2));
            writer.write(tag("WarrantDelta", item.getDelta(), NUMBERFORMAT2));
            writer.write(tag("WarrantRatio", item.getSubscriptionRatio(), NUMBERFORMAT2));
            writer.write(tag("PerformanceBenchmark52W", item.getPerformanceToBenchmark1y(), true));
            writer.write(tag("BenchmarkFonds", item.getBenchmark()));
            writer.write(tag("Rendite", item.getYield(), true));
            writer.write(tag("PriceValueBasisPoint", item.getBpv(), NUMBERFORMAT2));
            writer.write(tag("LetzteDividende", item.getDividend(), NUMBERFORMAT2));
            writer.write(tag("WaehrungDividende", item.getDividendCurrency()));
            writer.write(tag("DatumDividende", item.getDividendDate() == null ? null : item.getDividendDate().toString("dd.MM.yyyy")));
        }
        writer.write("</Kursinfo>");
    }

    private String tag(String tagname, BigDecimal price, boolean percent) {
        if (price == null) {
            return tag(tagname, null);
        }
        return tag(tagname, percent ? price.movePointRight(2) : price, NUMBERFORMAT2);
    }

    private String tag(String tagname, BigDecimal price, NumberFormat format) {
        if (price == null) {
            return tag(tagname, null);
        }
        return tag(tagname, format(price, format));
    }

    private String format(BigDecimal price, NumberFormat format) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (format) {
            return format.format(price);
        }
    }

    private String tag(String tagname, String value) {
        return "<" + tagname + ">"
                + (value == null ? "n/a" : XmlUtil.encode(value))
                + "</" + tagname + ">\n";
    }


    private void doFieldsSTK(List<Quote> quotes, Map<Long, Item> items) {
        final RatioSearchResponse response = this.ratiosProvider.search(createRequest(quotes, InstrumentTypeEnum.STK));
        if (response.isValid()) {
            final DefaultRatioSearchResponse r = (DefaultRatioSearchResponse) response;
            for (final RatioDataResult data : r.getElements()) {
                final Item item = items.get(data.getQuoteid());
                if (item == null) {
                    continue;
                }
                final QuoteRatiosSTK stk = (QuoteRatiosSTK) data.getQuoteData();
                item.setKgv(getPrice(stk.getFactsetcurrentpriceearningratio1y()));
                item.setKcv(getPrice(stk.getFactsetcurrentpricecashflowratio1y()));
                item.setVolatility1m(getPrice(stk.getVolatility1m()));
                item.setVolatility1y(getPrice(stk.getVolatility1y()));
                item.setBeta1w(getPrice(stk.getBeta1w()));
                item.setBeta1m(getPrice(stk.getBeta1m()));
                item.setBeta3m(getPrice(stk.getBeta3m()));
                item.setBeta6m(getPrice(stk.getBeta6m()));
                item.setBeta1y(getPrice(stk.getBeta1y()));
                item.setBeta3y(getPrice(stk.getBeta3y()));
                item.setBeta5y(getPrice(stk.getBeta5y()));
                item.setBeta10y(getPrice(stk.getBeta10y()));
                item.setCorrelation1y(getPrice(stk.getCorrelation1y()));
                item.setPerformanceToBenchmark1y(getPrice(stk.getPerformancetobenchmark1y()));
                item.setBenchmark(stk.getInstrumentRatios().getBenchmarkName());
            }
        }
    }

    private void doFieldsFND(List<Quote> quotes, Map<Long, Item> items) {
        final RatioSearchResponse response = this.ratiosProvider.search(createRequest(quotes, InstrumentTypeEnum.FND));
        if (response.isValid()) {
            final DefaultRatioSearchResponse r = (DefaultRatioSearchResponse) response;
            for (final RatioDataResult data : r.getElements()) {
                final Item item = items.get(data.getQuoteid());
                if (item == null) {
                    continue;
                }
                final QuoteRatiosFND fnd = (QuoteRatiosFND) data.getQuoteData();
                item.setVolatility1m(getPrice(fnd.getVolatility1m()));
                item.setVolatility1y(getPrice(fnd.getVolatility1y()));
                item.setBeta1w(getPrice(fnd.getBeta1w()));
                item.setBeta1m(getPrice(fnd.getBeta1m()));
                item.setBeta3m(getPrice(fnd.getBeta3m()));
                item.setBeta6m(getPrice(fnd.getBeta6m()));
                item.setBeta1y(getPrice(fnd.getBeta1y()));
                item.setBeta3y(getPrice(fnd.getBeta3y()));
                item.setBeta5y(getPrice(fnd.getBeta5y()));
                item.setBeta10y(getPrice(fnd.getBeta10y()));
                item.setCorrelation1y(getPrice(fnd.getCorrelation1y()));
                item.setPerformanceToBenchmark1y(getPrice(fnd.getPerformancetobenchmark1y()));
                item.setBenchmark(fnd.getInstrumentRatios().getMsBenchmarkName());
                item.setFundtype(fnd.getInstrumentRatios().getMsBroadassetclass(0));
                item.setFundvolume(new BigDecimal(fnd.getInstrumentRatios().getMsFundVolume()));
                item.setIssuername(fnd.getInstrumentRatios().getMsIssuername());
                item.setSharpeRatio3y(getPrice(fnd.getSharperatio3y()));
                item.setTreynorRatio3y(getPrice(fnd.getTreynor3y()));
            }
        }
    }

    private void doFieldsBND(List<Quote> quotes, Map<Long, Item> items) {
        final RatioSearchResponse response = this.ratiosProvider.search(createRequest(quotes, InstrumentTypeEnum.BND));
        if (response.isValid()) {
            final DefaultRatioSearchResponse r = (DefaultRatioSearchResponse) response;
            for (final RatioDataResult data : r.getElements()) {
                final Item item = items.get(data.getQuoteid());
                if (item == null) {
                    continue;
                }
                final QuoteRatiosBND bnd = (QuoteRatiosBND) data.getQuoteData();
                item.setVolatility1m(getPrice(bnd.getVolatility1m()));
                item.setVolatility1y(getPrice(bnd.getVolatility1y()));
                item.setBeta1w(getPrice(bnd.getBeta1w()));
                item.setBeta1m(getPrice(bnd.getBeta1m()));
                item.setBeta3m(getPrice(bnd.getBeta3m()));
                item.setBeta6m(getPrice(bnd.getBeta6m()));
                item.setBeta1y(getPrice(bnd.getBeta1y()));
                item.setBeta3y(getPrice(bnd.getBeta3y()));
                item.setBeta5y(getPrice(bnd.getBeta5y()));
                item.setBeta10y(getPrice(bnd.getBeta10y()));
                item.setCorrelation1y(getPrice(bnd.getCorrelation1y()));
                item.setYield(getPrice(bnd.getYieldRelative_mdps()));
                item.setBpv(getPrice(bnd.getMdpsbasepointvalue()));
            }
        }
    }

    private void doFieldsWNT(List<Quote> quotes, Map<Long, Item> items) {
        final RatioSearchResponse response = this.ratiosProvider.search(createRequest(quotes, InstrumentTypeEnum.WNT));
        if (response.isValid()) {
            final DefaultRatioSearchResponse r = (DefaultRatioSearchResponse) response;
            for (final RatioDataResult data : r.getElements()) {
                final Item item = items.get(data.getQuoteid());
                if (item == null) {
                    continue;
                }
                final QuoteRatiosWNT wnt = (QuoteRatiosWNT) data.getQuoteData();
                item.setVolatility1m(getPrice(wnt.getVolatility1m()));
                item.setVolatility1y(getPrice(wnt.getVolatility1y()));
                item.setDelta(getPrice(wnt.getDelta()));
            }
        }
    }

    private RatioSearchRequest createRequest(List<Quote> quotes, InstrumentTypeEnum type) {
        final StringBuilder sb = new StringBuilder();
        final List<Long> iids = new ArrayList<>();
        for (final Quote quote : quotes) {
            if (quote.getInstrument().getInstrumentType() != type) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("@");
            }
            sb.append(quote.getId());
            iids.add(quote.getInstrument().getId());
        }

        final RatioSearchRequest request = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        request.setType(type);
        request.setInstrumentIds(iids);
        request.addParameter("n", Integer.toString(iids.size()));
        request.addParameter("qid", sb.toString());
        return request;
    }

    private BigDecimal getPrice(Long value) {
        if (value == null || value == Long.MIN_VALUE) {
            return null;
        }

        return new BigDecimal(value).movePointLeft(5);
    }

    public static class Item {
        private final Long qid;
        private String wkn;
        private String requestMarket;
        private String isin;
        private String name;
        private BigDecimal price;
        private String market;
        private DateTime date;
        private String currency;
        private BigDecimal rateToEur;
        private DateTime rateDate;
        private BigDecimal kgv;
        private BigDecimal kcv;
        private BigDecimal volatility1m;
        private BigDecimal volatility1y;
        private BigDecimal beta1w;
        private BigDecimal beta1m;
        private BigDecimal beta3m;
        private BigDecimal beta6m;
        private BigDecimal beta1y;
        private BigDecimal beta3y;
        private BigDecimal beta5y;
        private BigDecimal beta7y;
        private BigDecimal beta10y;
        private BigDecimal correlation1y;
        private String fundtype;
        private BigDecimal fundvolume;
        private String issuername;
        private BigDecimal sharpeRatio3y;
        private BigDecimal treynorRatio3y;
        private BigDecimal delta;
        private BigDecimal subscriptionRatio;
        private BigDecimal performanceToBenchmark1y;
        private String benchmark;
        private BigDecimal yield;
        private BigDecimal bpv;
        private BigDecimal dividend;
        private String dividendCurrency;
        private DateTime dividendDate;

        public Item(Long qid) {
            this.qid = qid;
        }

        public Long getQid() {
            return qid;
        }

        public String getWkn() {
            return wkn;
        }

        public void setWkn(String wkn) {
            this.wkn = wkn;
        }

        public String getRequestMarket() {
            return requestMarket;
        }

        public void setRequestMarket(String requestMarket) {
            this.requestMarket = requestMarket;
        }

        public String getIsin() {
            return isin;
        }

        public void setIsin(String isin) {
            this.isin = isin;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        public DateTime getDate() {
            return date;
        }

        public void setDate(DateTime date) {
            this.date = date;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public BigDecimal getRateToEur() {
            return rateToEur;
        }

        public void setRateToEur(BigDecimal rateToEur) {
            this.rateToEur = rateToEur;
        }

        public DateTime getRateDate() {
            return rateDate;
        }

        public void setRateDate(DateTime rateDate) {
            this.rateDate = rateDate;
        }

        public BigDecimal getKgv() {
            return kgv;
        }

        public void setKgv(BigDecimal kgv) {
            this.kgv = kgv;
        }

        public BigDecimal getKcv() {
            return kcv;
        }

        public void setKcv(BigDecimal kcv) {
            this.kcv = kcv;
        }

        public BigDecimal getVolatility1m() {
            return volatility1m;
        }

        public void setVolatility1m(BigDecimal volatility1m) {
            this.volatility1m = volatility1m;
        }

        public BigDecimal getVolatility1y() {
            return volatility1y;
        }

        public void setVolatility1y(BigDecimal volatility1y) {
            this.volatility1y = volatility1y;
        }

        public BigDecimal getBeta1w() {
            return beta1w;
        }

        public void setBeta1w(BigDecimal beta1w) {
            this.beta1w = beta1w;
        }

        public BigDecimal getBeta1m() {
            return beta1m;
        }

        public void setBeta1m(BigDecimal beta1m) {
            this.beta1m = beta1m;
        }

        public BigDecimal getBeta3m() {
            return beta3m;
        }

        public void setBeta3m(BigDecimal beta3m) {
            this.beta3m = beta3m;
        }

        public BigDecimal getBeta6m() {
            return beta6m;
        }

        public void setBeta6m(BigDecimal beta6m) {
            this.beta6m = beta6m;
        }

        public BigDecimal getBeta1y() {
            return beta1y;
        }

        public void setBeta1y(BigDecimal beta1y) {
            this.beta1y = beta1y;
        }

        public BigDecimal getBeta3y() {
            return beta3y;
        }

        public void setBeta3y(BigDecimal beta3y) {
            this.beta3y = beta3y;
        }

        public BigDecimal getBeta5y() {
            return beta5y;
        }

        public void setBeta5y(BigDecimal beta5y) {
            this.beta5y = beta5y;
        }

        public BigDecimal getBeta7y() {
            return beta7y;
        }

        public void setBeta7y(BigDecimal beta7y) {
            this.beta7y = beta7y;
        }

        public BigDecimal getBeta10y() {
            return beta10y;
        }

        public void setBeta10y(BigDecimal beta10y) {
            this.beta10y = beta10y;
        }

        public BigDecimal getCorrelation1y() {
            return correlation1y;
        }

        public void setCorrelation1y(BigDecimal correlation1y) {
            this.correlation1y = correlation1y;
        }

        public String getFundtype() {
            return fundtype;
        }

        public void setFundtype(String fundtype) {
            this.fundtype = fundtype;
        }

        public BigDecimal getFundvolume() {
            return fundvolume;
        }

        public void setFundvolume(BigDecimal fundvolume) {
            this.fundvolume = fundvolume;
        }

        public String getIssuername() {
            return issuername;
        }

        public void setIssuername(String issuername) {
            this.issuername = issuername;
        }

        public BigDecimal getSharpeRatio3y() {
            return sharpeRatio3y;
        }

        public void setSharpeRatio3y(BigDecimal sharpeRatio3y) {
            this.sharpeRatio3y = sharpeRatio3y;
        }

        public BigDecimal getTreynorRatio3y() {
            return treynorRatio3y;
        }

        public void setTreynorRatio3y(BigDecimal treynorRatio3y) {
            this.treynorRatio3y = treynorRatio3y;
        }

        public BigDecimal getDelta() {
            return delta;
        }

        public void setDelta(BigDecimal delta) {
            this.delta = delta;
        }

        public BigDecimal getSubscriptionRatio() {
            return subscriptionRatio;
        }

        public void setSubscriptionRatio(BigDecimal subscriptionRatio) {
            this.subscriptionRatio = subscriptionRatio;
        }

        public BigDecimal getPerformanceToBenchmark1y() {
            return performanceToBenchmark1y;
        }

        public void setPerformanceToBenchmark1y(BigDecimal performanceToBenchmark1y) {
            this.performanceToBenchmark1y = performanceToBenchmark1y;
        }

        public String getBenchmark() {
            return benchmark;
        }

        public void setBenchmark(String benchmark) {
            this.benchmark = benchmark;
        }

        public BigDecimal getYield() {
            return yield;
        }

        public void setYield(BigDecimal yield) {
            this.yield = yield;
        }

        public BigDecimal getBpv() {
            return bpv;
        }

        public void setBpv(BigDecimal bpv) {
            this.bpv = bpv;
        }

        public BigDecimal getDividend() {
            return dividend;
        }

        public void setDividend(BigDecimal dividend) {
            this.dividend = dividend;
        }

        public String getDividendCurrency() {
            return dividendCurrency;
        }

        public void setDividendCurrency(String dividendCurrency) {
            this.dividendCurrency = dividendCurrency;
        }

        public DateTime getDividendDate() {
            return dividendDate;
        }

        public void setDividendDate(DateTime dividendDate) {
            this.dividendDate = dividendDate;
        }
    }
}
