/*
 * RatingSystem.java
 *
 * Created on 08.11.2005 16:32:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.rating;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RatingSystem {
    Rating getRating(String value);
}
