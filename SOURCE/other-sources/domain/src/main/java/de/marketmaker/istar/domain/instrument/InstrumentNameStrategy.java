/*
 * QuoteNameStrategy.java
 *
 * Created on 10.02.12 07:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.Locale;

import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author oflege
 */
public interface InstrumentNameStrategy extends NameStrategy<InstrumentNameStrategy, Instrument> {
    /**
     * Create an optionally modified version of this strategy applying locale and profile
     * @param locale Locale to be applied
     * @param profile Profile to be applied
     * @return Strategy with applied locale and profile
     */
    default InstrumentNameStrategy with(Locale locale, Profile profile) {
        return this;
    }
}
