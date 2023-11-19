/*
 * RatingUtil.java
 *
 * Created on 07.05.13 07:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;

/**
 * @author zzhao
 */
public final class RatingUtil {

    private RatingUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    private static RatingSystem getRatingSystem(RatingSystemProvider provider,
            IssuerRatingDescriptor desc) {
        return desc.isRating() ? provider.getRatingSystem(desc.name()) : null;
    }
}
