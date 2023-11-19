/*
 * UserDataReader.java
 *
 * Created on 22.11.2006 14:24:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_pb;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * d:\temp\pb-userimport localhost issue-db teli1.market-maker.de:61616
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserDataReader {
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yy");
    final StringBuilder sb = new StringBuilder();

    private File baseDir;

    private int numEmpty;
    private int numFilled;
    private int numDeletedPositions;

    private InstrumentServer instrumentServer;
    private UserInserter inserter;

    static final Map<String, List<String>> MARKETS = new HashMap<>();
    private static final int STEP_SIZE = 1;

    static {
        MARKETS.put("AMEX", Arrays.asList("A"));
        MARKETS.put("Berlin-Bremen", Arrays.asList("BLN"));
        MARKETS.put("D\ufffdsseldorf", Arrays.asList("DDF"));
        MARKETS.put("Deutsche Fonds", Arrays.asList("FONDS"));
        MARKETS.put("Frankfurt", Arrays.asList("FFM", "FFMST", "FFMFO"));
        MARKETS.put("Hamburg", Arrays.asList("HBG"));
        MARKETS.put("Hannover", Arrays.asList("HNV"));
        MARKETS.put("M\ufffdnchen", Arrays.asList("MCH"));
        MARKETS.put("NASDAQ", Arrays.asList("Q", "IQ"));
        MARKETS.put("Paris", Arrays.asList("FR"));
        MARKETS.put("Stuttgart", Arrays.asList("EUWAX", "STG"));
        MARKETS.put("XETRA", Arrays.asList("ETR", "EEU", "EUS"));
        MARKETS.put("Amsterdam", Arrays.asList("NL"));
        MARKETS.put("DJ STOXX", Arrays.asList("STX"));
        MARKETS.put("EUREX", Arrays.asList("DTB"));
        MARKETS.put("London AIM", Arrays.asList("UK", "UKINT"));
        MARKETS.put("London Dom Quotes", Arrays.asList("UK", "UKINT"));
        MARKETS.put("NYSE", Arrays.asList("N", "DJ"));
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    public void setInserter(UserInserter inserter) {
        this.inserter = inserter;
    }

    public void start() throws Exception {
        final long start = System.currentTimeMillis();
        final File[] files = this.baseDir.listFiles();

        final Set<String> usernames = new HashSet<>();
        for (final File file : files) {
            final String username = file.getName().split("#")[0];
            usernames.add(username);
        }
        System.out.println("#usernames: " + usernames.size());

        final Map<String, List<ImportPortfolio>> map = new HashMap<>();

        for (final File file : files) {
            final String[] tokens = file.getName().split("#");
            final String username = tokens[0];
//            final String secid = tokens[1];
            final String type = tokens[2];
            final int num = Integer.parseInt(tokens[3]);

            try {
                final boolean isPortfolio = "mde".equals(type);
                final ImportPortfolio portfolio = readPortfolio(username, num, file, isPortfolio);

                if (portfolio == null) {
                    continue;
                }

                List<ImportPortfolio> ips = map.get(username);
                if (ips == null) {
                    ips = new ArrayList<>();
                    map.put(username, ips);
                }
                ips.add(portfolio);
            }
            catch (Exception e) {
                System.out.println("failed for " + file.getName());
                e.printStackTrace();
            }
        }

        checkMarkets(map);
        final Map<String, Instrument> instruments = getInstruments(map);
        inserter.setInstrumentsByWkn(instruments);

        final List<String> names = new ArrayList<>(usernames);

        for (int i = 0; i < names.size(); i += STEP_SIZE) {
            System.out.println("inserted " + i + " users");
            final List<String> sublist = names.subList(i, Math.min(names.size(), i + STEP_SIZE));
            inserter.insertUsers(sublist, map);
        }

        System.out.println("took: " + (System.currentTimeMillis() - start) / 1000 + "sec");
        System.out.println("#empty: " + this.numEmpty);
        System.out.println("#filled: " + this.numFilled);
        System.out.println("#deleted positions: " + this.numDeletedPositions);
    }

    private void checkMarkets(Map<String, List<ImportPortfolio>> map) {
        boolean interrupt = false;
        for (final List<ImportPortfolio> ips : map.values()) {
            for (final ImportPortfolio ip : ips) {
                for (final ImportPosition importPosition : ip.getPositions()) {
                    if (!MARKETS.containsKey(importPosition.getMarket())) {
                        System.out.println("MARKETS.put(\"" + importPosition.getMarket() + "\", Arrays.asList(new String[]{\"\"}));");
                        interrupt = true;
                    }
                }
            }
        }
        if (interrupt) {
            throw new IllegalStateException("interrupt for unknown market");
        }
    }

    private Map<String, Instrument> getInstruments(Map<String, List<ImportPortfolio>> map) throws Exception {
        final Map<String, Instrument> instrumentsByWkn = new HashMap<>();

        final Map<String, Integer> wkns = new HashMap<>();
        for (final List<ImportPortfolio> ips : map.values()) {
            for (final ImportPortfolio ip : ips) {
                for (final ImportPosition importPosition : ip.getPositions()) {
                    final String wkn = importPosition.getWkn();
                    final Integer count = wkns.get(wkn);
                    if (count == null) {
                        wkns.put(wkn, 1);
                    }
                    else {
                        wkns.put(wkn, count + 1);
                    }
                }
            }
        }

        System.out.println("#wkns: " + wkns.size());

        int numWknsUniqueFound = 0;
        int numWknsUniqueUnknown = 0;
        int numWknsFound = 0;
        int numWknsUnknown = 0;

        for (final Map.Entry<String, Integer> entry : wkns.entrySet()) {
            final SearchRequestStringBased ir = new SearchRequestStringBased();
            ir.setMaxNumResults(1);
            ir.setSearchExpression("wkn: " + entry.getKey());
            final SearchResponse response = this.instrumentServer.search(ir);
            if (response.getInstruments().isEmpty()) {
                numWknsUniqueUnknown++;
                numWknsUnknown += wkns.get(entry.getKey());
                continue;
            }

            final Instrument instrument = response.getInstruments().get(0);
            numWknsUniqueFound++;
            numWknsFound += entry.getValue();

            instrumentsByWkn.put(entry.getKey(), instrument);
        }

        System.out.println("#wkns found/unique: " + numWknsUniqueFound);
        System.out.println("#wkns unknown/unique: " + numWknsUniqueUnknown);
        System.out.println("#wkns found: " + numWknsFound);
        System.out.println("#wkns unknown: " + numWknsUnknown);

        return instrumentsByWkn;
    }

    private ImportPortfolio readPortfolio(String username, int num, File file, boolean isPortfolio) throws Exception {
        final CsvData data = read(file);
        if (data == null) {
            return null;
        }

        if (!isPortfolio && data.getData()[0].length != 19) {
            System.out.println("error, wrong number of elements for " + file.getName());
        }
        if (isPortfolio && data.getData()[0].length != 24) {
            System.out.println("error, wrong number of elements for " + file.getName());
        }

        final ImportPortfolio portfolio = new ImportPortfolio(username, data.getListname(), num, isPortfolio);

        for (final String[] lines : data.getData()) {
            final BigDecimal volume;
            final String wkn;
            final String currency;
            final String market;
            final BigDecimal ordervalue;
            final YearMonthDay orderdate;
            final String notiz;

            if (isPortfolio) {
                if ("n/a".equals(lines[0])) {
                    continue;
                }
                volume = new BigDecimal(lines[0].replaceAll("\\.", "").replaceAll(",", "."));
                wkn = lines[2];
                currency = lines[4];
                market = lines[7];
                ordervalue = new BigDecimal(lines[14].replaceAll("\\.", "").replaceAll(",", "."));
                orderdate = DTF.parseDateTime(lines[15]).toYearMonthDay();
                notiz = lines[23];
            }
            else {
                wkn = lines[1];
                currency = lines[6];
                market = lines[2];
                notiz = lines[18];
                ordervalue = null;
                orderdate = null;
                volume = null;
            }

            if ("-".equals(wkn)) {
                this.numDeletedPositions++;
                continue;
            }

            final ImportPosition ip = new ImportPosition(wkn, currency, market, ordervalue, orderdate, volume, notiz);
            portfolio.add(ip);
        }
        portfolio.finish();

        return portfolio;
    }

    private CsvData read(File file) throws Exception {
        String listname = "Name";

        sb.setLength(0);

        final Scanner s = new Scanner(file, "UTF-8");
        boolean read = false;

        while (s.hasNextLine()) {
            final String line = s.nextLine();

            if (read && line.length() == 0) {
                read = false;
            }

            if (read) {
                sb.append(line).append(NEWLINE);
            }

            if (line.startsWith("\"CSV-Ausgabe von")) {
                final int index = line.indexOf("'");
                listname = line.substring(index + 1, line.indexOf("'", index + 1));
            }
            if (line.startsWith("\"Name\";") || line.startsWith("\"St\ufffdck/Nom.\";")) {
                read = true;
            }


        }
        s.close();

        if (sb.length() == 0) {
            this.numEmpty++;
            return null;
        }

        this.numFilled++;

        final String[][] strings = Arrays.stream(sb.toString().split(NEWLINE)).map(l -> l.split(";")).toArray(String[][]::new);
        return new CsvData(listname, strings);
    }

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[]{
                new ClassPathResource("user-import-context.xml", UserDataReader.class).getURL().toString()
        }, false);

        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        if (args.length > 0) {
            ppc.setLocation(new FileSystemResource(args[0]));
        }
        else {
            ppc.setLocation(new ClassPathResource("user-import.properties", UserDataReader.class));
        }

        ac.addBeanFactoryPostProcessor(ppc);
        ac.refresh();

        final UserDataReader reader = (UserDataReader) ac.getBean("userDataReader");
        reader.start();

        ac.close();
    }

    private static class CsvData {
        private final String listname;
        private final String[][] data;

        public CsvData(String listname, String[][] data) {
            this.listname = listname;
            this.data = data;
        }

        public String getListname() {
            return listname;
        }

        public String[][] getData() {
            return data;
        }
    }
}
