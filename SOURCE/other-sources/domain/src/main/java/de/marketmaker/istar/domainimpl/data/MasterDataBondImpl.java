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

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataBond;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MasterDataBondImpl implements Serializable, MasterDataBond {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;
    private final LocalizedString bondType;
    private final LocalizedString couponType;
    private final DateTime issueDate;
    private final BigDecimal nominalInterest;
    private final BigDecimal faceValue;
    private final BigDecimal redemptionPrice;
    private final BigDecimal issueVolume;
    private final Integer numberOfCoupons;
    private final LocalizedString interestPeriod;
    private final DateTime interestDate;
    private final String issuerName;
    private final String countryOfIssuer;
    private final String countryOfIssuerCode;
    private final String couponDateDayNumber;
    private final String couponDateMonthNumber;
    private final BigDecimal issuePrice;
    private final String issueCurrency;
    private final String interestType;
    private final String interestRunDeviation;
    private final String interestCalculationMethod;
    private final DateTime expirationDate;
    private final String couponFrequency;
    private final Integer couponDateDays;
    private final DateTime firstCouponDate;
    private final DateTime lastCouponDate;
    private final DateTime couponDate;
    private final BigDecimal smallestTransferableUnit;
    private final BigDecimal minAmountOfTransferableUnit;
    private final BigDecimal conversionFactor;
    private final String bondRank;

    public MasterDataBondImpl(long instrumentid, LocalizedString bondType, LocalizedString couponType, DateTime issuedate,
                              BigDecimal nominalInterest, BigDecimal facevalue, BigDecimal redemptionPrice,
                              BigDecimal issuevolume, Integer numberOfCoupons, LocalizedString interestPeriod,
                              DateTime interestdate, String issuername, String countryOfIssuer, String countryOfIssuerCode,
                              String couponDateDayNumber, String couponDateMonthNumber, BigDecimal issuePrice,
                              String issueCurrency, String interestType, String interestRunDeviation,
                              String interestCalculationMethod, DateTime expirationDate, String couponFrequency,
                              Integer couponDateDays, DateTime firstCouponDate, DateTime lastCouponDate,
                              DateTime couponDate, BigDecimal smallestTransferableUnit,
                              BigDecimal minAmountOfTransferableUnit, BigDecimal conversionFactor,
                              String bondRank) {
        this.instrumentid = instrumentid;
        this.bondType = bondType;
        this.couponType = couponType;
        this.issueDate = issuedate;
        this.nominalInterest = nominalInterest;
        this.faceValue = facevalue;
        this.redemptionPrice = redemptionPrice;
        this.issueVolume = issuevolume;
        this.numberOfCoupons = numberOfCoupons;
        this.interestPeriod = interestPeriod;
        this.interestDate = interestdate;
        this.issuerName = issuername;
        this.countryOfIssuer = countryOfIssuer;
        this.countryOfIssuerCode = countryOfIssuerCode;
        this.couponDateDayNumber = couponDateDayNumber;
        this.couponDateMonthNumber = couponDateMonthNumber;
        this.issuePrice = issuePrice;
        this.issueCurrency = issueCurrency;
        this.interestType = interestType;
        this.interestRunDeviation = interestRunDeviation;
        this.interestCalculationMethod = interestCalculationMethod;
        this.expirationDate = expirationDate;
        this.couponFrequency = couponFrequency;
        this.couponDateDays = couponDateDays;
        this.firstCouponDate = firstCouponDate;
        this.lastCouponDate = lastCouponDate;
        this.couponDate = couponDate;
        this.smallestTransferableUnit = smallestTransferableUnit;
        this.minAmountOfTransferableUnit = minAmountOfTransferableUnit;
        this.conversionFactor = conversionFactor;
        this.bondRank=bondRank;
    }

    public long getInstrumentid() {
        return instrumentid;
    }


    public LocalizedString getBondType() {
        return bondType;
    }

    @Override
    public String getBondRank() {
        return this.bondRank;
    }

    public LocalizedString getCouponType() {
        return couponType;
    }

    public DateTime getIssueDate() {
        return issueDate;
    }

    public BigDecimal getNominalInterest() {
        return nominalInterest;
    }

    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public BigDecimal getRedemptionPrice() {
        return redemptionPrice;
    }

    public BigDecimal getIssueVolume() {
        return issueVolume;
    }

    public Integer getNumberOfCoupons() {
        return numberOfCoupons;
    }

    public DateTime getInterestDate() {
        return interestDate;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getCountryOfIssuer() {
        return countryOfIssuer;
    }

    @Override
    public String getCountryOfIssuerCode() {
        return countryOfIssuerCode;
    }

    public LocalizedString getInterestPeriod() {
        return interestPeriod;
    }

    public String getCouponDateDayNumber() {
        return couponDateDayNumber;
    }

    public String getCouponDateMonthNumber() {
        return couponDateMonthNumber;
    }

    public BigDecimal getIssuePrice() {
        return issuePrice;
    }

    public String getIssueCurrency() {
        return issueCurrency;
    }

    public String getInterestType() {
        return interestType;
    }

    public String getInterestRunDeviation() {
        return interestRunDeviation;
    }

    public String getInterestCalculationMethod() {
        return interestCalculationMethod;
    }

    public DateTime getExpirationDate() {
        return expirationDate;
    }

    public String getCouponFrequency() {
        return couponFrequency;
    }

    public Integer getCouponDateDays() {
        return couponDateDays;
    }

    public DateTime getFirstCouponDate() {
        return firstCouponDate;
    }

    public DateTime getLastCouponDate() {
        return lastCouponDate;
    }

    public DateTime getCouponDate() {
        return couponDate;
    }

    public BigDecimal getSmallestTransferableUnit() {
        return smallestTransferableUnit;
    }

    @Override
    public BigDecimal getMinAmountOfTransferableUnit() {
        return minAmountOfTransferableUnit;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public String toString() {
        return "MasterDataFundImpl[instrumentid=" + instrumentid
                + ", bondType=" + bondType
                + ", coupontype=" + couponType
                + ", issueDate=" + issueDate
                + ", nominalInterest=" + nominalInterest
                + ", faceValue=" + faceValue
                + ", redemptionPrice=" + redemptionPrice
                + ", issueVolume=" + issueVolume
                + ", numberOfCoupons=" + numberOfCoupons
                + ", interestPeriod=" + interestPeriod
                + ", interestDate=" + interestDate
                + ", issuerName=" + issuerName
                + ", countryOfIssuer=" + countryOfIssuer
                + ", couponDateDayNumber=" + this.couponDateDayNumber
                + ", couponDateMonthNumber=" + this.couponDateMonthNumber
                + ", issuePrice=" + this.issuePrice
                + ", issueCurrency=" + this.issueCurrency
                + ", interestType=" + this.interestType
                + ", interestRunDeviation=" + this.interestRunDeviation
                + ", interestCalculationMethod=" + this.interestCalculationMethod
                + ", expirationDate=" + this.expirationDate
                + ", couponFrequency=" + this.couponFrequency
                + ", couponDateDays=" + this.couponDateDays
                + ", firstCouponDate=" + this.firstCouponDate
                + ", lastCouponDate=" + this.lastCouponDate
                + ", couponDate=" + this.couponDate
                + ", smallestTransferableUnit=" + this.smallestTransferableUnit
                + ", minAmountOfTransferableUnit=" + this.minAmountOfTransferableUnit
                + ", conversionFactor=" + this.conversionFactor
                + "]";
    }
}