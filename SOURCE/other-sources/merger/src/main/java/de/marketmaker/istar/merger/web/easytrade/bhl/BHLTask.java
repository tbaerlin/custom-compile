/*
 * BHLTask.java
 *
 * Created on 16.11.12 15:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.bhl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelector;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;

/**
 * @author tkiesgen
 */
public class BHLTask {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final QuoteSelector GERMANY_STOCK = QuoteSelectors.byMarket("ETR,EEU,EUS,ETRI,XETF,FFM,FFMFO,FFMST,DDF,MCH,STG,HBG,BLN,HNV");

    private static final QuoteSelector GERMANY_DERIVATIVE = QuoteSelectors.byMarket("STG,EUWAX,FFMST");

    private static final QuoteSelector PRIO_MARKETS = QuoteSelectors.byMarket("IT,CH");

    static final String RESULT_FILE_NAME = "result.csv";

    static final String MESSAGES_FILE_NAME = "unknown.txt";

    static final String PROBLEMS_FILE_NAME = "problems.log";

    private static final String EOL = "\r\n";

    private static final Duration DURATION_DAY = new Duration(DateTimeConstants.MILLIS_PER_DAY);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File dir;

    private PrintWriter resultWriter;

    private PrintWriter unknownWriter;

    /**
     * lines from input file, assume all lines fit into memory
     */
    private List<String> lines = new ArrayList<>(1000);

    @SuppressWarnings("FieldCanBeLocal")
    private int numProcessed = 0;

    private final BHLFundManagementEvaluation controller;

    private AtomicInteger pctComplete = new AtomicInteger(0);

    private final RequestContext requestContext;

    private final boolean testVersion;

    BHLTask(BHLFundManagementEvaluation controller, RequestContext requestContext, File dir,
            boolean testVersion) {
        this.controller = controller;
        this.requestContext = requestContext;
        this.dir = dir;
        this.testVersion = testVersion;
    }

    int getStatus() {
        return this.pctComplete.get();
    }

    File getDir() {
        return this.dir;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    void execute() throws Exception {
        this.pctComplete.set(1);
        final File resultFile = new File(this.dir, RESULT_FILE_NAME);
        final File messagesFile = new File(this.dir, MESSAGES_FILE_NAME);
        try {
            this.resultWriter = new PrintWriter(resultFile, "UTF-8");
            this.unknownWriter = new PrintWriter(messagesFile, "UTF-8");
            processRequest();
        } finally {
            IoUtils.close(this.resultWriter);
            IoUtils.close(this.unknownWriter);
        }
        this.pctComplete.set(100);
    }


    private void processRequest() throws Exception {
        try (final Scanner sc = new Scanner(new File(this.dir, "request.txt"), "UTF-8")) {
            appendResultHeader();
            readSymbols(sc);
            process();
        }

    }

    private void appendResultHeader() {
        this.resultWriter.print("#request-date;request-wkn;request-currency;request-isin;request-iso-market;vwdCode;iso-market;currency;price;date;volume;supplement;pricesource");
        this.resultWriter.print(EOL);
    }

    private void readSymbols(Scanner sc) {
        while (sc.hasNextLine()) {
            this.lines.add(sc.nextLine().trim());
        }
    }

    private void process() {
        final int batchSize = 100;

        for (int i = 0; i <= this.lines.size() / batchSize; i++) {
            final List<String> batch = this.lines.subList(i * batchSize, Math.min((i + 1) * batchSize, this.lines.size()));

            process(batch);
        }
    }

    private void process(List<String> lines) {
        final InstrumentRequest ir = new InstrumentRequest();

        final List<Item> items = new ArrayList<>();

        for (final String line : lines) {
            final String[] tokens = line.split(Pattern.quote(";"));

            final LocalDate date = DTF.parseDateTime(extract(tokens[0])).toLocalDate();
            final String currency = extract(tokens[2]);
            final String isin = extract(tokens[3]);
            final String market = extract(tokens[4]);

            final Item item = new Item(line, date, currency, isin, market);
            items.add(item);

            ir.addItem(item.getIsin(), InstrumentRequest.KeyType.ISIN);
        }

        final Map<String, Instrument> isin2instrument = new HashMap<>();

        final InstrumentResponse response = this.controller.getInstrumentServer().identify(ir);
        if (response.isValid()) {
            response.getInstruments().stream().filter(instrument -> instrument != null).forEach(instrument -> isin2instrument.put(instrument.getSymbolIsin(), instrument));
        }

        for (final Item item : items) {
            final Instrument instrument = isin2instrument.get(item.getIsin());
            try {
                handle(item, instrument);
            } catch (Exception e) {
                logProblem(item.getLine(), e);
                print(item);
            }

            incPctComplete();
        }

        RequestContextHolder.getRequestContext().clearIntradayContext();
    }

    private void handle(Item item, Instrument instrument) {
        if (instrument == null) {
            this.unknownWriter.write(item.getLine());
            this.unknownWriter.print(EOL);

            print(item);
            return;
        }

        final List<Quote> prioQuotesByCurrency = getQuotes(instrument, item.getCurrency(), true);
        if (!prioQuotesByCurrency.isEmpty()) {
            handle("1", item, instrument, prioQuotesByCurrency);
        }

        final List<Quote> quotesByCurrency = getQuotes(instrument, item.getCurrency(), false);
        if (!item.isValid() && quotesByCurrency.isEmpty()) {
            handle("2", item, instrument, quotesByCurrency);
        }

        final List<Quote> quotes = getQuotes(instrument, null, false);
        if (!item.isValid() && !quotes.isEmpty()) {
            handle("3", item, instrument, quotes);
        }

        print(item);
    }

    private void handle(String run, Item item, Instrument instrument, List<Quote> quotes) {
        final Quote quoteByMarket = getQuoteByMarket(item.getMarket(), quotes);
        eval(run + "-1", item, quoteByMarket);

        if (!item.isValid()) {
            if (instrument.getInstrumentType() == InstrumentTypeEnum.FND) {
                final Quote quote = getQuote("FONDS", quotes);
                eval(run + "-2", item, quote);
            }
            else if (instrument.getInstrumentType() == InstrumentTypeEnum.BND) {
                if (this.testVersion) {
                    final Quote quote = getQuote("DBAU", quotes);
                    eval(run + "-3-a", item, quote, true);
                    if (!item.isValid()) {
                        final Quote quoteB = getQuote("BAADER", quotes);
                        eval(run + "-3-b", item, quoteB, true);
                    }
                    if (!item.isValid()) {
                        final Quote quoteC = getQuote("MCHMM", quotes);
                        eval(run + "-3-c", item, quoteC, true);
                    }
                    if (!item.isValid()) {
                        final Quote quoteD = getQuote("LUS", quotes);
                        eval(run + "-3-d", item, quoteD, true);
                    }
                    if (!item.isValid()) {
                        final Quote quoteE = getQuote("XCOR", quotes);
                        eval(run + "-3-e", item, quoteE, true);
                    }
                }
                else {
                    final Quote quote = getQuote("XCOR", quotes);
                    eval(run + "-3", item, quote);
                }
            }
        }

        if (!item.isValid()) {
            if (item.getIsin().startsWith("DE") && instrument.getInstrumentType() == InstrumentTypeEnum.STK) {
                final Quote quote = GERMANY_STOCK.select(instrument, quotes);
                eval(run + "-4", item, quote);
            }
            if (item.getIsin().startsWith("DE") && instrument.getInstrumentType() == InstrumentTypeEnum.WNT || instrument.getInstrumentType() == InstrumentTypeEnum.CER) {
                final Quote quote = GERMANY_DERIVATIVE.select(instrument, quotes);
                eval(run + "-5", item, quote);
            }
        }

        if (!item.isValid()) {
            final List<Quote> quotesInCountry = new QuoteFilters.CountryFilter(item.getIsin().substring(0, 2)).apply(quotes);

            final Quote prioQuote = PRIO_MARKETS.select(instrument, quotesInCountry);
            eval(run + "-6", item, prioQuote);

            quotesInCountry.stream().filter(quote -> !item.isValid()).forEach(quote -> eval(run + "-7", item, quote));
        }

        if (!item.isValid()) {
            final Quote prioQuote = PRIO_MARKETS.select(instrument, quotes);
            eval(run + "-8", item, prioQuote);

            quotes.stream().filter(quote -> !item.isValid()).forEach(quote -> eval(run + "-9", item, quote));
        }
    }

    private List<Quote> getQuotes(Instrument instrument, String currency,
            boolean filterClassOneQuotes) {
        final List<Quote> quotes = getQuotes(instrument, currency);

        return filterClassOneQuotes ? QuoteFilters.CLASS_ONE_QUOTES.apply(quotes) : quotes;
    }

    private List<Quote> getQuotes(Instrument instrument, String currency) {
        try {
            final List<Quote> withPrices = QuoteFilters.WITH_PRICES.apply(instrument.getQuotes());
            return StringUtils.hasText(currency)
                    ? new QuoteFilters.CurrencyFilter(getCurrencies(currency)).apply(withPrices)
                    : withPrices;
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }

    private String[] getCurrencies(final String currency) {
        if ("GBP".equals(currency)) {
            return new String[]{"GBP", "GBX"};
        }
        if ("ZAR".equals(currency)) {
            return new String[]{"ZAR", "ZAX", "ZAC"};
        }
        return new String[]{currency};
    }

    private void eval(String iteration, Item item, Quote quote) {
        eval(iteration, item, quote, false);
    }

    private void eval(String iteration, Item item, Quote quote, boolean bidPricePrio) {
        if (quote == null) {
            return;
        }

        final IntradayData intradayData =
                this.controller.getIntradayProvider().getIntradayData(quote, null);

        final PriceRecord pr = intradayData.getPrice();
        if (pr == null) {
            return;
        }
        if (bidPricePrio) {
            final Price bid = pr.getBid();
            if (bid.getDate() != null && item.getDate().equals(bid.getDate().toLocalDate())) {
                item.setResult(iteration, quote, bid, "bid");
                return;
            }
        }

        final Price price = pr instanceof PriceRecordFund
                ? ((PriceRecordFund) pr).getRedemptionPrice()
                : pr.getPrice();
        if (price.getDate() != null && item.getDate().equals(price.getDate().toLocalDate())) {
            item.setResult(iteration, quote, price, "");
            return;
        }

        final Price close = pr instanceof PriceRecordFund
                ? ((PriceRecordFund) pr).getPreviousRedemptionPrice()
                : pr.getPreviousClose();
        if (close.getDate() != null && item.getDate().equals(close.getDate().toLocalDate())) {
            item.setResult(iteration, quote, close, "");
            return;
        }

        final Price bid = pr.getBid();
        if (bid.getDate() != null && item.getDate().equals(bid.getDate().toLocalDate())) {
            item.setResult(iteration, quote, bid, "bid");
            return;
        }

        if (quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.BND) {
            final Interval interval = item.getDate().toInterval();
            final IntradayData bidTimeseries =
                    this.controller.getIntradayProvider().getIntradayData(quote, interval);

            if (bidTimeseries.getTicks() != null) {
                final AggregatedTickRecord agg = bidTimeseries.getTicks().aggregate(DURATION_DAY, TickType.BID);
                final Iterator<DataWithInterval<AggregatedTick>> iterator = agg.getTimeseries(interval).iterator();
                if (iterator.hasNext()) {
                    final DataWithInterval<AggregatedTick> dwi = iterator.next();
                    final PriceImpl hbid = new PriceImpl(PriceCoder.decode(dwi.getData().getClose()),
                            null, null, item.getDate().toDateTimeAtStartOfDay(), PriceQuality.END_OF_DAY);
                    item.setResult(iteration, quote, hbid, "hist-bid");
                    return;
                }
            }
        }

        final Price hprice = requestHistoricPrice(quote, item.getDate());
        if (hprice != null && hprice.getValue() != null && hprice.getDate() != null) {
            item.setResult(iteration, quote, hprice, "eod");
        }
    }

    private Price requestHistoricPrice(Quote quote, LocalDate date) {
        final List<SymbolQuote> sqs = Collections.singletonList(SymbolQuote.create(quote));
        final List<LocalDate> dates = Collections.singletonList(date);
        final List<List<Price>> historicPrices = this.controller.getHistoricRatiosProvider().getHistoricPrices(sqs, dates);
        if (historicPrices == null || historicPrices.isEmpty()) {
            this.logger.warn("<requestHistoricPrice> failed to get price for " + quote
                    + " on " + date + ", task = " + this.dir.getName());
            return null;
        }
        return historicPrices.get(0).get(0);
    }

    private Quote getQuoteByMarket(String market, List<Quote> quotes) {
        for (final Quote quote : quotes) {
            if (market != null && market.equals(quote.getMarket().getSymbolIso())) {
                return quote;
            }
        }
        return null;
    }

    private Quote getQuote(String market, List<Quote> quotes) {
        for (final Quote quote : quotes) {
            if (market != null && market.equals(quote.getSymbolVwdfeedMarket())) {
                return quote;
            }
        }
        return null;
    }

    private void print(Item item) {
        this.resultWriter.print(item.getLine());
        if (item.isValid()) {
            printField(item.getQuote().getSymbolVwdcode());
            printField(item.getQuote().getMarket().getSymbolIso());
            printField(item.getQuote().getCurrency().getSymbolIso());

            final Price price = item.getPrice();
            printDecimal(price.getValue());
            printField(price.getDate().toLocalDate());
            printField(price.getVolume());
            printField(price.getSupplement());
            printField(item.getIteration() + (StringUtils.hasText(item.getPriceType()) ? "/" + item.getPriceType() : ""));
        }
        this.resultWriter.print(EOL);
    }

    private void printDecimal(BigDecimal value) {
        if (value == null) {
            printField(null);
        }

        printField(value.toString().replace('.', ','));
    }

    private void printField(Object value) {
        this.resultWriter.print(";" + (value != null ? value : ""));
    }

    private String extract(String token) {
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }

        throw new IllegalArgumentException("failed for '" + token + "'");
    }

    private void incPctComplete() {
        updatePctComplete(1);
    }

    private void updatePctComplete(final int num) {
        this.numProcessed += num;
        this.pctComplete.set(Math.min(99, Math.max(1, (this.numProcessed * 100) / this.lines.size())));
    }

    private void logProblem(String line, Exception e) {
        PrintWriter pw = null;
        try {
            final File f = new File(this.dir, PROBLEMS_FILE_NAME);
            if (!f.exists()) {
                this.logger.error("<logProblem> new problem file " + f.getAbsolutePath());
            }
            pw = new PrintWriter(new FileWriter(f, true));
            pw.println("append failed for " + line + ":");
            e.printStackTrace(pw);
        } catch (IOException e1) {
            this.logger.error("<logProblem> failed ", e1);
            this.logger.error("<logProblem> ... when trying to log ", e);
        } finally {
            IoUtils.close(pw);
        }
    }

    private static class Item {
        private final String line;

        private final LocalDate date;

        private final String currency;

        private final String isin;

        private final String market;

        private Price price;

        private Quote quote;

        private String iteration;

        private String priceType;

        private Item(String line, LocalDate date, String currency, String isin, String market) {
            this.line = line;
            this.date = date;
            this.currency = currency;
            this.isin = isin;
            this.market = market;
        }

        public String getLine() {
            return line;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCurrency() {
            return currency;
        }

        public String getIsin() {
            return isin;
        }

        public String getMarket() {
            return market;
        }

        public void setResult(String iteration, Quote quote, Price price, String priceType) {
            this.iteration = iteration;
            this.price = price;
            this.quote = quote;
            this.priceType = priceType;
        }

        public String getPriceType() {
            return priceType;
        }

        public String getIteration() {
            return iteration;
        }

        public Price getPrice() {
            return price;
        }

        public Quote getQuote() {
            return quote;
        }

        public boolean isValid() {
            return this.price != null;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "date=" + date +
                    ", currency='" + currency + '\'' +
                    ", isin='" + isin + '\'' +
                    ", market='" + market + '\'' +
                    '}';
        }

    }

}
