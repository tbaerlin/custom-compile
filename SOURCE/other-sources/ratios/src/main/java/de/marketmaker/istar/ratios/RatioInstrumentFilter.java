/*
 * RatioInstrumentFilter.java
 *
 * Created on 16.11.11 09:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * Utility to figure out for which instruments ratios should be computed.
 * @author oflege
 */
public class RatioInstrumentFilter {
    private static final EnumSet<InstrumentTypeEnum> TYPES_WITHOUT_REQUIRED_ISIN = EnumSet.of(
            InstrumentTypeEnum.CUR,
            InstrumentTypeEnum.FND,
            InstrumentTypeEnum.FUT,
            InstrumentTypeEnum.IND,
            InstrumentTypeEnum.OPT,
            InstrumentTypeEnum.ZNS,
            InstrumentTypeEnum.MER
    );

    public static boolean isValid(Instrument instrument) {
        return StringUtils.hasText(instrument.getName())
                && hasValidIsin(instrument)
                && hasValidQuote(instrument);
    }

    private static boolean hasValidIsin(Instrument instrument) {
        return StringUtils.hasText(instrument.getSymbolIsin())
                || TYPES_WITHOUT_REQUIRED_ISIN.contains(instrument.getInstrumentType());
    }

    public static boolean hasValidQuote(Instrument instrument) {
        for (final Quote quote : instrument.getQuotes()) {
            if (isValid(quote)) {
                return true;
            }
        }
        return false;
    }

    public static List<Quote> getValidQuotes(Instrument instrument) {
        final List<Quote> result = new ArrayList<>(instrument.getQuotes().size());
        for (final Quote quote : instrument.getQuotes()) {
            if (isValid(quote)) {
                result.add(quote);
            }
        }
        return result;
    }

    public static boolean isValid(Quote quote) {
        return StringUtils.hasText(quote.getSymbol(KeysystemEnum.MMWKN))
                && StringUtils.hasText(quote.getSymbol(KeysystemEnum.VWDFEED))
                && StringUtils.hasText(quote.getCurrency().getSymbolIso())
                && !is1DTBQuote(quote);
    }

    private static boolean is1DTBQuote(Quote quote) {
        return quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.STK
                && "DTB".equals(quote.getSymbolVwdfeedMarket());
    }

}
