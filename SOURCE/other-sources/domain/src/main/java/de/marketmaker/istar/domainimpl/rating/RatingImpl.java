/*
 * RatingImpl.java
 *
 * Created on 08.11.2005 16:36:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.rating;

import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatingImpl implements Rating, Serializable {

    private final RatingSystem ratingSystem;

    private final String symbol;

    private final String fullSymbol;

    private int ordinal;

    private int prefixOrdinal;

    private int suffixOrdinal;

    public RatingImpl(RatingSystem rs, String symbol, int ordinal) {
        this(rs, symbol, ordinal, symbol, 0, 0);
    }

    public RatingImpl(RatingSystem rs, String fullSymbol, int ordinal, String symbol,
            int pre, int post) {
        this.ratingSystem = rs;
        this.fullSymbol = fullSymbol;
        this.symbol = symbol;
        this.ordinal = ordinal;
        this.prefixOrdinal = pre;
        this.suffixOrdinal = post;
    }

    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public String getFullSymbol() {
        return this.fullSymbol;
    }

    public int ordinal() {
        return ordinal;
    }

    @Override
    public int prefixOrdinal() {
        return this.prefixOrdinal;
    }

    @Override
    public int suffixOrdinal() {
        return this.suffixOrdinal;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RatingImpl rating = (RatingImpl) o;

        return ratingSystem.equals(rating.ratingSystem) && fullSymbol.equals(rating.fullSymbol);
    }

    public int hashCode() {
        int result;
        result = ratingSystem.hashCode();
        result = 31 * result + fullSymbol.hashCode();
        return result;
    }

    @Override
    public RatingSystem getRatingSystem() {
        return this.ratingSystem;
    }

    @Override
    public int compareTo(Rating another) {
        if (another.getRatingSystem() != this.ratingSystem) {
            throw new IllegalArgumentException("rating system differs: "
                    + this.ratingSystem + "<->" + another.getRatingSystem());
        }

        if (this.ordinal() != another.ordinal()) {
            return ordinal() - another.ordinal();
        }
        else {
            if (this.prefixOrdinal != another.prefixOrdinal()) {
                return prefixOrdinal() - another.prefixOrdinal();
            }
            else {
                if (this.suffixOrdinal != another.suffixOrdinal()) {
                    return suffixOrdinal() - another.suffixOrdinal();
                }
                else {
                    return another.getFullSymbol().compareTo(getFullSymbol());
                }
            }
        }
    }

    public String toString() {
        return "Rating[" + fullSymbol + ";" + ordinal + ";" + symbol + ";" + prefixOrdinal
                + ";" + suffixOrdinal + "]";
    }
}
