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
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullMasterDataFund implements Serializable, MasterDataFund {
    protected static final long serialVersionUID = 1L;

    public final static MasterDataFund INSTANCE = new NullMasterDataFund();

    private NullMasterDataFund() {
    }

    public long getInstrumentid() {
        return -1;
    }

    public String getCurrency() {
        return null;
    }

    public BigDecimal getPerformanceFee() {
        return null;
    }

    public String getMorningstarOverallRating() {
        return null;
    }

    public DateTime getMorningstarRatingDate() {
        return null;
    }

    public String getFidaRating() {
        return null;
    }

    public Long getBenchmarkQid() {
        return null;
    }

    public String getDistributionCurrency() {
        return null;
    }

    public DateTime getPortfolioDate() {
        return null;
    }

    public BigDecimal getDuration() {
        return null;
    }

    public BigDecimal getModifiedDuration() {
        return null;
    }

    public LocalizedString getStrategy() {
        return null;
    }

    public LocalizedString getCountry() {
        return null;
    }

    public BigDecimal getLastDistribution() {
        return null;
    }

    public BigDecimal getMinimumInvestment() {
        return null;
    }

    public LocalizedString getMinimumInvestmentDescription() {
        return null;
    }

    public LocalizedString getInvestmentFocus() {
        return null;
    }

    public LocalizedString getBenchmarkName() {
        return null;
    }

    public DateTime getIssueDate() {
        return null;
    }

    public BigDecimal getFundVolume() {
        return null;
    }

    public String getFundVolumeCurrency() {
        return null;
    }

    public LocalizedString getFundManager() {
        return null;
    }

    public DistributionStrategy getDistributionStrategy() {
        return null;
    }

    public BigDecimal getDistributionCount() {
        return null;
    }

    public BigDecimal getIssueSurcharge() {
        return null;
    }

    public MasterDataFundImpl.IssueSurchargeType getIssueSurchargeType() {
        return null;
    }

    public BigDecimal getIssueSurchargeNet() {
        return null;
    }

    public BigDecimal getIssueSurchargeGross() {
        return null;
    }

    public BigDecimal getManagementFee() {
        return null;
    }

    public BigDecimal getRedemptionFee() {
        return null;
    }

    public BigDecimal getAccountFee() {
        return null;
    }

    public LocalizedString getIssuerName() {
        return null;
    }

    @Override
    public LocalizedString getIssuerOrganization() {
        return null;
    }

    public String getIssuerAddress() {
        return null;
    }

    public String getIssuerStreet() {
        return null;
    }

    public String getIssuerPostalcode() {
        return null;
    }

    public String getIssuerCity() {
        return null;
    }

    public String getIssuerEmail() {
        return null;
    }

    public String getIssuerUrl() {
        return null;
    }

    public String getIssuerPhone() {
        return null;
    }

    public String getIssuerFax() {
        return null;
    }

    @Override
    public String getIssuerCountryCode() {
        return null;
    }

    public BigDecimal getTer() {
        return null;
    }

    public LocalizedString getAccountFeeInfo() {
        return null;
    }

    public DateTime getFundVolumeDate() {
        return null;
    }

    public LocalizedString getFundtype() {
        return null;
    }

    public String getFundtypeBviCoarse() {
        return null;
    }

    public DateTime getReportDate() {
        return null;
    }

    public BigDecimal getAllinFee() {
        return null;
    }

    public DateTime getLastDistributionDate() {
        return null;
    }

    public String getMarketAdmission() {
        return null;
    }

    @Override
    public boolean isFundOfFunds() {
        return false;
    }

    @Override
    public BigDecimal getOngoingCharge() {
        return null;
    }

    @Override
    public DateTime getOngoingChargeDate() {
        return null;
    }

    @Override
    public String getSrriValue() {
        return null;
    }

    @Override
    public DateTime getSrriValueDate() {
        return null;
    }

    @Override
    public String getDiamondRating() {
        return null;
    }

    @Override
    public DateTime getDiamondRatingDate() {
        return null;
    }

    @Override
    public String getEfcfClassification() {
        return null;
    }

    @Override
    public String getPermissionType() {
        return null;
    }

    @Override
    public Source getSource() {
        return null;
    }

    @Override
    public BigDecimal getFundclassVolume() {
        return null;
    }

    @Override
    public String getFundclassVolumeCurrency() {
        return null;
    }

    @Override
    public DateTime getFundclassVolumeDate() {
        return null;
    }

    public Integer getFwwRiskclass() {
        return null;
    }

    public String getCustomer() {
        return null;
    }

    public boolean isWithDefaultBenchmark() {
        return true;
    }

    public String toString() {
        return "NullMasterDataFund[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    public String getLanguage() {
        return null;
    }

    @Override
    public BigDecimal getTaxableIncomeDividend() {
        return null;
    }

    @Override
    public BigDecimal getCapitalGuaranteed() {
        return null;
    }

    @Override
    public Boolean getEuroPassport() {
        return null;
    }

    @Override
    public Boolean isEtfReplication() {
        return null;
    }

    @Override
    public Boolean isEtcReplication() {
        return null;
    }

    @Override
    public Boolean isEtnReplication() {
        return null;
    }

    @Override
    public Boolean isEtpReplication() {
        return null;
    }

    @Override
    public LocalizedString getEtfReplicationLevel() {
        return null;
    }

    @Override
    public LocalizedString getQuoteFrequency() {
        return null;
    }

    @Override
    public List<DateTime> getFundManagerStartDates() {
        return null;
    }

    @Override
    public List<String> getFundManagerNames() {
        return null;
    }

    @Override
    public Boolean isEtf() {
        return null;
    }

    @Override
    public Boolean isEtc() {
        return null;
    }

    @Override
    public Boolean isEtn() {
        return null;
    }

    @Override
    public Boolean isEtp() {
        return null;
    }

    @Override
    public String getRegionCode() {
        return null;
    }

    @Override
    public LocalizedString getRegion() {
        return null;
    }

    @Override
    public String getSectorCode() {
        return null;
    }

    @Override
    public LocalizedString getSector() {
        return null;
    }

    @Override
    public LocalizedString getFullName() {
        return LocalizedString.NULL_LOCALIZED_STRING;
    }

    @Override
    public LocalizedString getShortName() {
        return LocalizedString.NULL_LOCALIZED_STRING;
    }

    @Override
    public LocalizedString getLegalType() {
        return LocalizedString.NULL_LOCALIZED_STRING;
    }
}
