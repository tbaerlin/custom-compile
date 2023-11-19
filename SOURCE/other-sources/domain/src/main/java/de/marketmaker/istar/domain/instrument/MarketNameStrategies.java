/*
 * MarketNameStrategies.java
 *
 * Created on 26.06.12 16:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author oflege
 */
public final class MarketNameStrategies {
    public static final MarketNameStrategy DEFAULT = new DefaultMarketNameStrategy(Language.de);

    public static final MarketNameStrategy DONNER_REUSCHEL = new DefaultMarketNameStrategy(Language.de) {
        private final Set<String> XETRA = new HashSet<>(Arrays.asList("ETR", "EEU", "EUS", "XETF", "ETRI"));

        private final Set<String> STUTTGART = new HashSet<>(Arrays.asList("STG", "STG2", "EUWAX"));

        private final Set<String> FRANKFURT = new HashSet<>(Arrays.asList("FFM", "FFMST", "FFMFO"));


        @Override
        public String getName(Quote quote) {
            final String market = quote.getSymbolVwdfeedMarket();
            if (XETRA.contains(market)) {
                return "Xetra";
            }
            if (FRANKFURT.contains(market)) {
                return "Frankfurt";
            }
            if (STUTTGART.contains(market)) {
                return "Stuttgart";
            }
            return super.getName(quote);
        }
    };

    private static class DefaultMarketNameStrategy implements MarketNameStrategy {
        private final Language language;

        private DefaultMarketNameStrategy(Language language) {
            this.language = language;
        }

        @Override
        public String getName(Quote quote) {
            if (quote.isNullQuote()) {
                return null;
            }

            final String vwdMarket = quote.getSymbolVwdfeedMarket();
            if ("FONDS".equals(vwdMarket)) {
                return InstrumentTypeEnum.FND.getName(language);
            }

            String name = quote.getMarket().getNameOrDefault(this.language);
            if (StringUtils.hasText(name)) {
                return name;
            }

            if (StringUtils.hasText(vwdMarket)) {
                return vwdMarket;
            }

            final String prefix = (this.language == Language.de) ? "Markt" : "Market";
            return prefix + " " + quote.getMarket().getId();
        }

        @Override
        public MarketNameStrategy with(Locale locale, Profile profile) {
            final Language lang = Language.valueOf(locale);
            return (this.language == lang) ? this : new DefaultMarketNameStrategy(lang);
        }

    }
}
