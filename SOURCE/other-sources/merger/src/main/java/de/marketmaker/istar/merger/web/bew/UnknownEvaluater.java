/*
 * UnknownEvaluater.java
 *
 * Created on 06.07.2010 14:04:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import de.marketmaker.istar.common.util.FileUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnknownEvaluater {
    private File baseDir;

    private static final Comparator<UnknownSymbol> MARKET_COMPARATOR = new Comparator<UnknownSymbol>() {
        public int compare(UnknownSymbol o1, UnknownSymbol o2) {
            final String m1 = o1.getMarket();
            final String m2 = o2.getMarket();
            if (m1 == null && m2 == null) {
                return o1.getSymbol().compareTo(o2.getSymbol());
            }
            if (m1 == null) {
                return -1;
            }
            if (m2 == null) {
                return 1;
            }
            return m1.compareTo(m2);
        }
    };

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void evaluate() throws Exception {
        final List<File> files = FileUtil.listAllFiles(this.baseDir, new FileFilter() {
            public boolean accept(File f) {
                return "unknown.txt".equals(f.getName());
            }
        });


        final List<UnknownSymbol> all = new ArrayList<>();
        for (final File file : files) {
            final Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();

                final UnknownSymbol us = new UnknownSymbol(line);
                all.add(us);
            }
            scanner.close();
        }

        final Set<UnknownSymbol> allUnique = new HashSet<>(all);
        System.out.println("#all: " + all.size());
        System.out.println("#all (unique): " + allUnique.size());

        final List<UnknownSymbol> eurex = new ArrayList<>();
        final List<UnknownSymbol> cbo = new ArrayList<>();
        final List<UnknownSymbol> otherOptions= new ArrayList<>();
        final List<UnknownSymbol> unknownIsin = new ArrayList<>();
        final List<UnknownSymbol> unknownValor = new ArrayList<>();
        final List<UnknownSymbol> noStrategy = new ArrayList<>();
        final List<UnknownSymbol> knownIsinsWithoutMarket = new ArrayList<>();
        final List<UnknownSymbol> knownValorWithoutMarket = new ArrayList<>();
        final List<UnknownSymbol> fx = new ArrayList<>();
        final List<UnknownSymbol> others = new ArrayList<>();

        for (final UnknownSymbol us : allUnique) {
//            if ("EUX".equals(us.getMarket()) && "EUREXTICKER".equals(us.getStrategy()) && "null".equals(us.getResult())) {
            if ("CBO".equals(us.getMarket())) {
                cbo.add(us);
            }
            else if (("EUX".equals(us.getMarket()) || us.getMarket()==null) && "EUREXTICKER".equals(us.getStrategy()) && "null".equals(us.getResult())) {
                eurex.add(us);
            }
            else if ("EUREXTICKER".equals(us.getStrategy()) && "null".equals(us.getResult())) {
                otherOptions.add(us);
            }
            else if ("ISIN".equals(us.getStrategy()) && "null".equals(us.getResult())) {
                unknownIsin.add(us);
            }
            else if ("VALOR".equals(us.getStrategy()) && "null".equals(us.getResult())) {
                unknownValor.add(us);
            }
            else if ("ISIN".equals(us.getStrategy()) && !"null".equals(us.getResult())) {
                knownIsinsWithoutMarket.add(us);
            }
            else if ("VALOR".equals(us.getStrategy()) && !"null".equals(us.getResult())) {
                knownValorWithoutMarket.add(us);
            }
            else if (("FX".equals(us.getMarket()) || isInt(us.getMarket())) && us.getSymbol().indexOf("/") > 0) {
                fx.add(us);
            }
            else if ("IID".equals(us.getStrategy()) && "0".equals(us.getInternalRequestSymbol()) && "null".equals(us.getResult())) {
                noStrategy.add(us);
            }
            else {
                others.add(us);
            }
        }

        System.out.println("#eurex: " + eurex.size());
        System.out.println("#cbo: " + cbo.size());
        System.out.println("#otherOptions: " + otherOptions.size());
        System.out.println("#unknownValor: " + unknownValor.size());
        System.out.println("#unknownIsin: " + unknownIsin.size());
        System.out.println("#knownIsinsWithoutMarket: " + knownIsinsWithoutMarket.size());
        System.out.println("#knownValorWithoutMarket: " + knownValorWithoutMarket.size());
        System.out.println("#fx: " + fx.size());
        System.out.println("#noStrategy: " + noStrategy.size());

        System.out.println("#others: " + others.size());

        print(others);
    }

    private void print(List<UnknownSymbol> list) {
        Collections.sort(list,MARKET_COMPARATOR);
        for (final UnknownSymbol us : list) {
            System.out.println(us.getLine());
        }
    }

    private boolean isInt(String market) {
        try {
            Integer.parseInt(market);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        final UnknownEvaluater ue = new UnknownEvaluater();
        ue.setBaseDir(new File("d:/temp/bew"));
        ue.evaluate();
    }

    private static class UnknownSymbol {
        private final String line;

        private final String symbol;

        private final String internalRequestSymbol;

        private final String market;

        private final String strategy;

        private final String result;

        private UnknownSymbol(String line) {
            this.line = line;

            final String[] tokens = line.split(Pattern.quote(" -- "));
            final String request = tokens[0];
            final String[] requestTokens = request.split(Pattern.quote(";"));
            this.symbol = requestTokens[0];
            this.market = requestTokens.length == 1 ? null : requestTokens[1];

            final String[] response = tokens[1].split(Pattern.quote(" => "));
            final String[] internalRequest = response[0].split(Pattern.quote("/"));
            this.internalRequestSymbol = internalRequest[0];
            this.strategy = internalRequest[1];

            this.result = response[1];
        }

        public String getLine() {
            return line;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getInternalRequestSymbol() {
            return internalRequestSymbol;
        }

        public String getMarket() {
            return market;
        }

        public String getStrategy() {
            return strategy;
        }

        public String getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnknownSymbol that = (UnknownSymbol) o;

            //noinspection RedundantIfStatement
            if (!line.equals(that.line)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return line.hashCode();
        }
    }
}
