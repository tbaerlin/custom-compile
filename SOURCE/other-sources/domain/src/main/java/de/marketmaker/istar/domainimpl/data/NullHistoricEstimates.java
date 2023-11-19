/*
 * NullHistoricEstimates.java
 *
 * Created on 16.09.2008 11:03:32
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
public class NullHistoricEstimates implements HistoricEstimates, Serializable {
    protected static final long serialVersionUID = 1L;

    public final static HistoricEstimates INSTANCE = new NullHistoricEstimates();
        
    private NullHistoricEstimates() {
    }

    public long getInstrumentid() {
        return 0;
    }

    public BigDecimal getEarningPerShare1m() {
        return null;
    }

    public BigDecimal getEarningPerShare3m() {
        return null;
    }

    public BigDecimal getEarningPerShareGrowth1m() {
        return null;
    }

    public BigDecimal getEarningPerShareGrowth3m() {
        return null;
    }

    public BigDecimal getCashflowPerShare1m() {
        return null;
    }

    public BigDecimal getCashflowPerShare3m() {
        return null;
    }

    public BigDecimal getPriceEarningRatio1m() {
        return null;
    }

    public BigDecimal getPriceEarningRatio3m() {
        return null;
    }

    public BigDecimal getPreTaxProfit1m() {
        return null;
    }

    public BigDecimal getPreTaxProfit3m() {
        return null;
    }

    public BigDecimal getRecommendation1w() {
        return null;
    }

    public BigDecimal getRecommendation2w() {
        return null;
    }

    public BigDecimal getRecommendation3w() {
        return null;
    }

    public BigDecimal getRecommendation1m() {
        return null;
    }

    public BigDecimal getRecommendation2m() {
        return null;
    }

    public BigDecimal getRecommendation3m() {
        return null;
    }

    public BigDecimal getRecommendation4m() {
        return null;
    }

    public Integer getNumBuy1w() {
        return null;
    }

    public Integer getNumOverweight1w() {
        return null;
    }

    public Integer getNumHold1w() {
        return null;
    }

    public Integer getNumUnderweight1w() {
        return null;
    }

    public Integer getNumSell1w() {
        return null;
    }

    public Integer getNumTotal1w() {
        return null;
    }

    public Integer getNumBuy1m() {
        return null;
    }

    public Integer getNumOverweight1m() {
        return null;
    }

    public Integer getNumHold1m() {
        return null;
    }

    public Integer getNumUnderweight1m() {
        return null;
    }

    public Integer getNumSell1m() {
        return null;
    }

    public Integer getNumTotal1m() {
        return null;
    }
}
