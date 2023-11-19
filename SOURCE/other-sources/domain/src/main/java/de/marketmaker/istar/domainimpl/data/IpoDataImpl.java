/*
 * IpoDataImpl.java
 *
 * Created on 15.09.2006 14:08:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.IpoData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IpoDataImpl implements IpoData, Serializable {
    protected static final long serialVersionUID = 1L;

    private final Long instrumentid;
    private final String name;
    private final DateTime issueDate;
    private final String issuePrice;
    private final String issueCurrency;
    private final DateTime startOfSubscriptionPeriod;
    private final DateTime endOfSubscriptionPeriod;
    private final BigDecimal bookbuildingLow;
    private final BigDecimal bookbuildingHigh;
    private final String marketsegment;
    private final List<String> leadManagers;

    public IpoDataImpl(Long instrumentid, String name, DateTime issueDate, String issuePrice, String issueCurrency,
                       DateTime startOfSubscriptionPeriod, DateTime endOfSubscriptionPeriod,
                       BigDecimal bookbuildingLow, BigDecimal bookbuildingHigh, String marketsegment, List<String> leadManagers) {
        this.instrumentid = instrumentid;
        this.name = name;
        this.issueDate = issueDate;
        this.issuePrice = issuePrice;
        this.issueCurrency = issueCurrency;
        this.startOfSubscriptionPeriod = startOfSubscriptionPeriod;
        this.endOfSubscriptionPeriod = endOfSubscriptionPeriod;
        this.bookbuildingLow = bookbuildingLow;
        this.bookbuildingHigh = bookbuildingHigh;
        this.marketsegment = marketsegment;
        this.leadManagers = leadManagers;
    }

    public Long getInstrumentid() {
        return instrumentid;
    }

    public String getName() {
        return name;
    }

    public DateTime getIssueDate() {
        return issueDate;
    }

    public String getIssuePrice() {
        return issuePrice;
    }

    public String getIssueCurrency() {
        return issueCurrency;
    }

    public DateTime getStartOfSubscriptionPeriod() {
        return startOfSubscriptionPeriod;
    }

    public DateTime getEndOfSubscriptionPeriod() {
        return endOfSubscriptionPeriod;
    }

    public BigDecimal getBookbuildingLow() {
        return bookbuildingLow;
    }

    public BigDecimal getBookbuildingHigh() {
        return bookbuildingHigh;
    }

    public String getMarketsegment() {
        return marketsegment;
    }

    public List<String> getLeadManagers() {
        return leadManagers;
    }

    public String toString() {
        return "IpoDataImpl[instrumentid=" + instrumentid
                + ", name=" + name
                + ", issueDate=" + issueDate
                + ", issuePrice=" + issuePrice
                + ", issueCurrency=" + issueCurrency
                + ", startOfSubscriptionPeriod=" + startOfSubscriptionPeriod
                + ", endOfSubscriptionPeriod=" + endOfSubscriptionPeriod
                + ", marketsegment=" + marketsegment
                + ", bookbuildingLow=" + bookbuildingLow
                + ", bookbuildingHigh=" + bookbuildingHigh
                + ", leadManagers=" + leadManagers
                + "]";
    }
}
