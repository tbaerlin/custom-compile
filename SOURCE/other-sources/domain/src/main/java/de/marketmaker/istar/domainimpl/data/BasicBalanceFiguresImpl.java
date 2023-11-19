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

import de.marketmaker.istar.domain.data.BasicBalanceFigures;
import de.marketmaker.istar.domain.data.ReferenceInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BasicBalanceFiguresImpl implements BasicBalanceFigures, Serializable {
    protected static final long serialVersionUID = 1L;

    public static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    private final ReferenceInterval reference;
    private final String currency;
    private final BigDecimal equityCapital;
    private final BigDecimal shareholdersEquity;
    private final BigDecimal balanceSheetTotal;
    private Integer numberOfEmployees;
    private BigDecimal assets;

    public BasicBalanceFiguresImpl(ReferenceInterval reference, String currency, BigDecimal equityCapital, BigDecimal shareholdersEquity, BigDecimal balanceSheetTotal, Integer numberOfEmployees, BigDecimal assets) {
        this.reference = reference;
        this.currency = currency;
        this.equityCapital = equityCapital;
        this.shareholdersEquity = shareholdersEquity;
        this.balanceSheetTotal = balanceSheetTotal;
        this.numberOfEmployees = numberOfEmployees;
        this.assets = assets;
    }

    public ReferenceInterval getReference() {
        return reference;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getEquityCapital() {
        return equityCapital;
    }

    public BigDecimal getEquityCapitalPercent() {
        if (this.equityCapital == null
                || this.balanceSheetTotal == null || this.balanceSheetTotal.signum() == 0) {
            return null;
        }

        return this.equityCapital.divide(this.balanceSheetTotal, MC);
    }

    public BigDecimal getShareholdersEquity() {
        return shareholdersEquity;
    }

    public BigDecimal getBalanceSheetTotal() {
        return balanceSheetTotal;
    }

    public Integer getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public BigDecimal getAssets() {
        return assets;
    }

    public String toString() {
        return "BasicBalanceFiguresImpl[reference=" + reference
                + ", currency=" + currency
                + ", equityCapital=" + equityCapital
                + ", shareholdersEquity=" + shareholdersEquity
                + ", balanceSheetTotal=" + balanceSheetTotal
                + ", #employees=" + numberOfEmployees
                + ", assets=" + assets
                + "]";
    }
}
