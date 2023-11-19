/*
 * Rating.java
 *
 * Created on 08.11.2005 16:33:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.rating;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Rating extends Comparable<Rating> {
    String getSymbol();

    String getFullSymbol();

    int ordinal();

    int prefixOrdinal();

    int suffixOrdinal();

    RatingSystem getRatingSystem();
}
