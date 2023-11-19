/*
 * HistoricEstimates.java
 *
 * Created on 16.09.2008 10:15:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface HistoricEstimates {
    long getInstrumentid();

    BigDecimal getEarningPerShare1m();

    BigDecimal getEarningPerShare3m();

    BigDecimal getEarningPerShareGrowth1m();

    BigDecimal getEarningPerShareGrowth3m();

    BigDecimal getCashflowPerShare1m();

    BigDecimal getCashflowPerShare3m();

    BigDecimal getPriceEarningRatio1m();

    BigDecimal getPriceEarningRatio3m();

    BigDecimal getPreTaxProfit1m();

    BigDecimal getPreTaxProfit3m();

    BigDecimal getRecommendation1w();

    BigDecimal getRecommendation2w();

    BigDecimal getRecommendation3w();

    BigDecimal getRecommendation1m();

    BigDecimal getRecommendation2m();

    BigDecimal getRecommendation3m();

    BigDecimal getRecommendation4m();

    Integer getNumBuy1w();

    Integer getNumOverweight1w();

    Integer getNumHold1w();

    Integer getNumUnderweight1w();

    Integer getNumSell1w();

    Integer getNumTotal1w();

    Integer getNumBuy1m();

    Integer getNumOverweight1m();

    Integer getNumHold1m();

    Integer getNumUnderweight1m();

    Integer getNumSell1m();

    Integer getNumTotal1m();
}
