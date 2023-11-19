/*
 * InstrumentComparator.java
 *
 * Created on 25.02.2005 14:43:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.search.SortField;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexConstants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultInstrumentSorter implements InstrumentSorter {
    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_SORT_INSTRUMENT_DEFAULT, SortField.INT);

    private static final Set<String> GERMAN_MARKETS = new HashSet<>(Arrays.asList(
            "ETR",
            "FFM",
            "FFMST",
            "EEU",
            "EUS",
            "STG",
            "DTB",
            "HBG",
            "BLN",
            "STG2",
            "EUWAX",
            "DDF",
            "MCH",
            "HNV")
    );

    public void prepare(File indexBaseDir, boolean update) {
        // empty
    }

    public void afterInstrumentIndexed() {
        // empty
    }

    public String getOrder(Instrument instrument) {
        int result = getType(instrument.getInstrumentType()) << 27;  // 4 bit
        result |= (getHomeType(instrument) << 26); // 1 bit
        result |= (getQuotesSize(instrument) << 20); // 6 bit
        result |= (getFirstCharsValue(instrument) << 4); // 16 bit
        result |= getLength(instrument);
        return Integer.toString(result);
    }

    public SortField getSortField() {
        return SORT_FIELD;
    }

    private static int getLength(Instrument instrument) {
        if (!StringUtils.hasText(instrument.getName())) {
            return 0xf;
        }
        return Math.min(0xf, instrument.getName().length() / 4);
    }

    private static int getFirstCharsValue(Instrument instrument) {
        if (!StringUtils.hasText(instrument.getName())) {
            return 0xff;
        }
        final char c1 = convert(Character.toLowerCase(instrument.getName().charAt(0)));
        if (instrument.getName().length() > 1) {
            final char c2 = convert(Character.toLowerCase(instrument.getName().charAt(1)));
            return (c1 << 8) | c2;
        }
        return c1 << 8;
    }

    private static char convert(char c) {
        switch (c) {
            case 'ä':
                return 'a';
            case 'ö':
                return 'o';
            case 'ü':
                return 'u';
            default:
                return (char) Math.min(255, c);
        }
    }

    private static int getQuotesSize(Instrument instrument) {
        return Math.max(0, 63 - instrument.getQuotes().size());
    }

    private static int getHomeType(Instrument instrument) {
        return isGerman(instrument) ? 0 : 1;
    }

    private static int getType(InstrumentTypeEnum type) {
        switch (type) {
            case STK:
                return 0x0;
            case IND:
            case ZNS:
                return 0x1;
            case FND:
                return 0x2;
            case BND:
                return 0x3;
            case GNS:
                return 0x4;
            case CER:
                return 0x5;
            case WNT:
                return 0x6;
            case CUR:
                return 0x7;
            case OPT:
                return 0x8;
            case FUT:
                return 0x9;
            case IMO:
            case MER:
                return 0xa;
            case MK:
                return 0xb;
            case NON:
                return 0xc;
            case UND:
                return 0xd;
            default:
                return 0xe;
        }
    }

    private static boolean isGerman(Instrument instrument) {
        for (final Quote quote : instrument.getQuotes()) {
            final String market = quote.getMarket().getSymbol(KeysystemEnum.VWDFEED);
            if (GERMAN_MARKETS.contains(market)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        for (InstrumentTypeEnum e : InstrumentTypeEnum.values()) {
            System.out.println(e.name() + ": " + Integer.toHexString(getType(e) << 27));
        }

    }
}
