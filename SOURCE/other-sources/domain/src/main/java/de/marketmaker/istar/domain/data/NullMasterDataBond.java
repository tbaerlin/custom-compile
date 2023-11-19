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

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullMasterDataBond implements Serializable,MasterDataBond {
    protected static final long serialVersionUID = 1L;

    public final static MasterDataBond INSTANCE = new NullMasterDataBond();

    private NullMasterDataBond() {
    }

    public LocalizedString getBondType() {
        return null;
    }

    @Override
    public String getBondRank() {
        return null;
    }

    public LocalizedString getCouponType() {
        return null;
    }

    public DateTime getIssueDate() {
        return null;
    }

    public BigDecimal getNominalInterest() {
        return null;
    }

    public BigDecimal getFaceValue() {
        return null;
    }

    public BigDecimal getRedemptionPrice() {
        return null;
    }

    public BigDecimal getIssueVolume() {
        return null;
    }

    public Integer getNumberOfCoupons() {
        return null;
    }

    public DateTime getInterestDate() {
        return null;
    }

    public LocalizedString getInterestPeriod() {
        return null;
    }

    public String getIssuerName() {
        return null;
    }

    public String getCountryOfIssuer() {
        return null;
    }

    public String getCountryOfIssuerCode() {
        return null;
    }

    public String getCouponDateDayNumber() {
        return null;
    }

    public String getCouponDateMonthNumber() {
        return null;
    }

    public BigDecimal getIssuePrice() {
        return null;
    }

    public String getIssueCurrency() {
        return null;
    }

    public String getInterestType() {
        return null;
    }

    public String getInterestRunDeviation() {
        return null;
    }

    public String getInterestCalculationMethod() {
        return null;
    }

    public DateTime getExpirationDate() {
        return null;
    }

    public String getCouponFrequency() {
        return null;
    }

    public Integer getCouponDateDays() {
        return null;
    }

    public DateTime getFirstCouponDate() {
        return null;
    }

    public DateTime getLastCouponDate() {
        return null;
    }

    public DateTime getCouponDate() {
        return null;
    }

    public BigDecimal getSmallestTransferableUnit() {
        return null;
    }

    @Override
    public BigDecimal getMinAmountOfTransferableUnit() {
        return null;
    }

    public BigDecimal getConversionFactor() {
        return null;
    }

    public String toString() {
        return "NullMasterDataBond[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}
