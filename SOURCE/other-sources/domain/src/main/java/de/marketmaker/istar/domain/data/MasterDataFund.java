/*
 * MasterDataFund.java
 *
 * Created on 17.07.2006 13:58:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MasterDataFund extends MasterData {
    String getCustomer();

    LocalizedString getMinimumInvestmentDescription();

    MasterDataFundImpl.IssueSurchargeType getIssueSurchargeType();

    BigDecimal getIssueSurchargeNet();

    BigDecimal getIssueSurchargeGross();

    DateTime getMorningstarRatingDate();

    LocalizedString getAccountFeeInfo();

    DateTime getFundVolumeDate();

    public enum DistributionStrategy {
        RETAINING, DISTRIBUTING, BOTH, UNKNOWN
    }

    /**
     * Please keep in sync with xsd-base.txt.
     */
    public enum Source {
        MORNINGSTAR, SSAT, UNION, FWW, STOCK_SELECTION, FUNDINFO, VWDBENL, FIDA, VWDIT, VWD
    }

    long getInstrumentid();

    String getIssuerStreet();

    String getIssuerPostalcode();

    String getIssuerCity();

    String getIssuerEmail();

    String getCurrency();

    BigDecimal getPerformanceFee();

    String getMorningstarOverallRating();

    String getFidaRating();

    Long getBenchmarkQid();

    boolean isWithDefaultBenchmark();

    String getDistributionCurrency();

    DateTime getPortfolioDate();

    BigDecimal getDuration();

    BigDecimal getModifiedDuration();

    Integer getFwwRiskclass();

    LocalizedString getFundtype();

    String getFundtypeBviCoarse();

    LocalizedString getStrategy();

    LocalizedString getInvestmentFocus();

    LocalizedString getBenchmarkName();

    LocalizedString getIssuerName();

    LocalizedString getIssuerOrganization();

    String getIssuerAddress();

    String getIssuerUrl();

    String getIssuerPhone();

    String getIssuerFax();

    String getIssuerCountryCode();

    DateTime getIssueDate();

    BigDecimal getFundVolume();

    String getFundVolumeCurrency();

    LocalizedString getFundManager();

    DistributionStrategy getDistributionStrategy();

    BigDecimal getDistributionCount();

    BigDecimal getIssueSurcharge();

    BigDecimal getManagementFee();

    BigDecimal getRedemptionFee();

    BigDecimal getAccountFee();

    BigDecimal getTer();

    DateTime getReportDate();

    BigDecimal getAllinFee();

    DateTime getLastDistributionDate();

    LocalizedString getCountry();

    BigDecimal getLastDistribution();

    BigDecimal getMinimumInvestment();

    String getMarketAdmission();

    boolean isFundOfFunds();

    BigDecimal getOngoingCharge();

    DateTime getOngoingChargeDate();

    String getSrriValue();

    DateTime getSrriValueDate();

    String getDiamondRating();

    DateTime getDiamondRatingDate();

    BigDecimal getFundclassVolume();

    String getFundclassVolumeCurrency();

    DateTime getFundclassVolumeDate();

    String getEfcfClassification();

    String getPermissionType();

    Source getSource();

    BigDecimal getTaxableIncomeDividend();

    BigDecimal getCapitalGuaranteed();

    Boolean getEuroPassport();

    Boolean isEtfReplication();

    Boolean isEtcReplication();

    Boolean isEtnReplication();

    Boolean isEtpReplication();

    LocalizedString getEtfReplicationLevel();

    LocalizedString getQuoteFrequency();

    List<DateTime> getFundManagerStartDates();

    List<String> getFundManagerNames();

    Boolean isEtf();

    Boolean isEtc();

    Boolean isEtn();

    Boolean isEtp();

    String getRegionCode();

    LocalizedString getRegion();

    String getSectorCode();

    LocalizedString getSector();

    LocalizedString getFullName();

    LocalizedString getShortName();

    LocalizedString getLegalType();
}

