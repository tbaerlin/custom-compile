/*
 * StockAnalysis.java
 *
 * Created on 12.07.2006 14:47:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StockAnalysis {

    enum Recommendation {
        NONE,
        STRONG_BUY,
        BUY,
        HOLD,
        SELL,
        STRONG_SELL,
        SIGN,
        NOT_SIGN
    }

    String getId();

    Long getInstrumentid();

    DateTime getDate();

    String getSource();

    String getHeadline();

    String getText();

    String getSector();

    Recommendation getRecommendation();

    Recommendation getPreviousRecommendation();

    BigDecimal getTarget();

    BigDecimal getPreviousTarget();

    String getTargetCurrency();

    String getTimeframe();

    /**
     * For old analyses, the instrumentid might no longer be valid. In order to make sense of the
     * analysis, it is helpful to provide the name of the instrument that could be identified when
     * the analyses was inserted.
     */
    String getCompanyName();

}
