/*
 * MasterDataFundImpl.java
 *
 * Created on 11.08.2006 18:54:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataFund;

/**
 * MasterDataFund implementation. Instances are created using a Builder as follows:
 * <pre>
 * MasterDataFundImpl.Builder b = new MasterDataFundImpl.Builder(12345);
 * b.setIssuerFax(...);
 * ... // set other properties
 * MasterDataFund mdf = b.build();
 * </pre>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class MasterDataFundImpl implements Serializable, MasterDataFund, Cloneable {
    protected static final long serialVersionUID = 1L;

    public enum IssueSurchargeType {
        NET, GROSS, UNKNOWN
    }

    public static class Builder {
        private long instrumentid = -1;

        private LocalizedString.Builder strategy;

        private String currency;

        private LocalizedString.Builder fundtype;

        private LocalizedString.Builder broadassetclass;

        private LocalizedString.Builder investmentFocus;

        private LocalizedString.Builder benchmarkName;

        private Long benchmarkQid;

        private boolean withDefaultBenchmark = true;

        private LocalizedString.Builder issuerName;

        private LocalizedString.Builder issuerOrganization;

        private String issuerAddress;

        private String issuerStreet;

        private String issuerPostalcode;

        private String issuerCity;

        private String issuerEmail;

        private String issuerUrl;

        private String issuerPhone;

        private String issuerFax;

        private String issuerCountryCode;

        private DateTime issueDate;

        private DateTime reportDate;

        private BigDecimal lastDistribution;

        private String distributionCurrency;

        private DateTime lastDistributionDate;

        private BigDecimal fundVolume;

        private String fundVolumeCurrency;

        private DateTime fundVolumeDate;

        private LocalizedString.Builder fundManager;

        private DistributionStrategy distributionStrategy;

        private BigDecimal distributionCount;

        private BigDecimal issueSurcharge;

        private IssueSurchargeType issueSurchargeType;

        private BigDecimal issueSurchargeNet;

        private BigDecimal issueSurchargeGross;

        private BigDecimal managementFee;

        private BigDecimal redemptionFee;

        private BigDecimal accountFee;

        private LocalizedString.Builder accountFeeInfo;

        private BigDecimal performanceFee;

        private BigDecimal ter;

        private BigDecimal allinFee;

        private String fundtypeBviCoarse;

        private LocalizedString.Builder country;

        private BigDecimal minimumInvestment;

        private LocalizedString.Builder minimumInvestmentDescription;

        private String marketAdmission;

        private String morningstarOverallRating;

        private String fidaRating;

        private DateTime morningstarRatingDate;

        private DateTime portfolioDate;

        private BigDecimal duration;

        private BigDecimal modifiedDuration;

        private Integer fwwRiskclass;

        private String customer;

        private boolean fundOfFunds;

        private BigDecimal ongoingCharge;

        private DateTime ongoingChargeDate;

        private String srriValue;

        private DateTime srriValueDate;

        private String diamondRating;

        private DateTime diamondRatingDate;

        private String efcfClassification;

        private BigDecimal fundclassVolume;

        private String fundclassVolumeCurrency;

        private DateTime fundclassVolumeDate;

        private String permissionType;

        private Source source;

        private BigDecimal taxableIncomeDividend;

        private BigDecimal capitalGuaranteed;

        private Boolean euroPassport;

        private Boolean etfReplication;
        private Boolean etcReplication;
        private Boolean etnReplication;
        private Boolean etpReplication;

        private LocalizedString.Builder etfReplicationLevel;

        private LocalizedString.Builder quoteFrequency;

        private List<DateTime> fundManagerStartDates;

        private List<String> fundManagerNames;

        private Boolean etf;
        private Boolean etc;
        private Boolean etn;
        private Boolean etp;

        private String regionCode;

        private LocalizedString.Builder region;

        private String sectorCode;

        private LocalizedString.Builder sector;

        private LocalizedString.Builder fullName;

        private LocalizedString.Builder shortName;

        private LocalizedString.Builder legalType;

        public Builder() {
            this.distributionStrategy = DistributionStrategy.UNKNOWN;
            this.accountFeeInfo = new LocalizedString.Builder();
            this.strategy = new LocalizedString.Builder();
            this.fundtype = new LocalizedString.Builder();
            this.broadassetclass = new LocalizedString.Builder();
            this.investmentFocus = new LocalizedString.Builder();
            this.benchmarkName = new LocalizedString.Builder();
            this.issuerName = new LocalizedString.Builder();
            this.issuerOrganization = new LocalizedString.Builder();
            this.fundManager = new LocalizedString.Builder();
            this.minimumInvestmentDescription = new LocalizedString.Builder();
            this.country = new LocalizedString.Builder();
            this.etfReplicationLevel = new LocalizedString.Builder();
            this.quoteFrequency = new LocalizedString.Builder();
            this.region = new LocalizedString.Builder();
            this.sector = new LocalizedString.Builder();
            this.fullName = new LocalizedString.Builder();
            this.shortName = new LocalizedString.Builder();
            this.legalType = new LocalizedString.Builder();
        }

        public Builder(MasterDataFundImpl b) {
            this.instrumentid = b.instrumentid;
            this.currency = b.currency;
            this.strategy = new LocalizedString.Builder(b.strategyL);
            this.fundtype = new LocalizedString.Builder(b.fundtypeL);
            this.broadassetclass = new LocalizedString.Builder(b.broadassetclassL);
            this.investmentFocus = new LocalizedString.Builder(b.investmentFocusL);
            this.benchmarkName = new LocalizedString.Builder(b.benchmarkNameL);
            this.benchmarkQid = b.benchmarkQid;
            this.withDefaultBenchmark = b.withDefaultBenchmark;
            this.issuerName = new LocalizedString.Builder(b.issuerNameL);
            this.issuerOrganization = new LocalizedString.Builder(b.issuerOrganizationL);
            this.issueDate = b.issueDate;
            this.fundVolume = b.fundVolume;
            this.fundVolumeCurrency = b.fundVolumeCurrency;
            this.fundVolumeDate = b.fundVolumeDate;
            this.fundManager = new LocalizedString.Builder(b.fundManagerL);
            this.distributionStrategy = b.distributionStrategy;
            this.distributionCount = b.distributionCount;
            this.issueSurcharge = b.issueSurcharge;
            this.issueSurchargeType = b.issueSurchargeType;
            this.issueSurchargeNet = b.issueSurchargeNet;
            this.issueSurchargeGross = b.issueSurchargeGross;
            this.managementFee = b.managementFee;
            this.redemptionFee = b.redemptionFee;
            this.accountFee = b.accountFee;
            this.accountFeeInfo = new LocalizedString.Builder(b.accountFeeInfoL);
            this.performanceFee = b.performanceFee;
            this.ter = b.ter;
            this.issuerAddress = b.issuerAddress;
            this.issuerStreet = b.issuerStreet;
            this.issuerPostalcode = b.issuerPostalcode;
            this.issuerCity = b.issuerCity;
            this.issuerEmail = b.issuerEmail;
            this.issuerUrl = b.issuerUrl;
            this.issuerPhone = b.issuerPhone;
            this.issuerFax = b.issuerFax;
            this.issuerCountryCode = b.issuerCountryCode;
            this.allinFee = b.allinFee;
            this.reportDate = b.reportDate;
            this.lastDistribution = b.lastDistribution;
            this.distributionCurrency = b.distributionCurrency;
            this.lastDistributionDate = b.lastDistributionDate;
            this.fundtypeBviCoarse = b.fundtypeBviCoarse;
            this.minimumInvestment = b.minimumInvestment;
            this.minimumInvestmentDescription = new LocalizedString.Builder(b.minimumInvestmentDescriptionL);
            this.country = new LocalizedString.Builder(b.countryL);
            this.marketAdmission = b.marketAdmission;
            this.portfolioDate = b.portfolioDate;
            this.duration = b.duration;
            this.modifiedDuration = b.modifiedDuration;
            this.morningstarOverallRating = b.morningstarOverallRating;
            this.fidaRating = b.fidaRating;
            this.morningstarRatingDate = b.morningstarRatingDate;
            this.fwwRiskclass = b.fwwRiskclass;
            this.customer = b.customer;
            this.fundOfFunds = b.fundOfFunds;
            this.ongoingCharge = b.ongoingCharge;
            this.ongoingChargeDate = b.ongoingChargeDate;
            this.srriValue = b.srriValue;
            this.srriValueDate = b.srriValueDate;
            this.diamondRating = b.diamondRating;
            this.diamondRatingDate = b.diamondRatingDate;
            this.fundclassVolume = b.fundclassVolume;
            this.fundclassVolumeCurrency = b.fundclassVolumeCurrency;
            this.fundclassVolumeDate = b.fundclassVolumeDate;
            this.efcfClassification = b.efcfClassification;
            this.permissionType = b.permissionType;
            this.taxableIncomeDividend = b.taxableIncomeDividend;
            this.capitalGuaranteed = b.capitalGuaranteed;
            this.euroPassport = b.euroPassport;
            this.etfReplication = b.etfReplication;
            this.etcReplication = b.etcReplication;
            this.etnReplication = b.etnReplication;
            this.etpReplication = b.etpReplication;
            this.etfReplicationLevel = new LocalizedString.Builder(b.etfReplicationLevelL);
            this.quoteFrequency = new LocalizedString.Builder(b.quoteFrequencyL);
            this.fundManagerStartDates = b.fundManagerStartDates;
            this.fundManagerNames = b.fundManagerNames;
            this.etf = b.etf;
            this.etc = b.etc;
            this.etn = b.etn;
            this.etp = b.etp;
            this.regionCode = b.regionCode;
            this.region = new LocalizedString.Builder(b.region);
            this.sectorCode = b.sectorCode;
            this.sector = new LocalizedString.Builder(b.sector);
            this.fullName = new LocalizedString.Builder(b.fullNameL);
            this.shortName = new LocalizedString.Builder(b.shortNameL);
            this.legalType = new LocalizedString.Builder(b.legalTypeL);
        }


        public MasterDataFundImpl build() {
            if (!isValid()) {
                throw new IllegalStateException("instrumentid not set");
            }
            return new MasterDataFundImpl(this);
        }

        public boolean isValid() {
            return this.instrumentid != -1;
        }

        public void setFundOfFunds(boolean fundOfFunds) {
            this.fundOfFunds = fundOfFunds;
        }

        public void setInstrumentid(long instrumentid) {
            this.instrumentid = instrumentid;
        }

        public void setStrategy(String strategy, Language... languages) {
            this.strategy.add(strategy, languages);
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setFundtype(String fundtype, Language... languages) {
            this.fundtype.add(fundtype, languages);
        }

        public void setBroadassetclass(String broadassetclass, Language... languages) {
            this.broadassetclass.add(broadassetclass, languages);
        }

        public void setInvestmentFocus(String investmentFocus, Language... languages) {
            this.investmentFocus.add(investmentFocus, languages);
        }

        public void setBenchmarkName(String benchmarkName, Language... languages) {
            this.benchmarkName.add(benchmarkName, languages);
        }

        public void setBenchmarkQid(Long benchmarkQid) {
            this.benchmarkQid = benchmarkQid;
        }

        public void setWithDefaultBenchmark(boolean withDefaultBenchmark) {
            this.withDefaultBenchmark = withDefaultBenchmark;
        }

        public void setIssuerName(String issuerName, Language... languages) {
            this.issuerName.add(issuerName, languages);
        }

        public void setIssuerOrganization(String issuerOrganization, Language... languages) {
            this.issuerOrganization.add(issuerOrganization, languages);
        }

        public void setIssuerAddress(String issuerAddress) {
            this.issuerAddress = issuerAddress;
        }

        public void setIssuerStreet(String issuerStreet) {
            this.issuerStreet = issuerStreet;
        }

        public void setIssuerPostalcode(String issuerPostalcode) {
            this.issuerPostalcode = issuerPostalcode;
        }

        public void setIssuerCity(String issuerCity) {
            this.issuerCity = issuerCity;
        }

        public void setIssuerEmail(String issuerEmail) {
            this.issuerEmail = issuerEmail;
        }

        public void setIssuerUrl(String issuerUrl) {
            this.issuerUrl = issuerUrl;
        }

        public void setIssuerPhone(String issuerPhone) {
            this.issuerPhone = issuerPhone;
        }

        public void setIssuerFax(String issuerFax) {
            this.issuerFax = issuerFax;
        }

        public void setIssuerCountryCode(String issuerCountryCode) {
            this.issuerCountryCode = issuerCountryCode;
        }

        public void setIssueDate(DateTime issueDate) {
            this.issueDate = issueDate;
        }

        public void setReportDate(DateTime reportDate) {
            this.reportDate = reportDate;
        }

        public void setLastDistribution(BigDecimal lastDistribution) {
            this.lastDistribution = lastDistribution;
        }

        public void setDistributionCurrency(String distributionCurrency) {
            this.distributionCurrency = distributionCurrency;
        }

        public void setLastDistributionDate(DateTime lastDistributionDate) {
            this.lastDistributionDate = lastDistributionDate;
        }

        public void setFundVolume(BigDecimal fundVolume) {
            this.fundVolume = fundVolume;
        }

        public void setFundVolumeCurrency(String fundVolumeCurrency) {
            this.fundVolumeCurrency = fundVolumeCurrency;
        }

        public void setFundVolumeDate(DateTime fundVolumeDate) {
            this.fundVolumeDate = fundVolumeDate;
        }

        public void setFundManager(String fundManager, Language... languages) {
            this.fundManager.add(fundManager, languages);
        }

        public void setDistributionStrategy(DistributionStrategy distributionStrategy) {
            this.distributionStrategy = distributionStrategy;
        }

        public void setDistributionCount(BigDecimal distributionCount) {
            this.distributionCount = distributionCount;
        }

        public void setIssueSurcharge(BigDecimal issueSurcharge) {
            this.issueSurcharge = issueSurcharge;
        }

        public void setIssueSurchargeType(IssueSurchargeType issueSurchargeType) {
            this.issueSurchargeType = issueSurchargeType;
        }

        public void setIssueSurchargeNet(BigDecimal issueSurchargeNet) {
            this.issueSurchargeNet = issueSurchargeNet;
        }

        public void setIssueSurchargeGross(BigDecimal issueSurchargeGross) {
            this.issueSurchargeGross = issueSurchargeGross;
        }

        public void setManagementFee(BigDecimal managementFee) {
            this.managementFee = managementFee;
        }

        public void setRedemptionFee(BigDecimal redemptionFee) {
            this.redemptionFee = redemptionFee;
        }

        public void setAccountFee(BigDecimal accountFee) {
            this.accountFee = accountFee;
        }

        public void setAccountFeeInfo(String accountFeeInfo, Language... languages) {
            this.accountFeeInfo.add(accountFeeInfo, languages);
        }

        public void setPerformanceFee(BigDecimal performanceFee) {
            this.performanceFee = performanceFee;
        }

        public void setTer(BigDecimal ter) {
            this.ter = ter;
        }

        public void setAllinFee(BigDecimal allinFee) {
            this.allinFee = allinFee;
        }

        public void setFundtypeBviCoarse(String fundtypeBviCoarse) {
            this.fundtypeBviCoarse = fundtypeBviCoarse;
        }

        public void setCountry(String country, Language... languages) {
            this.country.add(country, languages);
        }

        public void setMinimumInvestment(BigDecimal minimumInvestment) {
            this.minimumInvestment = minimumInvestment;
        }

        public void setMinimumInvestmentDescription(String minimumInvestmentDescription,
                Language... languages) {
            this.minimumInvestmentDescription.add(minimumInvestmentDescription, languages);
        }

        public void setMarketAdmission(String marketAdmission) {
            this.marketAdmission = marketAdmission;
        }

        public void setPermissionType(String permissionType) {
            this.permissionType = permissionType;
        }

        public void setMorningstarOverallRating(String morningstarOverallRating) {
            this.morningstarOverallRating = morningstarOverallRating;
        }

        public void setFidaRating(String fidaRating) {
            this.fidaRating = fidaRating;
        }

        public void setMorningstarRatingDate(DateTime morningstarRatingDate) {
            this.morningstarRatingDate = morningstarRatingDate;
        }

        public void setPortfolioDate(DateTime portfolioDate) {
            this.portfolioDate = portfolioDate;
        }

        public void setDuration(BigDecimal duration) {
            this.duration = duration;
        }

        public void setModifiedDuration(BigDecimal modifiedDuration) {
            this.modifiedDuration = modifiedDuration;
        }

        public void setFwwRiskclass(Integer fwwRiskclass) {
            this.fwwRiskclass = fwwRiskclass;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        public void setOngoingCharge(BigDecimal ongoingCharge) {
            this.ongoingCharge = ongoingCharge;
        }

        public void setOngoingChargeDate(DateTime ongoingChargeDate) {
            this.ongoingChargeDate = ongoingChargeDate;
        }

        public void setSrriValue(String srriValue) {
            this.srriValue = srriValue;
        }

        public void setSrriValueDate(DateTime srriValueDate) {
            this.srriValueDate = srriValueDate;
        }

        public void setDiamondRating(String diamondRating) {
            this.diamondRating = diamondRating;
        }

        public void setDiamondRatingDate(DateTime diamondRatingDate) {
            this.diamondRatingDate = diamondRatingDate;
        }

        public void setEfcfClassification(String efcfClassification) {
            this.efcfClassification = efcfClassification;
        }

        public void setFundclassVolume(BigDecimal fundclassVolume) {
            this.fundclassVolume = fundclassVolume;
        }

        public void setFundclassVolumeCurrency(String fundclassVolumeCurrency) {
            this.fundclassVolumeCurrency = fundclassVolumeCurrency;
        }

        public void setFundclassVolumeDate(DateTime fundclassVolumeDate) {
            this.fundclassVolumeDate = fundclassVolumeDate;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public void setTaxableIncomeDividend(BigDecimal taxableIncomeDividend) {
            this.taxableIncomeDividend = taxableIncomeDividend;
        }

        public void setCapitalGuaranteed(BigDecimal capitalGuaranteed) {
            this.capitalGuaranteed = capitalGuaranteed;
        }

        public void setEuroPassport(Boolean euroPassport) {
            this.euroPassport = euroPassport;
        }

        public void setEtcReplication(Boolean etcReplication) {
            this.etcReplication = etcReplication;
        }

        public void setEtpReplication(Boolean etpReplication) {
            this.etpReplication = etpReplication;
        }

        public void setEtnReplication(Boolean etnReplication) {
            this.etnReplication = etnReplication;
        }

        public void setEtfReplication(Boolean etfReplication) {
            this.etfReplication = etfReplication;
        }

        public void setEtfReplicationLevel(String etfReplicationLevel, Language... languages) {
            this.etfReplicationLevel.add(etfReplicationLevel, languages);
        }

        public void setQuoteFrequency(String quoteFrequency, Language... languages) {
            this.quoteFrequency.add(quoteFrequency, languages);
        }

        public void setFundManagerStartDates(List<DateTime> fundManagerStartDates) {
            this.fundManagerStartDates = fundManagerStartDates;
        }

        public void setFundManagerNames(List<String> fundManagerNames) {
            this.fundManagerNames = fundManagerNames;
        }

        public void setEtf(Boolean etf) {
            this.etf = etf;
        }

        public void setEtc(Boolean etc) {
            this.etc = etc;
        }

        public void setEtn(Boolean etn) {
            this.etn = etn;
        }

        public void setEtp(Boolean etp) {
            this.etp = etp;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public void setRegion(String region, Language... languages) {
            this.region.add(region, languages);
        }

        public void setSectorCode(String sectorCode) {
            this.sectorCode = sectorCode;
        }

        public void setSector(String sector, Language... languages) {
            this.sector.add(sector, languages);
        }

        public void setFullName(String fullName, Language... languages) {
            this.fullName.add(fullName, languages);
        }

        public void setShortName(String shortName, Language... languages) {
            this.shortName.add(shortName, languages);
        }

        public void setLegalType(String legalType, Language... languages) {
            this.legalType.add(legalType, languages);
        }
    }

    private final long instrumentid;

    private final LocalizedString strategyL;

    private final String currency;

    private final LocalizedString fundtypeL;

    private final LocalizedString broadassetclassL;

    private final LocalizedString investmentFocusL;

    private final LocalizedString benchmarkNameL;

    private final Long benchmarkQid;

    private final boolean withDefaultBenchmark;

    private final LocalizedString issuerNameL;

    private final LocalizedString issuerOrganizationL;

    private final String issuerAddress;

    private final String issuerStreet;

    private final String issuerPostalcode;

    private final String issuerCity;

    private final String issuerEmail;

    private final String issuerUrl;

    private final String issuerPhone;

    private final String issuerFax;

    private final String issuerCountryCode;

    private final DateTime issueDate;

    private final DateTime reportDate;

    private final BigDecimal lastDistribution;

    private final String distributionCurrency;

    private final DateTime lastDistributionDate;

    private final BigDecimal fundVolume;

    private final String fundVolumeCurrency;

    private final DateTime fundVolumeDate;

    private final LocalizedString fundManagerL;

    private final DistributionStrategy distributionStrategy;

    private final BigDecimal distributionCount;

    private final BigDecimal issueSurcharge;

    private final IssueSurchargeType issueSurchargeType;

    private final BigDecimal issueSurchargeNet;

    private final BigDecimal issueSurchargeGross;

    private final BigDecimal managementFee;

    private final BigDecimal redemptionFee;

    private final BigDecimal accountFee;

    private final LocalizedString accountFeeInfoL;

    private final BigDecimal performanceFee;

    private final BigDecimal ter;

    private final BigDecimal allinFee;

    private final String fundtypeBviCoarse;

    private final LocalizedString countryL;

    private final BigDecimal minimumInvestment;

    private final LocalizedString minimumInvestmentDescriptionL;

    private final String marketAdmission;

    private final String permissionType;

    private final String morningstarOverallRating;

    private final String fidaRating;

    private final DateTime morningstarRatingDate;

    private final DateTime portfolioDate;

    private final BigDecimal duration;

    private final BigDecimal modifiedDuration;

    private final Integer fwwRiskclass;

    private final String customer;

    private final boolean fundOfFunds;

    private final BigDecimal ongoingCharge;

    private final DateTime ongoingChargeDate;

    private final String srriValue;

    private final DateTime srriValueDate;

    private final String diamondRating;

    private final DateTime diamondRatingDate;

    private final BigDecimal fundclassVolume;

    private final String fundclassVolumeCurrency;

    private final DateTime fundclassVolumeDate;

    private final String efcfClassification;

    private final Source source;

    private final BigDecimal taxableIncomeDividend;

    private final BigDecimal capitalGuaranteed;

    private final Boolean euroPassport;

    private final Boolean etfReplication;
    private final Boolean etcReplication;
    private final Boolean etnReplication;
    private final Boolean etpReplication;

    private final LocalizedString etfReplicationLevelL;

    private final LocalizedString quoteFrequencyL;

    private final List<DateTime> fundManagerStartDates;

    private final List<String> fundManagerNames;

    private final Boolean etf;

    private final Boolean etc;

    private final Boolean etn;
    private final Boolean etp;


    private String regionCode;

    private final LocalizedString region;

    private String sectorCode;

    private final LocalizedString sector;

    private final LocalizedString fullNameL;

    private final LocalizedString legalTypeL;

    private final LocalizedString shortNameL;

    private MasterDataFundImpl(Builder b) {
        this.instrumentid = b.instrumentid;
        this.currency = b.currency;
        this.strategyL = b.strategy.build();
        this.fundtypeL = b.fundtype.build();
        this.broadassetclassL = b.broadassetclass.build();
        this.investmentFocusL = b.investmentFocus.build();
        this.benchmarkNameL = b.benchmarkName.build();
        this.benchmarkQid = b.benchmarkQid;
        this.withDefaultBenchmark = b.withDefaultBenchmark;
        this.issuerNameL = b.issuerName.build();
        this.issuerOrganizationL = b.issuerOrganization.build();
        this.issueDate = b.issueDate;
        this.fundVolume = b.fundVolume;
        this.fundVolumeCurrency = b.fundVolumeCurrency;
        this.fundVolumeDate = b.fundVolumeDate;
        this.fundManagerL = b.fundManager.build();
        this.distributionStrategy = b.distributionStrategy;
        this.distributionCount = b.distributionCount;
        this.issueSurcharge = b.issueSurcharge;
        this.issueSurchargeType = b.issueSurchargeType;
        this.issueSurchargeNet = b.issueSurchargeNet;
        this.issueSurchargeGross = b.issueSurchargeGross;
        this.managementFee = b.managementFee;
        this.redemptionFee = b.redemptionFee;
        this.accountFee = b.accountFee;
        this.accountFeeInfoL = b.accountFeeInfo.build();
        this.performanceFee = b.performanceFee;
        this.ter = b.ter;
        this.issuerAddress = b.issuerAddress;
        this.issuerStreet = b.issuerStreet;
        this.issuerPostalcode = b.issuerPostalcode;
        this.issuerCity = b.issuerCity;
        this.issuerEmail = b.issuerEmail;
        this.issuerUrl = b.issuerUrl;
        this.issuerPhone = b.issuerPhone;
        this.issuerFax = b.issuerFax;
        this.issuerCountryCode = b.issuerCountryCode;
        this.allinFee = b.allinFee;
        this.reportDate = b.reportDate;
        this.lastDistribution = b.lastDistribution;
        this.distributionCurrency = b.distributionCurrency;
        this.lastDistributionDate = b.lastDistributionDate;
        this.fundtypeBviCoarse = b.fundtypeBviCoarse;
        this.minimumInvestment = b.minimumInvestment;
        this.minimumInvestmentDescriptionL = b.minimumInvestmentDescription.build();
        this.countryL = b.country.build();
        this.marketAdmission = b.marketAdmission;
        this.portfolioDate = b.portfolioDate;
        this.duration = b.duration;
        this.modifiedDuration = b.modifiedDuration;
        this.morningstarOverallRating = b.morningstarOverallRating;
        this.fidaRating = b.fidaRating;
        this.morningstarRatingDate = b.morningstarRatingDate;
        this.fwwRiskclass = b.fwwRiskclass;
        this.customer = b.customer;
        this.fundOfFunds = b.fundOfFunds;
        this.ongoingCharge = b.ongoingCharge;
        this.ongoingChargeDate = b.ongoingChargeDate;
        this.srriValue = b.srriValue;
        this.srriValueDate = b.srriValueDate;
        this.diamondRating = b.diamondRating;
        this.diamondRatingDate = b.diamondRatingDate;
        this.fundclassVolume = b.fundclassVolume;
        this.fundclassVolumeCurrency = b.fundclassVolumeCurrency;
        this.fundclassVolumeDate = b.fundclassVolumeDate;
        this.efcfClassification = b.efcfClassification;
        this.permissionType = b.permissionType;
        this.source = b.source;
        this.taxableIncomeDividend = b.taxableIncomeDividend;
        this.capitalGuaranteed = b.capitalGuaranteed;
        this.euroPassport = b.euroPassport;
        this.etcReplication = b.etcReplication;
        this.etnReplication = b.etnReplication;
        this.etpReplication = b.etpReplication;
        this.etfReplication = b.etfReplication;
        this.etfReplicationLevelL = b.etfReplicationLevel.build();
        this.quoteFrequencyL = b.quoteFrequency.build();
        this.fundManagerStartDates = b.fundManagerStartDates;
        this.fundManagerNames = b.fundManagerNames;
        this.etf = b.etf;
        this.etc = b.etc;
        this.etn = b.etn;
        this.etp = b.etp;
        this.regionCode = b.regionCode;
        this.region = b.region.build();
        this.sectorCode = b.sectorCode;
        this.sector = b.sector.build();
        this.fullNameL = b.fullName.build();
        this.shortNameL = b.shortName.build();
        this.legalTypeL = b.legalType.build();
    }

    public MasterDataFund withoutFidaRating() {
        Builder b = new Builder(this);
        b.setFidaRating(null);
        return b.build();
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalizedString getFundtype() {
        if (this.broadassetclassL != null && StringUtils.hasText(this.broadassetclassL.getDe())) {
            return this.broadassetclassL;
        }

        return this.fundtypeL;
    }

    public String getFundtypeBviCoarse() {
        return fundtypeBviCoarse;
    }

    public LocalizedString getCountry() {
        return this.countryL;
    }

    public LocalizedString getStrategy() {
        return this.strategyL;
    }

    public LocalizedString getInvestmentFocus() {
        return this.investmentFocusL;
    }

    public LocalizedString getBenchmarkName() {
        return this.benchmarkNameL;
    }

    public Long getBenchmarkQid() {
        return benchmarkQid;
    }

    public boolean isWithDefaultBenchmark() {
        return withDefaultBenchmark;
    }

    public LocalizedString getIssuerName() {
        return this.issuerNameL;
    }

    @Override
    public LocalizedString getIssuerOrganization() {
        return this.issuerOrganizationL;
    }

    public DateTime getIssueDate() {
        return issueDate;
    }

    public BigDecimal getFundVolume() {
        return fundVolume;
    }

    public String getFundVolumeCurrency() {
        return fundVolumeCurrency;
    }

    public DateTime getFundVolumeDate() {
        return fundVolumeDate;
    }

    public LocalizedString getFundManager() {
        return this.fundManagerL;
    }

    public DistributionStrategy getDistributionStrategy() {
        return distributionStrategy;
    }

    public BigDecimal getDistributionCount() {
        return distributionCount;
    }

    public BigDecimal getIssueSurcharge() {
        return issueSurcharge;
    }

    public IssueSurchargeType getIssueSurchargeType() {
        return issueSurchargeType;
    }

    public BigDecimal getIssueSurchargeNet() {
        return issueSurchargeNet;
    }

    public BigDecimal getIssueSurchargeGross() {
        return issueSurchargeGross;
    }

    public BigDecimal getManagementFee() {
        return managementFee;
    }

    public BigDecimal getRedemptionFee() {
        return redemptionFee;
    }

    public BigDecimal getAccountFee() {
        return accountFee;
    }

    public BigDecimal getPerformanceFee() {
        return performanceFee;
    }

    public String getIssuerAddress() {
        return issuerAddress;
    }

    public String getIssuerStreet() {
        return issuerStreet;
    }

    public String getIssuerPostalcode() {
        return issuerPostalcode;
    }

    public String getIssuerCity() {
        return issuerCity;
    }

    public String getIssuerEmail() {
        return issuerEmail;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public String getIssuerPhone() {
        return issuerPhone;
    }

    public String getIssuerFax() {
        return issuerFax;
    }

    @Override
    public String getIssuerCountryCode() {
        return issuerCountryCode;
    }

    public BigDecimal getTer() {
        return ter;
    }

    public DateTime getReportDate() {
        return reportDate;
    }

    public BigDecimal getAllinFee() {
        return allinFee;
    }

    public BigDecimal getLastDistribution() {
        return lastDistribution;
    }

    public String getDistributionCurrency() {
        return distributionCurrency;
    }

    public DateTime getLastDistributionDate() {
        return lastDistributionDate;
    }

    public BigDecimal getMinimumInvestment() {
        return minimumInvestment;
    }

    public LocalizedString getAccountFeeInfo() {
        return this.accountFeeInfoL;
    }

    public LocalizedString getMinimumInvestmentDescription() {
        return this.minimumInvestmentDescriptionL;
    }

    public String getMarketAdmission() {
        return marketAdmission;
    }

    @Override
    public boolean isFundOfFunds() {
        return fundOfFunds;
    }

    @Override
    public BigDecimal getOngoingCharge() {
        return ongoingCharge;
    }

    @Override
    public DateTime getOngoingChargeDate() {
        return ongoingChargeDate;
    }

    @Override
    public String getSrriValue() {
        return srriValue;
    }

    @Override
    public DateTime getSrriValueDate() {
        return srriValueDate;
    }

    @Override
    public String getDiamondRating() {
        return diamondRating;
    }

    @Override
    public DateTime getDiamondRatingDate() {
        return diamondRatingDate;
    }

    @Override
    public String getEfcfClassification() {
        return efcfClassification;
    }

    @Override
    public String getPermissionType() {
        return this.permissionType;
    }

    @Override
    public Source getSource() {
        return this.source;
    }

    @Override
    public BigDecimal getFundclassVolume() {
        return fundclassVolume;
    }

    @Override
    public String getFundclassVolumeCurrency() {
        return fundclassVolumeCurrency;
    }

    @Override
    public DateTime getFundclassVolumeDate() {
        return fundclassVolumeDate;
    }

    @Override
    public DateTime getPortfolioDate() {
        return portfolioDate;
    }

    @Override
    public BigDecimal getDuration() {
        return duration;
    }

    @Override
    public BigDecimal getModifiedDuration() {
        return modifiedDuration;
    }

    @Override
    public String getMorningstarOverallRating() {
        return morningstarOverallRating;
    }

    @Override
    public String getFidaRating() {
        return fidaRating;
    }

    @Override
    public DateTime getMorningstarRatingDate() {
        return morningstarRatingDate;
    }

    @Override
    public Integer getFwwRiskclass() {
        return fwwRiskclass;
    }

    @Override
    public String getCustomer() {
        return customer;
    }

    @Override
    public BigDecimal getTaxableIncomeDividend() {
        return taxableIncomeDividend;
    }

    @Override
    public BigDecimal getCapitalGuaranteed() {
        return capitalGuaranteed;
    }

    public Boolean getEuroPassport() {
        return euroPassport;
    }

    public Boolean isEtfReplication() {
        return etfReplication;
    }

    public Boolean isEtcReplication() {
        return etcReplication;
    }

    public Boolean isEtnReplication() {
        return etnReplication;
    }

    public Boolean isEtpReplication() {
        return etpReplication;
    }

    public LocalizedString getEtfReplicationLevel() {
        return etfReplicationLevelL;
    }

    public LocalizedString getQuoteFrequency() {
        return quoteFrequencyL;
    }

    public List<DateTime> getFundManagerStartDates() {
        return fundManagerStartDates;
    }

    public List<String> getFundManagerNames() {
        return fundManagerNames;
    }

    @Override
    public Boolean isEtf() {
        return etf;
    }

    @Override
    public Boolean isEtc() {
        return etc;
    }

    @Override
    public Boolean isEtn() {
        return etn;
    }

    @Override
    public Boolean isEtp() {
        return etp;
    }

    @Override
    public String getRegionCode() {
        return regionCode;
    }

    @Override
    public LocalizedString getRegion() {
        return region;
    }

    @Override
    public String getSectorCode() {
        return sectorCode;
    }

    @Override
    public LocalizedString getSector() {
        return sector;
    }

    @Override
    public LocalizedString getFullName() {
        return fullNameL;
    }

    @Override
    public LocalizedString getShortName() {
        return shortNameL;
    }

    @Override
    public LocalizedString getLegalType() {
        return legalTypeL;
    }

    @Override
    public String toString() {
        return "MasterDataFundImpl{" +
                "instrumentid=" + instrumentid +
                ", strategyL=" + strategyL +
                ", currency='" + currency + '\'' +
                ", fundtypeL=" + fundtypeL +
                ", broadassetclassL=" + broadassetclassL +
                ", investmentFocusL=" + investmentFocusL +
                ", benchmarkNameL=" + benchmarkNameL +
                ", benchmarkQid=" + benchmarkQid +
                ", withDefaultBenchmark=" + withDefaultBenchmark +
                ", issuerNameL=" + issuerNameL +
                ", issuerOrganizationL=" + issuerOrganizationL +
                ", issuerAddress='" + issuerAddress + '\'' +
                ", issuerStreet='" + issuerStreet + '\'' +
                ", issuerPostalcode='" + issuerPostalcode + '\'' +
                ", issuerCity='" + issuerCity + '\'' +
                ", issuerEmail='" + issuerEmail + '\'' +
                ", issuerUrl='" + issuerUrl + '\'' +
                ", issuerPhone='" + issuerPhone + '\'' +
                ", issuerFax='" + issuerFax + '\'' +
                ", issuerCountryCode='" + issuerCountryCode + '\'' +
                ", issueDate=" + issueDate +
                ", reportDate=" + reportDate +
                ", lastDistribution=" + lastDistribution +
                ", distributionCurrency='" + distributionCurrency + '\'' +
                ", lastDistributionDate=" + lastDistributionDate +
                ", fundVolume=" + fundVolume +
                ", fundVolumeCurrency='" + fundVolumeCurrency + '\'' +
                ", fundVolumeDate=" + fundVolumeDate +
                ", fundManagerL=" + fundManagerL +
                ", distributionStrategy=" + distributionStrategy +
                ", distributionCount=" + distributionCount +
                ", issueSurcharge=" + issueSurcharge +
                ", issueSurchargeType=" + issueSurchargeType +
                ", issueSurchargeNet=" + issueSurchargeNet +
                ", issueSurchargeGross=" + issueSurchargeGross +
                ", managementFee=" + managementFee +
                ", redemptionFee=" + redemptionFee +
                ", accountFee=" + accountFee +
                ", accountFeeInfoL=" + accountFeeInfoL +
                ", performanceFee=" + performanceFee +
                ", ter=" + ter +
                ", allinFee=" + allinFee +
                ", fundtypeBviCoarse='" + fundtypeBviCoarse + '\'' +
                ", countryL=" + countryL +
                ", minimumInvestment=" + minimumInvestment +
                ", minimumInvestmentDescriptionL=" + minimumInvestmentDescriptionL +
                ", marketAdmission='" + marketAdmission + '\'' +
                ", permissionType='" + permissionType + '\'' +
                ", morningstarOverallRating='" + morningstarOverallRating + '\'' +
                ", fidaRating='" + fidaRating + '\'' +
                ", morningstarRatingDate=" + morningstarRatingDate +
                ", portfolioDate=" + portfolioDate +
                ", duration=" + duration +
                ", modifiedDuration=" + modifiedDuration +
                ", fwwRiskclass=" + fwwRiskclass +
                ", customer='" + customer + '\'' +
                ", fundOfFunds=" + fundOfFunds +
                ", ongoingCharge=" + ongoingCharge +
                ", ongoingChargeDate=" + ongoingChargeDate +
                ", srriValue='" + srriValue + '\'' +
                ", srriValueDate=" + srriValueDate +
                ", diamondRating='" + diamondRating + '\'' +
                ", diamondRatingDate=" + diamondRatingDate +
                ", fundclassVolume=" + fundclassVolume +
                ", fundclassVolumeCurrency='" + fundclassVolumeCurrency + '\'' +
                ", fundclassVolumeDate=" + fundclassVolumeDate +
                ", efcfClassification='" + efcfClassification + '\'' +
                ", source=" + source +
                ", taxableIncomeDividend=" + taxableIncomeDividend +
                ", capitalGuaranteed=" + capitalGuaranteed +
                ", euroPassport=" + euroPassport +
                ", etfReplication=" + etfReplication +
                ", etfReplicationLevelL=" + etfReplicationLevelL +
                ", quoteFrequencyL=" + quoteFrequencyL +
                ", fundManagerStartDates=" + fundManagerStartDates +
                ", fundManagerNames=" + fundManagerNames +
                ", etf=" + etf +
                ", regionCode='" + regionCode + '\'' +
                ", region=" + region +
                ", sectorCode='" + sectorCode + '\'' +
                ", sector=" + sector +
                ", fullNameL=" + fullNameL +
                ", legalTypeL=" + legalTypeL +
                ", shortNameL=" + shortNameL +
                '}';
    }
}
