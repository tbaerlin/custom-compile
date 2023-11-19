package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;

import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.List;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.FND;
import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.IND;

public final class LbbwMarketStrategy {

    public static final QuoteSelector LBBW_PREFERRED_EXCHANGES = QuoteSelectors.group(
            new QuoteSelectors.ByMarketsymbol("FFM"),
            new QuoteSelectors.ByMarketsymbol("STG"),
            new QuoteSelectors.ByMarketsymbol("ETR"),
            new QuoteSelectors.ByMarketsymbol("EEU"),
            new QuoteSelectors.ByMarketsymbol("EUS"),
            new QuoteSelectors.ByMarketsymbol("BLN"),
            new QuoteSelectors.ByMarketsymbol("MCH"),
            new QuoteSelectors.ByMarketsymbol("DDF"),
            new QuoteSelectors.ByMarketsymbol("HBG"),
            new QuoteSelectors.ByMarketsymbol("HNV"),
            new QuoteSelectors.ByMarketsymbol("FXVWD"),
            new QuoteSelectors.ByMarketsymbol("FX"),
            new QuoteSelectors.ByMarketsymbol("IQ"),
            new QuoteSelectors.ByMarketsymbol("DJ"),
            new QuoteSelectors.ByMarketsymbol("DEKA"),
            new QuoteSelectors.ByMarketsymbol("GMF"));

    public static final QuoteSelector LBBW_FONDS = new QuoteSelectors.ByType(FND, QuoteSelectors.group(
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.EUR"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.USD"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.CHF"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.GBP"),
            new QuoteSelectors.ByMarketsymbol("FONDS")));

    public static final QuoteSelector MSCI = new QuoteSelectors.ByType(IND, QuoteSelectors.group(
            new QuoteSelectors.BySymbolSuffix("_PI_USD"),
            new QuoteSelectors.BySymbolSuffix("_PI_LOC"),
            new QuoteSelectors.ByMarketsymbol("MSCI")));

    public static final QuoteSelector EUWAX_OR_FFMST = QuoteSelectors.group(
            new QuoteSelectors.ByMarketsymbol("STG"),
            new QuoteSelectors.ByMarketsymbol("EUWAX"),
            new QuoteSelectors.ByMarketsymbol("FFMST"));

    public static final QuoteSelector GERMAN_HOME_EXHANGE_OR_INDEX = new QuoteSelector() {
        public Quote select(Instrument instrument, List<Quote> quotes) {
            final Quote quote = QuoteSelectors.HOME_EXCHANGE.select(instrument, quotes);
            if (quote != null &&
                    ("DE".equals(quote.getMarket().getCountry().getSymbolIso())
                            || instrument.getInstrumentType() == InstrumentTypeEnum.IND)) {
                return quote;
            }
            return null;
        }

        @Override
        public String toString() {
            return ClassUtils.getShortName(LbbwMarketStrategy.class) + "#GERMAN_HOME_EXHANGE_OR_INDEX";
        }
    };

    public static final MarketStrategy INSTANCE = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LBBW_FONDS,
                    MSCI,
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    private LbbwMarketStrategy() {
    }
}
