/*
 * HistoricEstimatesImpl.java
 *
 * Created on 16.09.2008 10:22:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.HistoricEstimates;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricEstimatesImpl implements HistoricEstimates, Serializable {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;
    private final BigDecimal earningPerShare1m;
    private final BigDecimal earningPerShare3m;
    private final BigDecimal earningPerShareGrowth1m;
    private final BigDecimal earningPerShareGrowth3m;
    private final BigDecimal cashflowPerShare1m;
    private final BigDecimal cashflowPerShare3m;
    private final BigDecimal priceEarningRatio1m;
    private final BigDecimal priceEarningRatio3m;
    private final BigDecimal preTaxProfit1m;
    private final BigDecimal preTaxProfit3m;
    private final BigDecimal recommendation1w;
    private final BigDecimal recommendation2w;
    private final BigDecimal recommendation3w;
    private final BigDecimal recommendation1m;
    private final BigDecimal recommendation2m;
    private final BigDecimal recommendation3m;
    private final BigDecimal recommendation4m;
    private final Integer numBuy1w;
    private final Integer numOverweight1w;
    private final Integer numHold1w;
    private final Integer numUnderweight1w;
    private final Integer numSell1w;
    private final Integer numTotal1w;
    private final Integer numBuy1m;
    private final Integer numOverweight1m;
    private final Integer numHold1m;
    private final Integer numUnderweight1m;
    private final Integer numSell1m;
    private final Integer numTotal1m;

    public HistoricEstimatesImpl(long instrumentid, BigDecimal earningPerShare1m, BigDecimal earningPerShare3m, BigDecimal earningPerShareGrowth1m, BigDecimal earningPerShareGrowth3m, BigDecimal cashflowPerShare1m, BigDecimal cashflowPerShare3m, BigDecimal priceEarningRatio1m, BigDecimal priceEarningRatio3m, BigDecimal preTaxProfit1m, BigDecimal preTaxProfit3m, BigDecimal recommendation1w, BigDecimal recommendation2w, BigDecimal recommendation3w, BigDecimal recommendation1m, BigDecimal recommendation2m, BigDecimal recommendation3m, BigDecimal recommendation4m, Integer numBuy1w, Integer numOverweight1w, Integer numHold1w, Integer numUnderweight1w, Integer numSell1w, Integer numTotal1w, Integer numBuy1m, Integer numOverweight1m, Integer numHold1m, Integer numUnderweight1m, Integer numSell1m, Integer numTotal1m) {
        this.instrumentid = instrumentid;
        this.earningPerShare1m = earningPerShare1m;
        this.earningPerShare3m = earningPerShare3m;
        this.earningPerShareGrowth1m = earningPerShareGrowth1m;
        this.earningPerShareGrowth3m = earningPerShareGrowth3m;
        this.cashflowPerShare1m = cashflowPerShare1m;
        this.cashflowPerShare3m = cashflowPerShare3m;
        this.priceEarningRatio1m = priceEarningRatio1m;
        this.priceEarningRatio3m = priceEarningRatio3m;
        this.preTaxProfit1m = preTaxProfit1m;
        this.preTaxProfit3m = preTaxProfit3m;
        this.recommendation1w = recommendation1w;
        this.recommendation2w = recommendation2w;
        this.recommendation3w = recommendation3w;
        this.recommendation1m = recommendation1m;
        this.recommendation2m = recommendation2m;
        this.recommendation3m = recommendation3m;
        this.recommendation4m = recommendation4m;
        this.numBuy1w = numBuy1w;
        this.numOverweight1w = numOverweight1w;
        this.numHold1w = numHold1w;
        this.numUnderweight1w = numUnderweight1w;
        this.numSell1w = numSell1w;
        this.numTotal1w = numTotal1w;
        this.numBuy1m = numBuy1m;
        this.numOverweight1m = numOverweight1m;
        this.numHold1m = numHold1m;
        this.numUnderweight1m = numUnderweight1m;
        this.numSell1m = numSell1m;
        this.numTotal1m = numTotal1m;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public BigDecimal getEarningPerShare1m() {
        return earningPerShare1m;
    }

    public BigDecimal getEarningPerShare3m() {
        return earningPerShare3m;
    }

    public BigDecimal getEarningPerShareGrowth1m() {
        return earningPerShareGrowth1m;
    }

    public BigDecimal getEarningPerShareGrowth3m() {
        return earningPerShareGrowth3m;
    }

    public BigDecimal getCashflowPerShare1m() {
        return cashflowPerShare1m;
    }

    public BigDecimal getCashflowPerShare3m() {
        return cashflowPerShare3m;
    }

    public BigDecimal getPriceEarningRatio1m() {
        return priceEarningRatio1m;
    }

    public BigDecimal getPriceEarningRatio3m() {
        return priceEarningRatio3m;
    }

    public BigDecimal getPreTaxProfit1m() {
        return preTaxProfit1m;
    }

    public BigDecimal getPreTaxProfit3m() {
        return preTaxProfit3m;
    }

    public BigDecimal getRecommendation1w() {
        return recommendation1w;
    }

    public BigDecimal getRecommendation2w() {
        return recommendation2w;
    }

    public BigDecimal getRecommendation3w() {
        return recommendation3w;
    }

    public BigDecimal getRecommendation1m() {
        return recommendation1m;
    }

    public BigDecimal getRecommendation2m() {
        return recommendation2m;
    }

    public BigDecimal getRecommendation3m() {
        return recommendation3m;
    }

    public BigDecimal getRecommendation4m() {
        return recommendation4m;
    }

    public Integer getNumBuy1w() {
        return numBuy1w;
    }

    public Integer getNumOverweight1w() {
        return numOverweight1w;
    }

    public Integer getNumHold1w() {
        return numHold1w;
    }

    public Integer getNumUnderweight1w() {
        return numUnderweight1w;
    }

    public Integer getNumSell1w() {
        return numSell1w;
    }

    public Integer getNumTotal1w() {
        return numTotal1w;
    }

    public Integer getNumBuy1m() {
        return numBuy1m;
    }

    public Integer getNumOverweight1m() {
        return numOverweight1m;
    }

    public Integer getNumHold1m() {
        return numHold1m;
    }

    public Integer getNumUnderweight1m() {
        return numUnderweight1m;
    }

    public Integer getNumSell1m() {
        return numSell1m;
    }

    public Integer getNumTotal1m() {
        return numTotal1m;
    }

    public String toString() {
        return "HistoricEstimates[instrumentid=" + instrumentid
                + ", earningPerShare1m=" + earningPerShare1m
                + ", earningPerShare3m=" + earningPerShare3m
                + ", earningPerShareGrowth1m=" + earningPerShareGrowth1m
                + ", earningPerShareGrowth3m=" + earningPerShareGrowth3m
                + ", cashflowPerShare1m=" + cashflowPerShare1m
                + ", cashflowPerShare3m=" + cashflowPerShare3m
                + ", priceEarningRatio1m=" + priceEarningRatio1m
                + ", priceEarningRatio3m=" + priceEarningRatio3m
                + ", preTaxProfit1m=" + preTaxProfit1m
                + ", preTaxProfit3m=" + preTaxProfit3m
                + ", recommendation1w=" + recommendation1w
                + ", recommendation2w=" + recommendation2w
                + ", recommendation3w=" + recommendation3w
                + ", recommendation1m=" + recommendation1m
                + ", recommendation2m=" + recommendation2m
                + ", recommendation3m=" + recommendation3m
                + ", recommendation4m=" + recommendation4m
                + ", numBuy1w=" + numBuy1w
                + ", numOverweight1w=" + numOverweight1w
                + ", numHold1w=" + numHold1w
                + ", numUnderweight1w=" + numUnderweight1w
                + ", numSell1w=" + numSell1w
                + ", numTotal1w=" + numTotal1w
                + ", numBuy1m=" + numBuy1m
                + ", numOverweight1m=" + numOverweight1m
                + ", numHold1m=" + numHold1m
                + ", numUnderweight1m=" + numUnderweight1m
                + ", numSell1m=" + numSell1m
                + ", numTotal1m=" + numTotal1m
                + ", ]";
    }
}
