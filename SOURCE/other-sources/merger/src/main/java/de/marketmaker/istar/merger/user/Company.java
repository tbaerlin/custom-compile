/*
 * Company.java
 *
 * Created on 02.08.2006 10:13:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Company implements Serializable {
    private Map<String, Property> properties;

    private long id;

    private String name;

    private static final int DEFAULT_MAX_NUM_PORTFOLIOS = 5;

    private static final int DEFAULT_MAX_NUM_WATCHLISTS = 5;

    private static final int DEFAULT_MAX_NUM_PORTFOLIOPOSITIONS = 50;

    private static final int DEFAULT_MAX_NUM_WATCHLISTPOSITIONS = 50;


    public String toString() {
        return "Company[" + this.id + ", " + this.name + "]";
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        final String override = System.getProperty(key);
        if (override != null) {
            return "null".equals(override) ? null : override;
        }

        if (this.properties == null) {
            return null;
        }
        final Property property = this.properties.get(key);
        return property != null ? property.getValue() : null;
    }

    private int getIntProperty(String key, int defaultValue) {
        final String s = getProperty(key);
        return (s != null) ? Integer.parseInt(s) : defaultValue;
    }

    public int getMaxNumPortfoliosPerUser() {
        return getIntProperty("user.max.num.portfolios", DEFAULT_MAX_NUM_PORTFOLIOS);
    }

    public int getMaxNumWatchlistsPerUser() {
        return getIntProperty("user.max.num.watchlists", DEFAULT_MAX_NUM_WATCHLISTS);
    }

    public int getMaxNumPositionsPerPortfolio() {
        return getIntProperty("portfolio.max.num.positions", DEFAULT_MAX_NUM_PORTFOLIOPOSITIONS);
    }

    public int getMaxNumPositionsPerWatchlist() {
        return getIntProperty("watchlist.max.num.positions", DEFAULT_MAX_NUM_WATCHLISTPOSITIONS);
    }
}
