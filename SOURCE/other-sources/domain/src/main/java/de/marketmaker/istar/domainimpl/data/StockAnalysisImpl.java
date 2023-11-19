/*
 * StockAnalysisImpl.java
 *
 * Created on 10.08.2006 13:55:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.StockAnalysis;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisImpl implements Serializable, StockAnalysis {
    protected static final long serialVersionUID = 1L;

    private final String id;
    private final DateTime date;
    private final String source;
    private final String headline;
    private final String sector;
    private final String text;
    private final Long instrumentid;
    private final Recommendation recommendation;
    private final Recommendation previousRecommendation;
    private final BigDecimal target;
    private final BigDecimal previousTarget;
    private final String targetCurrency;
    private final String timeframe;


    public StockAnalysisImpl(
            String id,
            DateTime date,
            String source,
            String headline,
            String sector,
            String text,
            Long instrumentid,
            Recommendation recommendation) {
        this.id = id;
        this.date = date;
        this.source = source;
        this.headline = headline;
        this.sector = sector;
        this.text = text;
        this.instrumentid = instrumentid;
        this.recommendation = recommendation;
        this.previousRecommendation = Recommendation.NONE;
        this.target = null;
        this.previousTarget = null;
        this.targetCurrency = null;
        this.timeframe = null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DateTime getDate() {
        return date;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getHeadline() {
        return headline;
    }

    @Override
    public String getSector() {
        return sector;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Long getInstrumentid() {
        return instrumentid;
    }

    @Override
    public Recommendation getRecommendation() {
        return recommendation;
    }

    @Override
    public Recommendation getPreviousRecommendation() {
        return previousRecommendation;
    }

    @Override
    public BigDecimal getTarget() {
        return target;
    }

    @Override
    public BigDecimal getPreviousTarget() {
        return previousTarget;
    }

    @Override
    public String getTargetCurrency() {
        return targetCurrency;
    }

    @Override
    public String getTimeframe() {
        return timeframe;
    }

    @Override
    public String getCompanyName() {
        return null;
    }

    public String toString() {
        return "StockAnalysisImpl[id=" + id
                + ", date=" + date
                + ", source=" + source
                + ", headline=" + headline
                + ", sector=" + sector
                + ", text=" + text
                + ", instrumentid=" + instrumentid
                + ", recommendation=" + recommendation
                + ", previousRecommendation=" + previousRecommendation
                + ", target=" + target
                + ", previousTarget=" + previousTarget
                + ", targetCurrency=" + targetCurrency
                + ", timeframe=" + timeframe
                + "]";
    }

}
