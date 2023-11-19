/*
 * StockAnalysisSummaryImpl.java
 *
 * Created on 12.07.2006 15:01:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StockAnalysisSummary {
    int getNumberOfAnalyses();
    BigDecimal getNormalizedRecommendation(boolean strongEqualsNormal);
    DateTime getDate();
    int getNumberOfBuys();
    int getNumberOfStrongBuys();
    int getNumberOfHolds();
    int getNumberOfSells();
    int getNumberOfStrongSells();
}
