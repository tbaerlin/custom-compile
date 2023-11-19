/*
 * BasicBalanceFiguresImpl.java
 *
 * Created on 09.08.2006 12:13:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import de.marketmaker.istar.domain.data.ProfitAndLoss;
import de.marketmaker.istar.domain.data.ReferenceInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfitAndLossImpl implements ProfitAndLoss, Serializable {
    protected static final long serialVersionUID = 1L;

    public static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    private final ReferenceInterval reference;
    private final String currency;
    private final BigDecimal sales;
    private final BigDecimal profitYear;
    private final BigDecimal dividend;
    private final BigDecimal dividendYield;
    private final BigDecimal earningPerShare;
    private final BigDecimal dilutedEarningPerShare;

    public ProfitAndLossImpl(ReferenceInterval reference, String currency, BigDecimal sales, BigDecimal profitYear, BigDecimal dividend, BigDecimal dividendYield, BigDecimal earningPerShare, BigDecimal dilutedEarningPerShare) {
        this.reference = reference;
        this.currency = currency;
        this.sales= sales;
        this.profitYear = profitYear;
        this.dividend = dividend;
        this.dividendYield = dividendYield;
        this.earningPerShare = earningPerShare;
        this.dilutedEarningPerShare = dilutedEarningPerShare;
    }

    public ReferenceInterval getReference() {
        return reference;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public BigDecimal getProfitYear() {
        return profitYear;
    }

    public BigDecimal getDividend() {
        return dividend;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public BigDecimal getEarningPerShare() {
        return earningPerShare;
    }

    public BigDecimal getDilutedEarningPerShare() {
        return dilutedEarningPerShare;
    }

    public String toString() {
        return "BasicBalanceFiguresImpl[reference=" + reference
                + ", currency=" + currency
                + ", sales=" + sales
                + ", profitYear=" + profitYear
                + ", dividend=" + dividend
                + ", dividendYield=" + dividendYield
                + ", earningPerShare=" + earningPerShare
                + ", dilutedEarningPerShare=" + dilutedEarningPerShare
                + "]";
    }
}
