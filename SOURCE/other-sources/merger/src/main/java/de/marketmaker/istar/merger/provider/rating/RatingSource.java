/*
 * RatingSource.java
 *
 * Created on 07.05.13 17:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * The {@link #fullName} has to match with {@code de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderBNDIssuer.RatingSource#sectionId}.
 *
 * @author zzhao
 */
public enum RatingSource {
    Fitch("Fitch", Selector.RATING_FITCH),
    Moodys("Moodys", Selector.RATING_MOODYS),
    SnP("Standard & Poor's", Selector.RATING_SuP);

    private final String fullName;

    private final Selector selector;

    private RatingSource(String fullName, Selector selector) {
        this.fullName = fullName;
        this.selector = selector;
    }

    public String getFullName() {
        return fullName;
    }

    public static RatingSource fromFullName(String fullName) {
        for (RatingSource ratingSource : values()) {
            if (ratingSource.fullName.equalsIgnoreCase(fullName)) {
                return ratingSource;
            }
        }

        return null;
    }

    public boolean accept(Profile profile) {
        return profile.isAllowed(this.selector);
    }

    @Override
    public String toString() {
        return name() + "/" + this.fullName;
    }
}
