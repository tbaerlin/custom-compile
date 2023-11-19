package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * this contains bits and pieces from the old merger implementation
 */
final class OldMergerToolkit {

    static final Set<String> GERMAN_OS_PLACES =
            new HashSet<>(Arrays.asList(new String[] {
                    "FFM", "ETR", "EEU", "EUS", "HBG", "HNV", "DDF", "STG", "NMX", "NMF", "BLN", "MCH", "BRE",
                    "EUWAX", "FFMST" }));


    // append currency if needed
    static String getMarketSymbol(Quote quote) {
        if (quote == null) {
            return null;
        }
        switch (quote.getInstrument().getInstrumentType()) {
            case FND:
            case BND:
            case CER:
                if (quote.getMarket() == null) {
                    return quote.getSymbolVwdfeedMarket();
                }
                final String symbol = quote.getMarket().getSymbolVwdfeed();
                if ("FONDS".equals(symbol)) {
                    // happens for wkn=HV5AC0
                    return symbol + "." + quote.getCurrency().getSymbolIso();
                }
                else {
                    return quote.getSymbolVwdfeedMarket();
                }
            default:
                return quote.getSymbolVwdfeedMarket();
        }
    }

    static String getDefaultExchange(Instrument instrument) {
        String defaultExchange = instrument.getHomeExchange().getSymbolVwdfeed();
        if (instrument.getInstrumentType() == InstrumentTypeEnum.FND) {
            defaultExchange = OldMergerToolkit.getMarketSymbol(MarketStrategy.FONDFIRST_OLD_STRATEGY.getQuote(instrument));
        } else if ((instrument.getInstrumentType() == InstrumentTypeEnum.CER)) {
            defaultExchange = OldMergerToolkit.getMarketSymbol(MarketStrategy.EUWAXFIRST_OLD_STRATEGY.getQuote(instrument));
        } else if (StringUtils.isEmpty(defaultExchange)
            || !GERMAN_OS_PLACES.contains(defaultExchange)) {
            defaultExchange = OldMergerToolkit.getMarketSymbol(MarketStrategy.MERGER_OLD_STRATEGY.getQuote(instrument));
        }
        return defaultExchange;
    }

        /**
         *  remapping type strings,
         *  the original implementation uses a WP_TYPES array and the mmtype int
         *  there is no mmtype in the instrument class now, this implementation seems to
         *  match the new types to the old types,
         *
         * new type --> old type is:
         * Anleihe (BND) --> Rente
         * Zertifikat (CER) --> Optionsschein
         * Währung (CUR) --> Devise
         * Ware/Rohstoff (WEA) --> Index (WTF?)
         */
    static String getOriginalTypeString(InstrumentTypeEnum type) {
        if (type == null) {
            return "unknown";
        }
        String typestring;
        switch (type) {
            case BND:
                typestring = "Rente";
                break;
            case CER:
                typestring = "Optionsschein";
                break;
            case CUR:
                typestring = "Devise";
                break;
            case MER:
                typestring = "Index";
                break;
            default:
                typestring = type.getName(Language.de);
        }
        return typestring;
    }

}
