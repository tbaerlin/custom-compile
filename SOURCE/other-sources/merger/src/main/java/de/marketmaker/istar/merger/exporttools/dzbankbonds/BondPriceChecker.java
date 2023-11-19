/*
 * BndVKRDetails.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.exporttools.dzbankbonds;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BondPriceChecker {
    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private List<String> symbols;

    private int countSuccess;

    private int countFailure;

    public BondPriceChecker() {
        this.symbols = new ArrayList<>();
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    public void process() throws Exception {
        RequestContextHolder.setRequestContext(new RequestContext(ProfileFactory.valueOf(true), MarketStrategy.STANDARD));

        FileWriter success = new FileWriter("dzbank-bond-check-list1.txt");
        FileWriter failure = new FileWriter("dzbank-bond-check-list2.txt");

        String headline = "market;last;last-volume;last-date;bid;ask;bid-ask-date";
        success.write(headline + "\n");
        failure.write(headline + "\n");

        this.countSuccess = 0;
        this.countFailure = 0;

        int chunkSize = 50;
        int chunks = this.symbols.size() / chunkSize + 1;
        for (int i = 0; i < chunks; ++i) {
            process(success, failure, this.symbols.subList(i * chunkSize, Math.min((i + 1) * chunkSize, this.symbols.size())));
        }

        System.out.println(this.countFailure + this.countSuccess + "/" + this.symbols.size());

        success.close();
        failure.close();
    }

    private void process(FileWriter success, FileWriter failure,
            List<String> symbols) throws Exception {
        Map map = this.instrumentProvider.identifyInstrument(symbols, SymbolStrategyEnum.ISIN);

        for (String symbol : symbols) {
            if ((this.countFailure + this.countSuccess) % 100 == 0) {
                System.out.println(this.countFailure + this.countSuccess + "/" + this.symbols.size());
            }

            Instrument instrument = (Instrument) map.get(symbol);
            List<Quote> quotes = (instrument == null) ? Collections.<Quote>emptyList() : instrument.getQuotes();

            List<PriceRecord> prs = (instrument == null) ? Collections.<PriceRecord>emptyList() : this.intradayProvider.getPriceRecords(quotes);

            Item item = new Item(instrument, prs);

            Check check = Check_1.INSTANCE;
            while (check != null) {
                check.reset(item);

                boolean valid = check.isValid();
                Check next = check.getSuccessor();

                if ((!valid) && (next == null)) {
                    this.countFailure += 1;

                    failure.write(this.countFailure + ") " + symbol + " failed: " + check.getDescription() + "\n");
                    print(failure, quotes, prs);
                    failure.write("\n");
                }

                if ((valid) && (next == null)) {
                    this.countSuccess += 1;

                    success.write(this.countSuccess + ") " + symbol + ": Bewertungskurs=" + check.getValuationRate() + "\n");
                    print(success, quotes, prs);
                    success.write("\n");
                }

                check = next;
            }
        }
    }

    private void print(FileWriter failure, List<Quote> quotes,
            List<PriceRecord> prs) throws IOException {
        for (int i = 0; i < quotes.size(); ++i) {
            Quote quote = quotes.get(i);
            PriceRecord pr = prs.get(i);
            failure.write(quote.getSymbolVwdfeedMarket() + ";" + printPrice(pr.getPrice()) + ";" + printVolume(pr.getPrice()) + ";" + printDate(pr.getPrice()) + ";" + printPrice(pr.getBid(), pr.getPreviousBid()) + ";" + printPrice(pr.getAsk(), pr.getPreviousAsk()) + ";" + printDate(pr.getAsk(), pr.getPreviousAsk()) + "\n");
        }
    }

    private String printDate(Price price, Price previousPrice) {
        String s = printDate(price);
        return (!"".equals(s)) ? s : printDate(previousPrice);
    }

    private String printPrice(Price price, Price previousPrice) {
        String s = printPrice(price);
        return (!"".equals(s)) ? s : printPrice(previousPrice);
    }

    private String printDate(Price price) {
        if ((price == null) || (price.getDate() == null)) {
            return "";
        }
        return price.getDate().toString("dd.MM.yyyy HH:mm:ss");
    }

    private String printVolume(Price price) {
        if ((price == null) || (price.getVolume() == null)) {
            return "";
        }
        return Long.toString(price.getVolume());
    }

    private String printPrice(Price price) {
        if ((price == null) || (price.getValue() == null)) {
            return "";
        }
        return price.getValue().toPlainString();
    }

    public static void main(String[] args) throws Exception {
        FileSystemXmlApplicationContext context = (args.length > 0) ? new FileSystemXmlApplicationContext(args[0]) : new FileSystemXmlApplicationContext("classpath:de/marketmaker/istar/merger/exporttools/dzbankbonds/applicationContext.xml");

        BondPriceChecker checker = (BondPriceChecker) context.getBean("checker");
        checker.process();
        context.destroy();
    }

    public static class Check_7 extends BondPriceChecker.Check {
        static final BondPriceChecker.Check INSTANCE = new Check_7();

        protected BondPriceChecker.Check getValidSuccessor() {
            return null;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return null;
        }

        public boolean calcValid() {
            double sumBid = 0.0D;
            double sumAsk = 0.0D;
            for (PriceRecord pr : this.item.getPriceRecords()) {
                Price bid = getBid(pr);
                Price ask = getAsk(pr);
                if ((isValidPrice(bid)) && (isValidPrice(ask))) {
                    sumBid += bid.getValue().doubleValue();
                    sumAsk += ask.getValue().doubleValue();
                }
            }
            return sumBid <= sumAsk && sumBid > 0;
        }

        private Price getBid(PriceRecord pr) {
            Price bid = pr.getBid();
            if (isValidPrice(bid)) {
                return bid;
            }
            return pr.getPreviousBid();
        }

        private Price getAsk(PriceRecord pr) {
            Price ask = pr.getAsk();
            if (isValidPrice(ask)) {
                return ask;
            }
            return pr.getPreviousAsk();
        }

        public BigDecimal getValuationRate() {
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;

            for (PriceRecord pr : this.item.getPriceRecords()) {
                Price bid = getBid(pr);
                Price ask = getAsk(pr);
                if ((isValidPrice(bid)) && (isValidPrice(ask))) {
                    sum = sum.add(bid.getValue(), Constants.MC).add(ask.getValue(), Constants.MC);
                    count += 2;
                }
            }
            try {
                return sum.divide(new BigDecimal(count), Constants.MC);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(item.getInstrument().getSymbolIsin());
                throw new RuntimeException(e);
            }
        }

        public String getDescription() {
            return (calcValid()) ? null : "7 Geld > Brief";
        }
    }

    public static class Check_6 extends BondPriceChecker.Check_5 {
        static final BondPriceChecker.Check INSTANCE = new Check_6();

        protected BondPriceChecker.Check getValidSuccessor() {
            return BondPriceChecker.Check_7.INSTANCE;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return null;
        }

        protected Price getPrice(PriceRecord pr) {
            Price bid = pr.getBid();
            if (isValidPrice(bid)) {
                return bid;
            }
            return pr.getPreviousBid();
        }

        public String getDescription() {
            return (calcValid()) ? null : "6 – Abweichung Geld >3%";
        }
    }

    public static class Check_5 extends BondPriceChecker.Check {
        static final BondPriceChecker.Check INSTANCE = new Check_5();

        protected BondPriceChecker.Check getValidSuccessor() {
            return null;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return null;
        }

        public boolean calcValid() {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (PriceRecord pr : this.item.getPriceRecords()) {
                Price price = getPrice(pr);
                if (isValidPrice(price)) {
                    max = Math.max(max, price.getValue().doubleValue());
                    min = Math.min(min, price.getValue().doubleValue());
                }
            }
            return (max - min) / min <= 0.03D;
        }

        protected Price getPrice(PriceRecord pr) {
            return pr.getPrice();
        }

        public BigDecimal getValuationRate() {
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;

            for (PriceRecord pr : this.item.getPriceRecords()) {
                Price price = getPrice(pr);
                if (isValidPrice(price)) {
                    sum = sum.add(price.getValue(), Constants.MC);
                    ++count;
                }
            }
            return sum.divide(new BigDecimal(count), Constants.MC);
        }

        public String getDescription() {
            return (calcValid()) ? null : "5 – Abweichung Bezahlt >3%";
        }
    }

    public static class Check_4 extends BondPriceChecker.Check {
        static final BondPriceChecker.Check INSTANCE = new Check_4();

        protected BondPriceChecker.Check getValidSuccessor() {
            return BondPriceChecker.Check_6.INSTANCE;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return null;
        }

        public boolean calcValid() {
            int count = 0;
            for (PriceRecord pr : this.item.getPriceRecords()) {
                if ((isValidPrice(pr.getBid())) || (isValidPrice(pr.getPreviousBid()))) {
                    ++count;
                }
            }
            return count >= 2;
        }

        public String getDescription() {
            return (calcValid()) ? null : "4 – keine 2. Geld-Quelle verfügbar ";
        }
    }

    public static class Check_3 extends BondPriceChecker.Check {
        static final BondPriceChecker.Check INSTANCE = new Check_3();

        protected BondPriceChecker.Check getValidSuccessor() {
            return BondPriceChecker.Check_4.INSTANCE;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return null;
        }

        public boolean calcValid() {
            for (PriceRecord pr : this.item.getPriceRecords()) {
                if ((isValidPrice(pr.getBid())) || (isValidPrice(pr.getPreviousBid()))) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return (calcValid()) ? null : "3 - keine Geld-Quelle verfügbar";
        }
    }

    public static class Check_2 extends BondPriceChecker.Check {
        static final BondPriceChecker.Check INSTANCE = new Check_2();

        protected BondPriceChecker.Check getValidSuccessor() {
            return BondPriceChecker.Check_5.INSTANCE;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return BondPriceChecker.Check_3.INSTANCE;
        }

        public boolean calcValid() {
            int count = 0;
            final DateTime dateBounds = new LocalDate().minusWeeks(2).toDateTimeAtStartOfDay();
            for (PriceRecord pr : this.item.getPriceRecords()) {
                Price price = pr.getPrice();
                if ((isValidPrice(price)) && (price.getDate() != null) && (price.getDate().isAfter(dateBounds))) {
                    ++count;
                }
            }
            return count >= 2;
        }

        public String getDescription() {
            throw new IllegalStateException("no leaf check");
        }
    }

    public static class Check_1 extends BondPriceChecker.Check {
        static final BondPriceChecker.Check INSTANCE = new Check_1();

        protected BondPriceChecker.Check getValidSuccessor() {
            return BondPriceChecker.Check_2.INSTANCE;
        }

        protected BondPriceChecker.Check getInvalidSuccessor() {
            return null;
        }

        public boolean calcValid() {
            return (this.item.getInstrument() != null) && (!this.item.getInstrument().getQuotes().isEmpty());
        }

        public String getDescription() {
            return (calcValid()) ? null : "1 – ISIN nicht verfügbar";
        }
    }

    public static abstract class Check {
        protected BondPriceChecker.Item item;

        private boolean isValid;

        public void reset(BondPriceChecker.Item item) {
            this.item = item;
            this.isValid = calcValid();
            if (this.isValid)
                return;
            calcValid();
        }

        public boolean isValid() {
            return this.isValid;
        }

        public Check getSuccessor() {
            return (calcValid()) ? getValidSuccessor() : getInvalidSuccessor();
        }

        protected boolean isValidPrice(Price price) {
            return (price != null) && (price.getValue() != null) && (price.getValue().compareTo(BigDecimal.ZERO) > 0);
        }

        protected abstract Check getValidSuccessor();

        protected abstract Check getInvalidSuccessor();

        protected abstract boolean calcValid();

        public BigDecimal getValuationRate() {
            return null;
        }

        public abstract String getDescription();
    }

    public static class Item {
        private final Instrument instrument;

        private final List<PriceRecord> priceRecords;

        public Item(Instrument instrument, List<PriceRecord> priceRecords) {
            this.instrument = instrument;
            this.priceRecords = priceRecords;
        }

        public Instrument getInstrument() {
            return this.instrument;
        }

        public List<PriceRecord> getPriceRecords() {
            return this.priceRecords;
        }
    }
}