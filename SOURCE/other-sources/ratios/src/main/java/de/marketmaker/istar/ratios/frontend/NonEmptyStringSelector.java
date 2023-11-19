/*
 * NonEmptyStringSelector.java
 *
 * Created on 20.08.2006 17:49:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class NonEmptyStringSelector implements Selector {
    private RatioFieldDescription.Field field;

    private int localeIndex;

    /**
     * Create new Criterion that checks for a non-empty String value.
     * @param field identifies field to be tested
     * @param locales list of preferred locales
     */
    NonEmptyStringSelector(RatioFieldDescription.Field field, List<Locale> locales) {
        this.field = field;
        if (this.field.getLocales() != null) {
            this.localeIndex = RatioFieldDescription.getLocaleIndex(this.field, locales);
        }
    }

    public boolean select(Selectable s) {
        return StringUtils.hasText(s.getString(this.field.id(), this.localeIndex));
    }

    @Override
    public int getCost() {
        return 1;
    }

    public String toString() {
        return "f(" + this.field.id() + ")!=null|''";
    }
}
