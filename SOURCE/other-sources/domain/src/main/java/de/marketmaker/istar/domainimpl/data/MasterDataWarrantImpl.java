/*
 * MasterDataFundImpl.java
 *
 * Created on 11.08.2006 18:54:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.MasterDataWarrant;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MasterDataWarrantImpl implements Serializable, MasterDataWarrant {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;
    private final BigDecimal issuePrice;
    private final YearMonthDay issueDate;
    private final String issuerName;
    private final String currencyStrike;
    private final Boolean american;
    private final LocalDate firstTradingDate;
    private final LocalDate lastTradingDate;


    public MasterDataWarrantImpl(long instrumentid, BigDecimal issuePrice, YearMonthDay issueDate, String issuerName,
                                 Boolean american, String currencyStrike, LocalDate firstTradingDate, LocalDate lastTradingDate) {
        this.instrumentid = instrumentid;
        this.issuePrice = issuePrice;
        this.issueDate = issueDate;
        this.issuerName = issuerName;
        this.american = american;
        this.currencyStrike = currencyStrike;
        this.firstTradingDate = firstTradingDate;
        this.lastTradingDate = lastTradingDate;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public BigDecimal getIssuePrice() {
        return issuePrice;
    }

    public YearMonthDay getIssueDate() {
        return issueDate;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getCurrencyStrike() {
        return currencyStrike;
    }

    public Boolean getAmerican() {
        return american;
    }

    public LocalDate getFirstTradingDate() {
        return firstTradingDate;
    }

    public LocalDate getLastTradingDate() {
        return lastTradingDate;
    }

    public String toString() {
        return "MasterDataWarrantImpl[instrumentid=" + instrumentid
                + ", issuePrice=" + issuePrice
                + ", issueDate=" + issueDate
                + ", issuerName=" + issuerName
                + ", american=" + american
                + ", currencyStrike=" + currencyStrike
                + ", firstTradingDate=" + firstTradingDate
                + ", lastTradingDate=" + lastTradingDate
                + "]";
    }
}