/*
 * NullMasterDataCertificate.java
 *
 * Created on 28.07.2006 10:55:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullMasterDataStock implements MasterDataStock, Serializable {
    protected static final long serialVersionUID = 1L;

    public final static MasterDataStock INSTANCE = new NullMasterDataStock();

    private NullMasterDataStock() {
    }

    public long getInstrumentid() {
        return 0L;
    }

    public BigDecimal getDividend() {
        return null;
    }

    public BigDecimal getDividendLastYear() {
        return null;
    }

    public LocalizedString getSector() {
        return null;
    }

    @Override
    public String getGicsSectorKey() {
        return null;
    }

    @Override
    public String getGicsIndustryGroupKey() {
        return null;
    }

    @Override
    public String getGicsIndustryKey() {
        return null;
    }

    @Override
    public String getGicsSubIndustryKey() {
        return null;
    }

    @Override
    public LocalizedString getGicsSector() {
        return null;
    }

    @Override
    public LocalizedString getGicsIndustryGroup() {
        return null;
    }

    @Override
    public LocalizedString getGicsIndustry() {
        return null;
    }

    @Override
    public LocalizedString getGicsSubIndustry() {
        return null;
    }

    @Override
    public String getMostLiquidMarket() {
        return null;
    }

    @Override
    public MasterDataStock merge(MasterDataStock other) {
        return other;
    }

    public String getDividendCurrency() {
        return null;
    }

    public LocalDate getDividendExDay() {
        return null;
    }

    public String toString() {
        return "NullMasterDataStock[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}