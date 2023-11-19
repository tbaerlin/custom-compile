/*
 * QuoteNameStrategies.java
 *
 * Created on 10.02.12 11:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.Locale;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
public final class QuoteNameStrategies {
    private static class InstrumentBasedQuoteNameStrategy implements QuoteNameStrategy {
        private final InstrumentNameStrategy instrumentNameStrategy;

        private final String strategyInstanceName;

        private InstrumentBasedQuoteNameStrategy(InstrumentNameStrategy instrumentNameStrategy,
                String strategyInstanceName) {
            this.instrumentNameStrategy = instrumentNameStrategy;
            this.strategyInstanceName = strategyInstanceName;
        }

        @Override
        public String getName(Quote quote) {
            return this.instrumentNameStrategy.getName(quote.getInstrument());
        }

        @Override
        public String getStrategyName() {
            return QuoteNameStrategies.class.getName() + '#' + this.strategyInstanceName;
        }
    }

    public static final QuoteNameStrategy DE_WITH_NEW_NAME
            = new InstrumentBasedQuoteNameStrategy(InstrumentNameStrategies.DE_WITH_NEW_NAME, "DE_WITH_NEW_NAME");

    public static final QuoteNameStrategy DE_WITH_NEW_NAME_FREE
            = new InstrumentBasedQuoteNameStrategy(InstrumentNameStrategies.DE_WITH_NEW_NAME_FREE, "DE_WITH_NEW_NAME_FREE");

    public static final QuoteNameStrategy DEFAULT
            = new InstrumentBasedQuoteNameStrategy(InstrumentNameStrategies.DEFAULT, "DEFAULT") {
        @Override
        public QuoteNameStrategy with(Locale locale, Profile profile) {
            if (FeatureFlags.isEnabled(FeatureFlags.Flag.DZ_RELEASE_2015_2)) { // redmine 15195
                if (profile != null && (profile.isAllowed(Selector.DZ_BANK_USER)
                        || profile.isAllowed(Selector.WGZ_BANK_USER))) {
                    return WM_WP_NAME_KURZ;
                }
            }
            if (!locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
                return WM_WP_NAME_KURZ;
            }
            if (FeatureFlags.Flag.NEW_WP_NAMES.isEnabled()) {
                return profile != null && profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)
                        ? DE_WITH_NEW_NAME : DE_WITH_NEW_NAME_FREE;
            }
            return this;
        }
    };


    public static final QuoteNameStrategy WM_WP_NAME_KURZ = new QuoteNameStrategy() {
        @Override
        public String getName(Quote quote) {
            final String result = quote.getSymbolWmWpNameKurz();
            return (result != null) ? result : DEFAULT.getName(quote);
        }

        @Override
        public String getStrategyName() {
            return QuoteNameStrategies.class.getName() + "#WM_WP_NAME_KURZ";
        }
    };

    public static final QuoteNameStrategy WM_WP_NAME_LANG = new QuoteNameStrategy() {
        @Override
        public String getName(Quote quote) {
            final String result = quote.getSymbolWmWpNameLang();
            return (result != null) ? result : DEFAULT.getName(quote);
        }

        @Override
        public String getStrategyName() {
            return QuoteNameStrategies.class.getName() + "#WM_WP_NAME_LANG";
        }
    };

    public static final QuoteNameStrategy DEFAULT_WITH_NEW_NAME = new InstrumentBasedQuoteNameStrategy(InstrumentNameStrategies.DEFAULT, "DEFAULT_WITH_NEW_NAME") {
        @Override
        public QuoteNameStrategy with(Locale locale, Profile profile) {
            if (!locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
                return WM_WP_NAME_KURZ;
            }
            return profile != null && profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)
                ? DE_WITH_NEW_NAME : DE_WITH_NEW_NAME_FREE;
        }
    };

    private QuoteNameStrategies() {
    }
}
