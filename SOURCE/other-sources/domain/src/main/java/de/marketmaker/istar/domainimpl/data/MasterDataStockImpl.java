/*
 * MasterDataFundImpl.java
 *
 * Created on 11.08.2006 18:54:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataStock;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MasterDataStockImpl implements Serializable, MasterDataStock {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;
    private BigDecimal dividend;
    private BigDecimal dividendLastYear;
    private String dividendCurrency;
    private LocalDate dividendExDay;
    private LocalizedString sector;
    private String gicsSectorKey;
    private String gicsIndustryGroupKey;
    private String gicsIndustryKey;
    private String gicsSubIndustryKey;
    private LocalizedString gicsSector;
    private LocalizedString gicsIndustryGroup;
    private LocalizedString gicsIndustry;
    private LocalizedString gicsSubIndustry;
    private String mostLiquidMarket;

    public MasterDataStockImpl(long instrumentid, BigDecimal dividend,
            BigDecimal dividendLastYear, String dividendCurrency, LocalDate dividendExDay,
            LocalizedString sector,
            String gicsSectorKey,
            String gicsIndustryGroupKey,
            String gicsIndustryKey,
            String gicsSubIndustryKey,
            LocalizedString gicsSector,
            LocalizedString gicsIndustryGroup,
            LocalizedString gicsIndustry,
            LocalizedString gicsSubIndustry,
            String mostLiquidMarkt) {
        this.instrumentid = instrumentid;
        this.dividend = dividend;
        this.dividendLastYear = dividendLastYear;
        this.dividendCurrency = dividendCurrency;
        this.dividendExDay = dividendExDay;
        this.sector = sector;
        this.gicsSectorKey = gicsSectorKey;
        this.gicsIndustryGroupKey = gicsIndustryGroupKey;
        this.gicsIndustryKey = gicsIndustryKey;
        this.gicsSubIndustryKey = gicsSubIndustryKey;
        this.gicsSector = gicsSector;
        this.gicsIndustryGroup = gicsIndustryGroup;
        this.gicsIndustry = gicsIndustry;
        this.gicsSubIndustry = gicsSubIndustry;
        this.mostLiquidMarket = mostLiquidMarkt;
    }

    public MasterDataStock merge(MasterDataStock other) {
        if (this.dividend == null) {
            this.dividend = other.getDividend();
        }
        if (this.dividendLastYear == null) {
            this.dividendLastYear = other.getDividendLastYear();
        }
        if (this.dividendCurrency == null) {
            this.dividendCurrency = other.getDividendCurrency();
        }
        if (this.dividendExDay == null) {
            this.dividendExDay = other.getDividendExDay();
        }
        if (this.sector == null) {
            this.sector = other.getSector();
        }
        if (this.gicsSectorKey == null) {
            this.gicsSectorKey = other.getGicsSectorKey();
        }
        if (this.gicsIndustryGroupKey == null) {
            this.gicsIndustryGroupKey = other.getGicsIndustryGroupKey();
        }
        if (this.gicsIndustryKey == null) {
            this.gicsIndustryKey = other.getGicsIndustryKey();
        }
        if (this.gicsSubIndustryKey == null) {
            this.gicsSubIndustryKey = other.getGicsSubIndustryKey();
        }
        if (this.gicsSector == null) {
            this.gicsSector = other.getGicsSector();
        }
        if (this.gicsIndustryGroup == null) {
            this.gicsIndustryGroup = other.getGicsIndustryGroup();
        }
        if (this.gicsIndustry == null) {
            this.gicsIndustry = other.getGicsIndustry();
        }
        if (this.gicsSubIndustry == null) {
            this.gicsSubIndustry = other.getGicsSubIndustry();
        }
        if (this.mostLiquidMarket == null) {
            this.mostLiquidMarket = other.getMostLiquidMarket();
        }
        return this;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public BigDecimal getDividend() {
        return dividend;
    }

    public BigDecimal getDividendLastYear() {
        return dividendLastYear;
    }

    public String getDividendCurrency() {
        return dividendCurrency;
    }

    public LocalDate getDividendExDay() {
        return dividendExDay;
    }

    public LocalizedString getSector() {
        return sector;
    }

    @Override
    public String getGicsSectorKey() {
        return gicsSectorKey;
    }

    @Override
    public String getGicsIndustryGroupKey() {
        return gicsIndustryGroupKey;
    }

    @Override
    public String getGicsIndustryKey() {
        return gicsIndustryKey;
    }

    @Override
    public String getGicsSubIndustryKey() {
        return gicsSubIndustryKey;
    }

    @Override
    public LocalizedString getGicsSector() {
        return this.gicsSector;
    }

    @Override
    public LocalizedString getGicsIndustryGroup() {
        return this.gicsIndustryGroup;
    }

    @Override
    public LocalizedString getGicsIndustry() {
        return this.gicsIndustry;
    }

    @Override
    public LocalizedString getGicsSubIndustry() {
        return this.gicsSubIndustry;
    }

    @Override
    public String getMostLiquidMarket() {
        return this.mostLiquidMarket;
    }

    public String toString() {
        return "MasterDataStockImpl[instrumentid=" + instrumentid
                + ", dividend=" + dividend
                + ", dividendLastYear=" + dividendLastYear
                + ", dividendCurrency=" + dividendCurrency
                + ", dividendExDay=" + dividendExDay
                + ", sector=" + sector
                + ", gicsSectorKey=" + gicsSectorKey
                + ", gicsIndustryGroupKey=" + gicsIndustryGroupKey
                + ", gicsIndustryKey=" + gicsIndustryKey
                + ", gicsSubIndustryKey=" + gicsSubIndustryKey
                + ", gicsSector=" + gicsSector
                + ", gicsIndustryGroup=" + gicsIndustryGroup
                + ", gicsIndustry=" + gicsIndustry
                + ", gicsSubIndustry=" + gicsSubIndustry
                + ", mostLiquidMarket=" + mostLiquidMarket
                + "]";
    }
}