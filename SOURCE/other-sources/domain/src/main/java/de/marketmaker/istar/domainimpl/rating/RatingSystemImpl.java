/*
 * RatingSystemImpl.java
 *
 * Created on 08.11.2005 16:44:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.rating;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatingSystemImpl implements RatingSystem, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(RatingSystemImpl.class);

    private final String name;

    private final Pattern pattern;

    private final ConcurrentMap<String, Rating> standardRatings = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Rating> ratings = new ConcurrentHashMap<>();

    RatingSystemImpl(String name, String regex) {
        this.name = name;
        this.pattern = StringUtils.isNotBlank(regex) ? Pattern.compile(regex) : null;
    }

    void add(String symbol, int ordinal) {
        final RatingImpl rating = new RatingImpl(this, symbol, ordinal);
        this.standardRatings.putIfAbsent(symbol, rating);
    }

    @Override
    public Rating getRating(String rawSymbol) {
        if (StringUtils.isBlank(rawSymbol)) {
            throw new IllegalArgumentException("rating symbol required");
        }

        final String ratingSymbol = rawSymbol.replace(' ', '_');

        if (this.standardRatings.containsKey(ratingSymbol)) { // standard symbol
            return this.standardRatings.get(ratingSymbol);
        }

        final Rating ret = this.ratings.get(ratingSymbol);
        if (null != ret) {
            return ret;
        }

        if (null == this.pattern) { // cannot determine rating anyway
            return new RatingImpl(this, ratingSymbol, Integer.MAX_VALUE);
        }

        // try to extract rating symbol, prefix a/o suffix
        final Matcher matcher = this.pattern.matcher(ratingSymbol);
        if (!matcher.find()) {
            // if not found, means the symbol is unknown, alert
            this.logger.warn("<getRating> no rating found for '{}', check spec {}",
                    ratingSymbol, this.name);
            return new RatingImpl(this, ratingSymbol, Integer.MAX_VALUE);
        }
        else {
            final String symbol = matcher.group();
            if (!this.standardRatings.containsKey(symbol)) {
                this.logger.warn("<getRating> no standard rating found for symbol: {}" +
                        " check spec {}", symbol + "/" + ratingSymbol, this.name);
                return new RatingImpl(this, ratingSymbol, Integer.MAX_VALUE);
            }

            final String prefix = ratingSymbol.substring(0, matcher.start());
            final String suffix = ratingSymbol.substring(matcher.end());
            final Rating stdRating = this.standardRatings.get(symbol);
            final RatingImpl rating = new RatingImpl(this, ratingSymbol, stdRating.ordinal(),
                    symbol, getPrefixOrdinal(prefix), getSuffixOrdinal(suffix));
            this.ratings.putIfAbsent(ratingSymbol, rating);
            return rating;
        }
    }

    static int getPrefixOrdinal(String prefix) {
        return StringUtils.isNotBlank(prefix) ? 51 : 0;
    }

    static int getSuffixOrdinal(String suffix) {
        return StringUtils.isNotBlank(suffix) ? 1 : 0;
    }

    public String toString() {
        return "RatingSystem["
                + name + ", " + this.standardRatings
                + "]";
    }
}
