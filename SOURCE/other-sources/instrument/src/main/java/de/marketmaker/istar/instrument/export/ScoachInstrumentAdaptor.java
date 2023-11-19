/*
 * ScoachInstrumentAdaptor.java
 *
 * Created on 22.03.2010 15:07:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.instrument.CertificateDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;

import static java.util.stream.Collectors.toCollection;

/**
 * If a symbol is available at FFMST and FFM, this class ensures that
 * the FFMST symbol will be used.
 * @author oflege
 */
public class ScoachInstrumentAdaptor implements InstrumentAdaptor, InitializingBean,
        DisposableBean {
    public static final String BIS_KEY_PREFIX_FFM = "2_";

    public static final String BIS_KEY_PREFIX_FFMST = "110_";

    static final String FFMST = ".FFMST";

    static final String FFM = ".FFM";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File symbolsFile;

    private File excludeFile;

    private Set<String> symbols;

    private int numFfmStockAdaptations;

    private int numFfmstAdaptations;

    public void setSymbolsFile(File symbolsFile) {
        this.symbolsFile = symbolsFile;
    }

    public void setExcludeFile(File excludeFile) {
        this.excludeFile = excludeFile;
    }

    public void afterPropertiesSet() throws Exception {
        this.symbols = readSymbols(this.symbolsFile, 1 << 22);
        if (this.excludeFile != null && this.excludeFile.canRead()) {
            final Set<String> excluded = readSymbols(this.excludeFile, 16);
            this.symbols.removeAll(excluded);
            this.logger.info("<afterPropertiesSet> excluded " + excluded);
        }
        this.logger.info("<afterPropertiesSet> read " + this.symbols.size() + " symbols");
    }

    @Override
    public void destroy() throws Exception {
        this.logger.info("<destroy> numFfmStockAdaptations=" + this.numFfmStockAdaptations
            + ", numFfmstAdaptations=" + this.numFfmstAdaptations);
    }

    private Set<String> readSymbols(final File f, int capacity) throws Exception {
        return Files.lines(f.toPath())
                .map(line -> getCoreSymbol(line, FFMST))
                .filter(s -> s != null)
                .collect(toCollection(() -> new HashSet<>(capacity)));
    }

    private String getCoreSymbol(String s, String suffix) {
        if (s != null && s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }
        return null;
    }

    public void adapt(InstrumentDp2 instrument) {
        final boolean stock = instrument.getInstrumentType().equals(InstrumentTypeEnum.STK);

        for (QuoteDp2 quote : instrument.getQuotesDp2()) {
            final String symbol = quote.getSymbolVwdfeed();
            if (symbol == null) {
                continue;
            }

            if (stock) {
                if (symbol.endsWith(FFMST)) {
                    // stock quotes always at FFM
                    quote.setSymbol(KeysystemEnum.VWDFEED, symbol.substring(0, symbol.length() - 2));
                    ensureBisKeyPrefix(quote, BIS_KEY_PREFIX_FFM);
                    this.numFfmStockAdaptations++;
                }
            }
            else {
                if (symbol.endsWith(FFM) && scoachSymbolExists(quote.getSymbolVwdcode())) {
                    // non-stock quotes with FFMST symbol in feed always use FFMST
                    quote.setSymbol(KeysystemEnum.VWDFEED, symbol + "ST");
                    ensureBisKeyPrefix(quote, BIS_KEY_PREFIX_FFMST);
                    this.numFfmstAdaptations++;
                }
            }
        }
    }

    private void ensureBisKeyPrefix(QuoteDp2 quote, String prefix) {
        final String s = quote.getSymbolBisKey();
        if (s != null && !s.startsWith(prefix)) {
            quote.setSymbol(KeysystemEnum.BIS_KEY, replaceBisKeyMarketPrefix(s, prefix));
        }
    }

    public static String replaceBisKeyMarketPrefix(String bisKey, String prefix) {
        assert prefix.endsWith("_");
        return (bisKey == null || bisKey.startsWith(prefix))
                ? bisKey
                : (prefix + bisKey.substring(bisKey.indexOf("_") + 1));
    }

    public boolean scoachSymbolExists(String symbolVwdcode) {
        return this.symbols.contains(getCoreSymbol(symbolVwdcode, FFM));
    }

    public static void main(String[] args) throws Exception {
        final ScoachInstrumentAdaptor adaptor = new ScoachInstrumentAdaptor();
        adaptor.setSymbolsFile(new File("d:/temp/ffmst.txt"));
        adaptor.setExcludeFile(new File("d:/temp/exclude.txt"));
        adaptor.afterPropertiesSet();
        final InstrumentDp2 i = new CertificateDp2(1);
        final QuoteDp2 q1 = new QuoteDp2(2);
        q1.setSymbol(KeysystemEnum.VWDFEED, "8.784613.EUWAX");
        i.addQuote(q1);
        final QuoteDp2 q2 = new QuoteDp2(3);
        q2.setSymbol(KeysystemEnum.VWDFEED, "8.784613.FFM");
        i.addQuote(q2);
        // DO change FFM to FFMST here
        adaptor.adapt(i);
        System.out.println(q1.getSymbolVwdfeed());
        System.out.println(q2.getSymbolVwdfeed());

        final InstrumentDp2 stk = new StockDp2(11);
        final QuoteDp2 sq1 = new QuoteDp2(12);
        sq1.setSymbol(KeysystemEnum.VWDFEED, "1.866671.FFM");
        stk.addQuote(sq1);
        final QuoteDp2 sq2 = new QuoteDp2(13);
        sq2.setSymbol(KeysystemEnum.VWDFEED, "1.866671.MCH");
        stk.addQuote(sq2);
        // DO NOT change since this is an STK
        adaptor.adapt(stk);
        System.out.println(sq1.getSymbolVwdfeed());
        System.out.println(sq2.getSymbolVwdfeed());

        final InstrumentDp2 stk1 = new StockDp2(111);
        final QuoteDp2 ssq1 = new QuoteDp2(112);
        ssq1.setSymbol(KeysystemEnum.VWDFEED, null);
        stk1.addQuote(ssq1);
        final QuoteDp2 ssq2 = new QuoteDp2(113);
        ssq2.setSymbol(KeysystemEnum.VWDFEED, "1.866671.MCH");
        stk1.addQuote(ssq2);
        // DO  change since this is a WRONG symbol for an STK
        adaptor.adapt(stk1);
        System.out.println(ssq1.getSymbolVwdfeed());
        System.out.println(ssq2.getSymbolVwdfeed());


    }
}
