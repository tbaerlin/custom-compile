/*
 * NullBasicHistoricRatios.java
 *
 * Created on 01.10.2006 13:42:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.EdgData;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullEdgData implements Serializable, EdgData {
    protected static final long serialVersionUID = 1L;
    public final static NullEdgData INSTANCE = new NullEdgData();

    private NullEdgData() {
    }

    public long getInstrumentid() {
        return 0;
    }

    public String getIsin() {
        return null;
    }

    public LocalDate getEdgRatingDate() {
        return null;
    }

    public Integer getEdgScore1() {
        return null;
    }

    public Integer getEdgScore2() {
        return null;
    }

    public Integer getEdgScore3() {
        return null;
    }

    public Integer getEdgScore4() {
        return null;
    }

    public Integer getEdgScore5() {
        return null;
    }

    public Integer getEdgTopScore() {
        return null;  
    }

    public Integer getEdgTopClass() {
        return null;
    }

    public LocalDate getDdvDate() {
        return null;
    }

    public BigDecimal getDdvVar10d() {
        return null;
    }

    public BigDecimal getDdvPriceRisk10d() {
        return null;
    }

    public BigDecimal getDdvInterestRisk10d() {
        return null;
    }

    public BigDecimal getDdvCurrencyRisk10d() {
        return null;
    }

    public BigDecimal getDdvIssuerRisk10d() {
        return null;
    }

    public BigDecimal getDdvVolatilityRisk10d() {
        return null;
    }

    public BigDecimal getDdvDiversificationRisk10d() {
        return null;
    }

    public BigDecimal getDdvTimevalue10d() {
        return null;
    }

    public BigDecimal getDdvVar250d() {
        return null;
    }

    public BigDecimal getDdvPriceRisk250d() {
        return null;
    }

    public BigDecimal getDdvInterestRisk250d() {
        return null;
    }

    public BigDecimal getDdvCurrencyRisk250d() {
        return null;
    }

    public BigDecimal getDdvIssuerRisk250d() {
        return null;
    }

    public BigDecimal getDdvVolatilityRisk250d() {
        return null;
    }

    public BigDecimal getDdvDiversificationRisk250d() {
        return null;
    }

    public BigDecimal getDdvTimevalue250d() {
        return null;  
    }

    public Integer getDdvRiskclass10d() {
        return null;
    }
}