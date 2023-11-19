/*
 * NullMasterDataCertificate.java
 *
 * Created on 28.07.2006 10:55:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullMasterDataWarrant implements MasterDataWarrant, Serializable {
    protected static final long serialVersionUID = 1L;

    public final static MasterDataWarrant INSTANCE = new NullMasterDataWarrant();

    private NullMasterDataWarrant() {
    }

    @Override
    public long getInstrumentid() {
        return 0;
    }

    @Override
    public BigDecimal getIssuePrice() {
        return null;
    }

    @Override
    public YearMonthDay getIssueDate() {
        return null;
    }

    @Override
    public String getIssuerName() {
        return null;
    }

    @Override
    public String getCurrencyStrike() {
        return null;  
    }

    @Override
    public Boolean getAmerican() {
        return null;
    }

    @Override
    public LocalDate getFirstTradingDate() {
        return null;
    }

    @Override
    public LocalDate getLastTradingDate() {
        return null;
    }

    @Override
    public String toString() {
        return "NullMasterDataWarrant[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}
