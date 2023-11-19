/*
 * NameStrategy.java
 *
 * Created on 26.06.12 16:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.Locale;

import de.marketmaker.istar.domain.profile.Profile;

/**
 * A strategy to get the name for objects of a certain type and that can be adapted with
 * a specific Locale.
 * @author oflege
 */
public interface NameStrategy<T extends NameStrategy<T, V>, V> {
    /**
     * @param v has a name
     * @return v's name wrt. this strategy
     */
    String getName(V v);

    /**
     * Adapts this object based on the given locale and profile
     * @return variant of this strategy for the locale and profile
     */
    T with(Locale locale, Profile profile);
}
