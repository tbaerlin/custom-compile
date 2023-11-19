/*
 * SymbolUtil.java
 *
 * Created on 08.02.13 11:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.util;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.regex.Pattern;

/**
 * @author zzhao
 */
public final class SymbolUtil {

    public static final String IID_SUFFIX = ".iid";

    public static final String QID_SUFFIX = ".qid";

    private static final String PREFIX_UNDERLYING = "underlying(";

    private static final Pattern BIS_KEY_PATTERN = Pattern.compile("[0-9]+(_[0-9]+){3}");

    private SymbolUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static Instrument toInstrument(InstrumentProvider instrumentProvider, String symbol) {
        return bySymbol(symbol, guessStrategy(symbol), instrumentProvider);
    }

    private static boolean isSpecific(SymbolStrategyEnum symbolStrategy) {
        return symbolStrategy != null && symbolStrategy != SymbolStrategyEnum.AUTO;
    }

    private static Instrument bySymbol(String symbol, SymbolStrategyEnum symbolStrategy,
            InstrumentProvider instrumentProvider) {
        if (!isSpecific(symbolStrategy)) {
            return identifyInstrument(symbol, guessStrategy(symbol), instrumentProvider);
        }
        return identifyInstrument(symbol, symbolStrategy, instrumentProvider);
    }

    public static Instrument identifyInstrument(String symbol, SymbolStrategyEnum symbolStrategy,
            InstrumentProvider instrumentProvider) {
        if (symbolStrategy == SymbolStrategyEnum.ISIN) {
            return instrumentProvider.identifyByIsin(symbol);
        }
        if (symbolStrategy == SymbolStrategyEnum.WKN) {
            return instrumentProvider.identifyByWkn(symbol);
        }
        if (symbolStrategy == SymbolStrategyEnum.VWDCODE) {
            return instrumentProvider.identifyByVwdcode(symbol).getInstrument();
        }
        if (symbolStrategy == SymbolStrategyEnum.MMWKN) {
            return instrumentProvider.identifyByMmwkn(symbol).getInstrument();
        }
        if (symbolStrategy == SymbolStrategyEnum.IID || symbol.endsWith(IID_SUFFIX)) {
            final Long iid = getId(symbol, IID_SUFFIX);
            return instrumentProvider.identifyByIid(iid);
        }
        if (symbolStrategy == SymbolStrategyEnum.QID || symbol.endsWith(QID_SUFFIX)) {
            final Long qid = getId(symbol, QID_SUFFIX);
            return instrumentProvider.identifyByQid(qid);
        }
        if (symbolStrategy == SymbolStrategyEnum.BIS_KEY || BIS_KEY_PATTERN.matcher(symbol).matches()) {
            return instrumentProvider.identifyByBisKey(symbol).getInstrument();
        }
        if (symbolStrategy == SymbolStrategyEnum.INFRONT_ID) {
            return instrumentProvider.identifyByInfrontId(symbol).getInstrument();
        }

        throw new UnknownSymbolException("invalid symbol: '" + symbol + "'");
    }

    public static SymbolStrategyEnum guessStrategy(String symbol) {
        return doGuessStrategy(extractSymbol(symbol));
    }

    public static String extractSymbol(String symbol) {
        if (!usesUnderlyingFunction(symbol)) {
            return symbol;
        }

        return symbol.substring(PREFIX_UNDERLYING.length(), symbol.length() - 1);
    }

    public static boolean usesUnderlyingFunction(String symbol) {
        return symbol.startsWith(PREFIX_UNDERLYING);
    }

    public static SymbolStrategyEnum doGuessStrategy(String symbol) {
        if (symbol.endsWith(QID_SUFFIX)) {
            return SymbolStrategyEnum.QID;
        }
        if (symbol.endsWith(IID_SUFFIX)) {
            return SymbolStrategyEnum.IID;
        }
        if (IsinUtil.isIsin(symbol)) {
            return SymbolStrategyEnum.ISIN;
        }
        if (symbol.indexOf('.') > 0) {
            if (symbol.endsWith("*")) {
                return SymbolStrategyEnum.VWDCODE_PREFIX;
            }
            return SymbolStrategyEnum.VWDCODE;
        }
        if (BIS_KEY_PATTERN.matcher(symbol).matches()) {
            return SymbolStrategyEnum.BIS_KEY;
        }
        if (symbol.indexOf(';') > 1) {
            return SymbolStrategyEnum.INFRONT_ID;
        }
        return SymbolStrategyEnum.WKN;
    }

    private static long getId(String symbol, String suffix) {
        try {
            if (symbol == null) {
                throw new IllegalArgumentException("symbol required");
            }
            return symbol.toLowerCase().endsWith(suffix)
                ? Long.parseLong(symbol.substring(0, symbol.length() - suffix.length()))
                : Long.parseLong(symbol);
        } catch (NumberFormatException e) {
            throw new UnknownSymbolException("invalid symbol: '" + symbol + "'");
        }
    }
}
