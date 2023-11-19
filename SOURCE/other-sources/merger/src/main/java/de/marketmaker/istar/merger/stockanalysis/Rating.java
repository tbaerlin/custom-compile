/*
 * Rating.java
 *
 * Created on 07.10.2009 13:22:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.StockAnalysis;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum Rating {
    NO_RATING(0, "Ohne", StockAnalysis.Recommendation.NONE),
    STRONG_SELL(1, "stark verkaufen", StockAnalysis.Recommendation.STRONG_SELL),
    SELL(2, "verkaufen", StockAnalysis.Recommendation.SELL),
    HOLD(3, "halten", StockAnalysis.Recommendation.HOLD),
    BUY(4, "kaufen", StockAnalysis.Recommendation.BUY),
    STRONG_BUY(5, "stark kaufen", StockAnalysis.Recommendation.STRONG_BUY),
    SIGN(6, "zeichnen", StockAnalysis.Recommendation.SIGN),
    NOT_SIGN(7, "nicht zeichnen", StockAnalysis.Recommendation.NOT_SIGN);

    private final int id;

    private final String text;

    private final StockAnalysis.Recommendation recommendation;

    private Rating(int id, String text, StockAnalysis.Recommendation recommendation) {
        this.id = id;
        this.text = text;
        this.recommendation = recommendation;
    }

    public String getText() {
        return text;
    }

    public int getId() {
        return id;
    }

    public StockAnalysis.Recommendation getRecommendation() {
        return this.recommendation;
    }

    public static Rating parse(String text) {
        if (!StringUtils.hasText(text)) {
            return NO_RATING;
        }

        for (final Rating rating : values()) {
            if (rating.text.equalsIgnoreCase(text)) {
                return rating;
            }
        }
        return null;
    }

    public static Rating parse(int id) {
        return (id >= 0 && id < values().length) ? values()[id] : null;
    }
}
