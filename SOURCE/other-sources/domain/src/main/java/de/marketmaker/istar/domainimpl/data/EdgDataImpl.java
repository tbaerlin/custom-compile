/*
 * MasterDataFundImpl.java
 *
 * Created on 11.08.2006 18:54:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.common.util.DateUtil;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EdgDataImpl implements Serializable, EdgData {
    protected static final long serialVersionUID = 1L;

    public static final MathContext MC = new MathContext(4, RoundingMode.HALF_EVEN);

    private final long instrumentid;
    private final String isin;
    private final int edgRatingDate;
    private final int scores;
    private final int ddvDate;
    private final double ddvVar10d;
    private final double ddvPriceRisk10d;
    private final double ddvInterestRisk10d;
    private final double ddvCurrencyRisk10d;
    private final double ddvIssuerRisk10d;
    private final double ddvVolatilityRisk10d;
    private final double ddvDiversificationRisk10d;
    private final double ddvTimevalue10d;
    private final double ddvVar250d;
    private final double ddvPriceRisk250d;
    private final double ddvInterestRisk250d;
    private final double ddvCurrencyRisk250d;
    private final double ddvIssuerRisk250d;
    private final double ddvVolatilityRisk250d;
    private final double ddvDiversificationRisk250d;
    private final double ddvTimevalue250d;

    public EdgDataImpl(long instrumentid, String isin, int edgRatingDate, int scores,
                       int ddvDate, double ddvVar10d, double ddvPriceRisk10d, double ddvInterestRisk10d,
                       double ddvCurrencyRisk10d, double ddvIssuerRisk10d, double ddvVolatilityRisk10d,
                       double ddvDiversificationRisk10d, double ddvTimevalue10d, double ddvVar250d, double ddvPriceRisk250d,
                       double ddvInterestRisk250d, double ddvCurrencyRisk250d, double ddvIssuerRisk250d,
                       double ddvVolatilityRisk250d, double ddvDiversificationRisk250d, double ddvTimevalue250d) {
        this.instrumentid = instrumentid;
        this.isin = isin;
        this.edgRatingDate = edgRatingDate;
        this.scores = scores;
        this.ddvDate = ddvDate;
        this.ddvVar10d = ddvVar10d;
        this.ddvPriceRisk10d = ddvPriceRisk10d;
        this.ddvInterestRisk10d = ddvInterestRisk10d;
        this.ddvCurrencyRisk10d = ddvCurrencyRisk10d;
        this.ddvIssuerRisk10d = ddvIssuerRisk10d;
        this.ddvVolatilityRisk10d = ddvVolatilityRisk10d;
        this.ddvDiversificationRisk10d = ddvDiversificationRisk10d;
        this.ddvTimevalue10d = ddvTimevalue10d;
        this.ddvVar250d = ddvVar250d;
        this.ddvPriceRisk250d = ddvPriceRisk250d;
        this.ddvInterestRisk250d = ddvInterestRisk250d;
        this.ddvCurrencyRisk250d = ddvCurrencyRisk250d;
        this.ddvIssuerRisk250d = ddvIssuerRisk250d;
        this.ddvVolatilityRisk250d = ddvVolatilityRisk250d;
        this.ddvDiversificationRisk250d = ddvDiversificationRisk250d;
        this.ddvTimevalue250d = ddvTimevalue250d;
    }

    private Integer decode(int index) {
        // scores (3 bits each, 0 as null, each value +1), order: edgScore1-5, topClass, ddvRiskclass10d
        final int value = (this.scores >> (index * 3)) & 0x7;
        return value == 0 ? null : value - 1;
    }

    private BigDecimal toBigDecimal(double d) {
        return Double.isNaN(d) ? null : BigDecimal.valueOf(d);
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public String getIsin() {
        return isin;
    }

    public LocalDate getEdgRatingDate() {
        return this.edgRatingDate == 0 ? null : DateUtil.yyyyMmDdToLocalDate(this.edgRatingDate);
    }

    public Integer getEdgScore1() {
        return decode(1);
    }

    public Integer getEdgScore2() {
        return decode(2);
    }

    public Integer getEdgScore3() {
        return decode(3);
    }

    public Integer getEdgScore4() {
        return decode(4);
    }

    public Integer getEdgScore5() {
        return decode(5);
    }

    public Integer getEdgTopClass() {
        return decode(6);
    }

    public Integer getEdgTopScore() {
        final Integer index = getEdgTopClass();
        return index == null ? null : decode(index);
    }

    public LocalDate getDdvDate() {
        return this.ddvDate == 0 ? null : DateUtil.yyyyMmDdToLocalDate(this.ddvDate);
    }

    public BigDecimal getDdvVar10d() {
        return toBigDecimal(ddvVar10d);
    }

    public BigDecimal getDdvPriceRisk10d() {
        return toBigDecimal(ddvPriceRisk10d);
    }

    public BigDecimal getDdvInterestRisk10d() {
        return toBigDecimal(ddvInterestRisk10d);
    }

    public BigDecimal getDdvCurrencyRisk10d() {
        return toBigDecimal(ddvCurrencyRisk10d);
    }

    public BigDecimal getDdvIssuerRisk10d() {
        return toBigDecimal(ddvIssuerRisk10d);
    }

    public BigDecimal getDdvVolatilityRisk10d() {
        return toBigDecimal(ddvVolatilityRisk10d);
    }

    public BigDecimal getDdvDiversificationRisk10d() {
        return toBigDecimal(ddvDiversificationRisk10d);
    }

    public BigDecimal getDdvTimevalue10d() {
        return toBigDecimal(ddvTimevalue10d);
    }

    public BigDecimal getDdvVar250d() {
        return toBigDecimal(ddvVar250d);
    }

    public BigDecimal getDdvPriceRisk250d() {
        return toBigDecimal(ddvPriceRisk250d);
    }

    public BigDecimal getDdvInterestRisk250d() {
        return toBigDecimal(ddvInterestRisk250d);
    }

    public BigDecimal getDdvCurrencyRisk250d() {
        return toBigDecimal(ddvCurrencyRisk250d);
    }

    public BigDecimal getDdvIssuerRisk250d() {
        return toBigDecimal(ddvIssuerRisk250d);
    }

    public BigDecimal getDdvVolatilityRisk250d() {
        return toBigDecimal(ddvVolatilityRisk250d);
    }

    public BigDecimal getDdvDiversificationRisk250d() {
        return toBigDecimal(ddvDiversificationRisk250d);
    }

    public BigDecimal getDdvTimevalue250d() {
        return toBigDecimal(ddvTimevalue250d);
    }

    public Integer getDdvRiskclass10d() {
        return decode(7);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EdgDataImpl");
        sb.append("{instrumentid=").append(instrumentid);
        sb.append(", isin='").append(isin).append('\'');
        sb.append(", edgRatingDate=").append(edgRatingDate);
        sb.append(", edgScore1=").append(getEdgScore1());
        sb.append(", edgScore2=").append(getEdgScore2());
        sb.append(", edgScore3=").append(getEdgScore3());
        sb.append(", edgScore4=").append(getEdgScore4());
        sb.append(", edgScore5=").append(getEdgScore5());
        sb.append(", edgTopScore=").append(getEdgTopScore());
        sb.append(", edgTopClass=").append(getEdgTopClass());
        sb.append(", ddvDate=").append(ddvDate);
        sb.append(", ddvVar10d=").append(ddvVar10d);
        sb.append(", ddvPriceRisk10d=").append(ddvPriceRisk10d);
        sb.append(", ddvInterestRisk10d=").append(ddvInterestRisk10d);
        sb.append(", ddvCurrencyRisk10d=").append(ddvCurrencyRisk10d);
        sb.append(", ddvIssuerRisk10d=").append(ddvIssuerRisk10d);
        sb.append(", ddvVolatilityRisk10d=").append(ddvVolatilityRisk10d);
        sb.append(", ddvDiversificationRisk10d=").append(ddvDiversificationRisk10d);
        sb.append(", ddvTimevalue10d=").append(ddvTimevalue10d);
        sb.append(", ddvVar250d=").append(ddvVar250d);
        sb.append(", ddvPriceRisk250d=").append(ddvPriceRisk250d);
        sb.append(", ddvInterestRisk250d=").append(ddvInterestRisk250d);
        sb.append(", ddvCurrencyRisk250d=").append(ddvCurrencyRisk250d);
        sb.append(", ddvIssuerRisk250d=").append(ddvIssuerRisk250d);
        sb.append(", ddvVolatilityRisk250d=").append(ddvVolatilityRisk250d);
        sb.append(", ddvDiversificationRisk250d=").append(ddvDiversificationRisk250d);
        sb.append(", ddvTimevalue250d=").append(ddvTimevalue250d);
        sb.append(", ddvRiskclass10d=").append(getDdvRiskclass10d());
        sb.append('}');
        return sb.toString();
    }
}