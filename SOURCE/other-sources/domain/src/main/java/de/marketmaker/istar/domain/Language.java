/*
 * Language.java
 *
 * Created on 02.01.2006 09:50:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

import java.util.List;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum Language {
    // ORDER IS IMPORTANT! DO NOT DELETE OR REARRANGE ANYTHING, ONLY APPEND IS PERMITTED
    de(Locale.GERMAN),
    en(Locale.ENGLISH),
    fr(Locale.FRENCH),
    it(Locale.ITALIAN),
    nl(new Locale("nl"));

    private final Locale locale;

    Language(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public static int getNumLanguages() {
        return Language.values().length;
    }

    /**
     * @param values localized strings, where each value's index corresponds to the ordinal of the
     * corresponding language enum.
     * @return localized value
     */
    public String resolve(String[] values) {
        return (values != null && ordinal() < values.length) ? values[ordinal()] : null;
    }

    /**
     * Returns the Language that corresponds to the locale
     * @param locale
     * @return language or {@link #en} if no corresponding Language is available
     */
    public static Language valueOf(Locale locale) {
        try {
            return Language.valueOf(locale.getLanguage());
        } catch (IllegalArgumentException e) {
            return Language.en;
        }
    }

    public static boolean hasFirstLocaleDeLanguage(List<Locale> locales) {
        return ((locales != null &&
                !locales.isEmpty() &&
                locales.get(0).getLanguage().equals(Language.de.getLocale().getLanguage())));
    }
}
