/*
 * ProfiledInstrument.java
 *
 * Created on 18.07.2006 11:12:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class ProfiledInstrument {
    private static final EnumSet<PriceQuality> NOT_NONE_PRICE_QUALITIES =
            EnumSet.complementOf(EnumSet.of(PriceQuality.NONE));

    /**
     * Returns a list of those quotes in i for which p returns a PriceQuality != PriceQuality.NONE
     * @param i instrument to filter
     * @param p profile to use
     * @return list of quotes of instrument i which are allowed by profile
     */
    public static List<Quote> quotesWithPrices(Instrument i, Profile p) {
        return filterQuotes(i, p, NOT_NONE_PRICE_QUALITIES);
    }

    /**
     * Returns a list of those quotes in quotes for which p returns a PriceQuality != PriceQuality.NONE
     * @param quotes to be filtered
     * @param p profile to use
     * @return list of quotes of instrument i which are allowed by profile
     */
    public static List<Quote> quotesWithPrices(List<Quote> quotes, Profile p) {
        return filterQuotes(quotes, p, NOT_NONE_PRICE_QUALITIES);
    }

    /**
     * Returns a list of those quotes in i for which p returns a PriceQuality contained in the
     * priceQualities Set.
     * @param i instrument to filter
     * @param p profile to use
     * @param priceQualities list of price qualities to use as filter
     * @return list of quotes of instrument i which are allowed by profile and priceQualities set
     */
    public static List<Quote> filterQuotes(Instrument i, Profile p,
            EnumSet<PriceQuality> priceQualities) {
        return filterQuotes(i.getQuotes(), p, priceQualities);
    }

    public static List<Quote> filterQuotes(List<Quote> quotes, Profile p,
            EnumSet<PriceQuality> priceQualities) {
        final QuoteFilter bqf = RequestContextHolder.getRequestContext().getBaseQuoteFilter();
        return quotes.stream()
                .filter(bqf)
                .filter(q -> priceQualities.contains(p.getPriceQuality(q)))
                .collect(Collectors.toList());
    }
}
