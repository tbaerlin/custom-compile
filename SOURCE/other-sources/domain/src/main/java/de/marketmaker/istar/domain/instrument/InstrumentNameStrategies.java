/*
 * InstrumentNameStrategies.java
 *
 * Created on 10.02.12 11:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.Locale;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

import static de.marketmaker.istar.domain.KeysystemEnum.PM_INSTRUMENT_NAME;
import static de.marketmaker.istar.domain.KeysystemEnum.PM_INSTRUMENT_NAME_FREE;

/**
 * @author oflege
 * @author mcoenen
 */
public final class InstrumentNameStrategies {

    public static final InstrumentNameStrategy DEFAULT = new InstrumentNameStrategy() {
        @Override
        public String getName(Instrument instrument) {
            return instrument.getName();
        }

        @Override
        public InstrumentNameStrategy with(Locale locale, Profile profile) {
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

    static final InstrumentNameStrategy DE_WITH_NEW_NAME = instrument -> {
        final String name = instrument.getSymbol(PM_INSTRUMENT_NAME);
        return (name != null) ? name : instrument.getName();
    };

    static final InstrumentNameStrategy DE_WITH_NEW_NAME_FREE = instrument -> {
        final String name = instrument.getSymbol(PM_INSTRUMENT_NAME_FREE);
        return (name != null) ? name : instrument.getName();
    };

    public static final InstrumentNameStrategy WM_WP_NAME_KURZ = instrument -> {
        final Market homeExchange = instrument.getHomeExchange();
        for (Quote quote : instrument.getQuotes()) {
            if (quote.getMarket() == homeExchange) {
                return QuoteNameStrategies.WM_WP_NAME_KURZ.getName(quote);
            }
        }
        return QuoteNameStrategies.WM_WP_NAME_KURZ.getName(instrument.getQuotes().get(0));
    };

    private InstrumentNameStrategies() {
    }
}
