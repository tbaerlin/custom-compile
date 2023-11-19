/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import java.math.BigDecimal;
import org.joda.time.DateTime;

public class RegulatoryReportingRecordImpl implements RegulatoryReportingRecord {

    // 1. MiFID II

    /**
     * ADF_MIFID_Kapitalklasse
     */
    private String mifidCapitalClass;

    /**
     * ADF_MIFID_Assetklasse
     */
    private String mifidAssetClass;

    /**
     * ADF_MIFID_Assetklasse_Zus
     */
    private String mifidAssetClassAddition;

    /**
     * ADF_MIFID_RTS_28_Klasse
     */
    private String mifidClFiRts28;

    /**
     * ADF_MIFID_Kennz_Hebelprodukt
     */
    private String mifidLeverageProductIdentifier;

    /**
     * ADF_MIFID_Tickgroesse
     */
    private String mifidTickSize;

    /**
     * ADF_MIFID_Kategorie_KE
     */
    private String mifidProductCategory;

    /**
     * ADF_MIFID_Kennz_Produktgenehm
     */
    private String mifidProductApprovalProcessIdentifier;

    // 2a. Target market

    /**
     * ADF_TM_Quelle
     */
    private String tmSourceOfTargetMarketData;

    /**
     * ADF_TM_Kundenkategorie
     */
    private String tmCustomerCategory;

    /**
     * ADF_TM_Anlageziele
     */
    private String tmInvestmentObjectives;

    /**
     * ADF_TM_Anlagehorizont
     */
    private String tmInvestmentHorizon;

    /**
     * ADF_TM_Risikoind
     */
    private String tmRiskIndicator;

    /**
     * ADF_TM_Ber_Risikoind
     */
    private String tmCalculationMethodOfRiskIndicator;

    /**
     * ADF_TM_Risikoprofil
     */
    private String tmRiskProfile;

    /**
     * ADF_TM_Verlusttrag
     */
    private String tmFinancialLossBearingCapacity;

    /**
     * ADF_TM_Kenntnisse
     */
    private String tmKnowledgeAndExperience;

    /**
     * ADF_TM_Strategie
     */
    private String tmDistributionStrategy;

    /**
     * ADF_TM_Spez_Anf
     */
    private String tmSpecialRequirements;

    /**
     * ADF_TM_Spez_Anf_Zusatz
     */
    private String tmAddendumToTheSpecialRequirements;

    // 2b. Neg. Target market

    /**
     * ADF_NTM_Kundenkategorie
     */
    private String ntmCustomerCategory;

    /**
     * ADF_NTM_Anlageziele
     */
    private String ntmInvestmentObjectives;

    /**
     * ADF_NTM_Anlagehorizont
     */
    private String ntmInvestmentHorizon;

    /**
     * ADF_NTM_Risikoind
     */
    private String ntmRiskIndicator;

    /**
     * ADF_NTM_Risikoprofil
     */
    private String ntmRiskAndYieldProfile;

    /**
     * ADF_NTM_Verlusttrag
     */
    private String ntmFinancialLossBearingCapacity;

    /**
     * ADF_NTM_Kenntnisse
     */
    private String ntmKnowledgeAndExperience;

    /**
     * ADF_NTM_Strategie
     */
    private String ntmDistributionStrategy;

    /**
     * ADF_NTM_Spez_Anf
     */
    private String ntmSpecialRequirements;

    // 3a. Cost Funds

    /**
     * ADF_CF_Kennz_Performance
     */
    private String cfPerformanceFeeIdentifier;

    /**
     * ADF_CF_Kennz_Swing
     */
    private String cfSwingPricingIdentifier;

    /**
     * ADF_CF_OGC_ExA
     */
    private BigDecimal cfRunningFundCostsEst;

    /**
     * ADF_CF_OGC_Date_ExA
     */
    private DateTime cfRunningFundCostsEstDate;

    /**
     * ADF_CF_Transaction_ExA
     */
    private BigDecimal cfTransactionCostsFundsEst;

    /**
     * ADF_CF_Transaction_Date_ExA
     */
    private DateTime cfTransactionCostsFundsEstDate;

    /**
     * ADF_CF_Incident_ExA
     */
    private BigDecimal cfEventRelatedCostsFundsEst;

    /**
     * ADF_CF_Incident_Date_ExA
     */
    private DateTime cfEventRelatedCostsFundsEstDate;

    /**
     * ADF_CF_Redemption_ExP
     */
    private BigDecimal cfActualRedemptionCostsFund;

    /**
     * ADF_CF_Redemption_Date_ExP
     */
    private DateTime cfActualRedemptionCostsFundDate;

    /**
     * ADF_CF_Min_BackEndLoad_Exp
     */
    private BigDecimal cfMinimumBackEndLoad;

    /**
     * ADF_CF_Min_BackEndLoad_Curr_Exp
     */
    private String cfMinimumBackEndLoadCurrency;

    /**
     * ADF_CF_Min_BackEndLoad_Proz_Exp
     */
    private String cfMinimumBackEndLoadPercentSign;

    /**
     * ADF_CF_Min_BackEndLoad_BG_Exp
     */
    private String cfMinimumBackEndLoadReferenceValue;

    /**
     * ADF_CF_Max_BackEndLoad_Exp
     */
    private BigDecimal cfMaximumBackEndLoad;

    /**
     * ADF_CF_Max_BackEndLoad_Curr_Exp
     */
    private String cfMaximumBackEndLoadCurrency;

    /**
     * ADF_CF_Max_BackEndLoad_Proz_Exp
     */
    private String cfMaximumBackEndLoadPercentSign;

    /**
     * ADF_CF_Max_BackEndLoad_BG_Exp
     */
    private String cfMaximumBackEndLoadReferenceValue;

    /**
     * ADF_CF_Total_Date_from_Exp
     */
    private DateTime cfTotalFundCostsDateFrom;

    /**
     * ADF_CF_Total_Date_to_Exp
     */
    private DateTime cfTotalFundCostsDateTo;

    /**
     * ADF_CF_Transaction_Exp
     */
    private BigDecimal cfTotalFundCostsTransaction;

    /**
     * ADF_CF_OGC_Exp
     */
    private BigDecimal cfTotalFundCostsRunning;

    /**
     * ADF_CF_Incident_Exp
     */
    private BigDecimal cfTotalFundCostsEventRelated;

    /**
     * ADF_CF_Total_Correction_Exp
     */
    private String cfTotalFundCostsCorrectionID;

    // 3b. Cost Structured Products

    /**
     * ADF_CSP_Kennz_FairValue
     */
    private String cspFairValueInstrument;

    /**
     * AFD_CSP_Kennz_OGC
     */
    private String cspInstrumentWithRunningCosts;

    /**
     * ADF_CSP_OGC_ExA
     */
    private BigDecimal cspEstRunningCostsPrFv;

    /**
     * ADF_CSP_OGC_Curr_ExA
     */
    private String cspEstRunningCostsPrFvCurrency;

    /**
     * ADF_CSP_OGC_Proz_ExA
     */
    private String cspEstRunningCostsPrFvPercentSign;

    /**
     * ADF_CSP_OGC_Date_ExA
     */
    private DateTime cspEstRunningCostsPrFvDate;

    /**
     * ADF_CSP_Entry_ExP
     */
    private BigDecimal cspEntryCostsPrFv;

    /**
     * ADF_CSP_Entry_Curr_ExP
     */
    private String cspEntryCostsPrFvCurrency;

    /**
     * ADF_CSP_Entry_Proz_ExP
     */
    private String cspEntryCostsPrFvPercentSign;

    /**
     * ADF_CSP_Entry_Time_ExP
     */
    private DateTime cspEntryCostsPrFvTime;

    /**
     * ADF_CSP_Entry_Correction_ExP
     */
    private String cspEntryCostsPrFvCorrectionId;

    /**
     * ADF_CSP_Exit_ExP
     */
    private BigDecimal cspExitCostsPrFv;

    /**
     * ADF_CSP_Exit_Curr_ExP
     */
    private String cspExitCostsPrFvCurrency;

    /**
     * ADF_CSP_Exit_Proz_ExP
     */
    private String cspExitCostsPrFvPercentSign;

    /**
     * ADF_CSP_Exit_Time_ExP
     */
    private DateTime cspExitCostsPrFvTime;

    /**
     * ADF_CSP_Exit_Correction_ExP
     */
    private String cspExitCostsPrFvCorrectionId;

    /**
     * ADF_CSP_OGC_ExP
     */
    private BigDecimal cspRunningIncrementalCostsPrFv;

    /**
     * ADF_CSP_OGC_Curr_ExP
     */
    private String cspRunningIncrementalCostsPrFvCurrency;

    /**
     * ADF_CSP_OGC_Proz_ExP
     */
    private String cspRunningIncrementalCostsPrFvPercentSign;

    /**
     * ADF_CSP_OGC_Date_ExP
     */
    private DateTime cspRunningIncrementalCostsPrFvDate;

    /**
     * ADF_CSP_OGC_Correction_ExP
     */
    private String cspRunningIncrementalCostsPrFvCorrectionId;

    /**
     * ADF_PRIIPS_Kennz
     */
    private String priipsID;
    private String priipsText;

    @Override
    public String getMifidCapitalClass() {
        return mifidCapitalClass;
    }

    public void setMifidCapitalClass(String mifidCapitalClass) {
        this.mifidCapitalClass = mifidCapitalClass;
    }

    @Override
    public String getMifidAssetClass() {
        return mifidAssetClass;
    }

    public void setMifidAssetClass(String mifidAssetClass) {
        this.mifidAssetClass = mifidAssetClass;
    }

    @Override
    public String getMifidAssetClassAddition() {
        return mifidAssetClassAddition;
    }

    public void setMifidAssetClassAddition(String mifidAssetClassAddition) {
        this.mifidAssetClassAddition = mifidAssetClassAddition;
    }

    @Override
    public String getMifidClFiRts28() {
        return mifidClFiRts28;
    }

    public void setMifidClFiRts28(String mifidClFiRts28) {
        this.mifidClFiRts28 = mifidClFiRts28;
    }

    @Override
    public String getMifidLeverageProductIdentifier() {
        return mifidLeverageProductIdentifier;
    }

    public void setMifidLeverageProductIdentifier(String mifidLeverageProductIdentifier) {
        this.mifidLeverageProductIdentifier = mifidLeverageProductIdentifier;
    }

    @Override
    public String getMifidTickSize() {
        return mifidTickSize;
    }

    public void setMifidTickSize(String mifidTickSize) {
        this.mifidTickSize = mifidTickSize;
    }

    @Override
    public String getMifidProductCategory() {
        return mifidProductCategory;
    }

    public void setMifidProductCategory(String mifidProductCategory) {
        this.mifidProductCategory = mifidProductCategory;
    }

    @Override
    public String getMifidProductApprovalProcessIdentifier() {
        return mifidProductApprovalProcessIdentifier;
    }

    public void setMifidProductApprovalProcessIdentifier(String mifidProductApprovalProcessIdentifier) {
        this.mifidProductApprovalProcessIdentifier = mifidProductApprovalProcessIdentifier;
    }

    @Override
    public String getTmSourceOfTargetMarketData() {
        return tmSourceOfTargetMarketData;
    }

    public void setTmSourceOfTargetMarketData(String tmSourceOfTargetMarketData) {
        this.tmSourceOfTargetMarketData = tmSourceOfTargetMarketData;
    }

    @Override
    public String getTmCustomerCategory() {
        return tmCustomerCategory;
    }

    public void setTmCustomerCategory(String tmCustomerCategory) {
        this.tmCustomerCategory = tmCustomerCategory;
    }

    @Override
    public String getTmInvestmentObjectives() {
        return tmInvestmentObjectives;
    }

    public void setTmInvestmentObjectives(String tmInvestmentObjectives) {
        this.tmInvestmentObjectives = tmInvestmentObjectives;
    }

    @Override
    public String getTmInvestmentHorizon() {
        return tmInvestmentHorizon;
    }

    public void setTmInvestmentHorizon(String tmInvestmentHorizon) {
        this.tmInvestmentHorizon = tmInvestmentHorizon;
    }

    @Override
    public String getTmRiskIndicator() {
        return tmRiskIndicator;
    }

    public void setTmRiskIndicator(String tmRiskIndicator) {
        this.tmRiskIndicator = tmRiskIndicator;
    }

    @Override
    public String getTmCalculationMethodOfRiskIndicator() {
        return tmCalculationMethodOfRiskIndicator;
    }

    public void setTmCalculationMethodOfRiskIndicator(String tmCalculationMethodOfRiskIndicator) {
        this.tmCalculationMethodOfRiskIndicator = tmCalculationMethodOfRiskIndicator;
    }

    @Override
    public String getTmRiskProfile() {
        return tmRiskProfile;
    }

    public void setTmRiskProfile(String tmRiskProfile) {
        this.tmRiskProfile = tmRiskProfile;
    }

    @Override
    public String getTmFinancialLossBearingCapacity() {
        return tmFinancialLossBearingCapacity;
    }

    public void setTmFinancialLossBearingCapacity(String tmFinancialLossBearingCapacity) {
        this.tmFinancialLossBearingCapacity = tmFinancialLossBearingCapacity;
    }

    @Override
    public String getTmKnowledgeAndExperience() {
        return tmKnowledgeAndExperience;
    }

    public void setTmKnowledgeAndExperience(String tmKnowledgeAndExperience) {
        this.tmKnowledgeAndExperience = tmKnowledgeAndExperience;
    }

    @Override
    public String getTmDistributionStrategy() {
        return tmDistributionStrategy;
    }

    public void setTmDistributionStrategy(String tmDistributionStrategy) {
        this.tmDistributionStrategy = tmDistributionStrategy;
    }

    @Override
    public String getTmSpecialRequirements() {
        return tmSpecialRequirements;
    }

    public void setTmSpecialRequirements(String tmSpecialRequirements) {
        this.tmSpecialRequirements = tmSpecialRequirements;
    }

    @Override
    public String getTmAddendumToTheSpecialRequirements() {
        return tmAddendumToTheSpecialRequirements;
    }

    public void setTmAddendumToTheSpecialRequirements(String tmAddendumToTheSpecialRequirements) {
        this.tmAddendumToTheSpecialRequirements = tmAddendumToTheSpecialRequirements;
    }

    @Override
    public String getNtmCustomerCategory() {
        return ntmCustomerCategory;
    }

    public void setNtmCustomerCategory(String ntmCustomerCategory) {
        this.ntmCustomerCategory = ntmCustomerCategory;
    }

    @Override
    public String getNtmInvestmentObjectives() {
        return ntmInvestmentObjectives;
    }

    public void setNtmInvestmentObjectives(String ntmInvestmentObjectives) {
        this.ntmInvestmentObjectives = ntmInvestmentObjectives;
    }

    @Override
    public String getNtmInvestmentHorizon() {
        return ntmInvestmentHorizon;
    }

    public void setNtmInvestmentHorizon(String ntmInvestmentHorizon) {
        this.ntmInvestmentHorizon = ntmInvestmentHorizon;
    }

    @Override
    public String getNtmRiskIndicator() {
        return ntmRiskIndicator;
    }

    public void setNtmRiskIndicator(String ntmRiskIndicator) {
        this.ntmRiskIndicator = ntmRiskIndicator;
    }

    @Override
    public String getNtmRiskAndYieldProfile() {
        return ntmRiskAndYieldProfile;
    }

    public void setNtmRiskAndYieldProfile(String ntmRiskAndYieldProfile) {
        this.ntmRiskAndYieldProfile = ntmRiskAndYieldProfile;
    }

    @Override
    public String getNtmFinancialLossBearingCapacity() {
        return ntmFinancialLossBearingCapacity;
    }

    public void setNtmFinancialLossBearingCapacity(String ntmFinancialLossBearingCapacity) {
        this.ntmFinancialLossBearingCapacity = ntmFinancialLossBearingCapacity;
    }

    @Override
    public String getNtmKnowledgeAndExperience() {
        return ntmKnowledgeAndExperience;
    }

    public void setNtmKnowledgeAndExperience(String ntmKnowledgeAndExperience) {
        this.ntmKnowledgeAndExperience = ntmKnowledgeAndExperience;
    }

    @Override
    public String getNtmDistributionStrategy() {
        return ntmDistributionStrategy;
    }

    public void setNtmDistributionStrategy(String ntmDistributionStrategy) {
        this.ntmDistributionStrategy = ntmDistributionStrategy;
    }

    @Override
    public String getNtmSpecialRequirements() {
        return ntmSpecialRequirements;
    }

    public void setNtmSpecialRequirements(String ntmSpecialRequirements) {
        this.ntmSpecialRequirements = ntmSpecialRequirements;
    }

    @Override
    public String getCfPerformanceFeeIdentifier() {
        return cfPerformanceFeeIdentifier;
    }

    public void setCfPerformanceFeeIdentifier(String cfPerformanceFeeIdentifier) {
        this.cfPerformanceFeeIdentifier = cfPerformanceFeeIdentifier;
    }

    @Override
    public String getCfSwingPricingIdentifier() {
        return cfSwingPricingIdentifier;
    }

    public void setCfSwingPricingIdentifier(String cfSwingPricingIdentifier) {
        this.cfSwingPricingIdentifier = cfSwingPricingIdentifier;
    }

    @Override
    public BigDecimal getCfRunningFundCostsEst() {
        return cfRunningFundCostsEst;
    }

    public void setCfRunningFundCostsEst(BigDecimal cfRunningFundCostsEst) {
        this.cfRunningFundCostsEst = cfRunningFundCostsEst;
    }

    @Override
    public DateTime getCfRunningFundCostsEstDate() {
        return cfRunningFundCostsEstDate;
    }

    public void setCfRunningFundCostsEstDate(DateTime cfRunningFundCostsEstDate) {
        this.cfRunningFundCostsEstDate = cfRunningFundCostsEstDate;
    }

    @Override
    public BigDecimal getCfTransactionCostsFundsEst() {
        return cfTransactionCostsFundsEst;
    }

    public void setCfTransactionCostsFundsEst(BigDecimal cfTransactionCostsFundsEst) {
        this.cfTransactionCostsFundsEst = cfTransactionCostsFundsEst;
    }

    @Override
    public DateTime getCfTransactionCostsFundsEstDate() {
        return cfTransactionCostsFundsEstDate;
    }

    public void setCfTransactionCostsFundsEstDate(DateTime cfTransactionCostsFundsEstDate) {
        this.cfTransactionCostsFundsEstDate = cfTransactionCostsFundsEstDate;
    }

    @Override
    public BigDecimal getCfEventRelatedCostsFundsEst() {
        return cfEventRelatedCostsFundsEst;
    }

    public void setCfEventRelatedCostsFundsEst(BigDecimal cfEventRelatedCostsFundsEst) {
        this.cfEventRelatedCostsFundsEst = cfEventRelatedCostsFundsEst;
    }

    @Override
    public DateTime getCfEventRelatedCostsFundsEstDate() {
        return cfEventRelatedCostsFundsEstDate;
    }

    public void setCfEventRelatedCostsFundsEstDate(DateTime cfEventRelatedCostsFundsEstDate) {
        this.cfEventRelatedCostsFundsEstDate = cfEventRelatedCostsFundsEstDate;
    }

    @Override
    public BigDecimal getCfActualRedemptionCostsFund() {
        return cfActualRedemptionCostsFund;
    }

    public void setCfActualRedemptionCostsFund(BigDecimal cfActualRedemptionCostsFund) {
        this.cfActualRedemptionCostsFund = cfActualRedemptionCostsFund;
    }

    @Override
    public DateTime getCfActualRedemptionCostsFundDate() {
        return cfActualRedemptionCostsFundDate;
    }

    public void setCfActualRedemptionCostsFundDate(DateTime cfActualRedemptionCostsFundDate) {
        this.cfActualRedemptionCostsFundDate = cfActualRedemptionCostsFundDate;
    }

    @Override
    public BigDecimal getCfMinimumBackEndLoad() {
        return cfMinimumBackEndLoad;
    }

    public void setCfMinimumBackEndLoad(BigDecimal cfMinimumBackEndLoad) {
        this.cfMinimumBackEndLoad = cfMinimumBackEndLoad;
    }

    @Override
    public String getCfMinimumBackEndLoadCurrency() {
        return cfMinimumBackEndLoadCurrency;
    }

    public void setCfMinimumBackEndLoadCurrency(String cfMinimumBackEndLoadCurrency) {
        this.cfMinimumBackEndLoadCurrency = cfMinimumBackEndLoadCurrency;
    }

    @Override
    public String getCfMinimumBackEndLoadPercentSign() {
        return cfMinimumBackEndLoadPercentSign;
    }

    public void setCfMinimumBackEndLoadPercentSign(String cfMinimumBackEndLoadPercentSign) {
        this.cfMinimumBackEndLoadPercentSign = cfMinimumBackEndLoadPercentSign;
    }

    @Override
    public String getCfMinimumBackEndLoadReferenceValue() {
        return cfMinimumBackEndLoadReferenceValue;
    }

    public void setCfMinimumBackEndLoadReferenceValue(String cfMinimumBackEndLoadReferenceValue) {
        this.cfMinimumBackEndLoadReferenceValue = cfMinimumBackEndLoadReferenceValue;
    }

    @Override
    public BigDecimal getCfMaximumBackEndLoad() {
        return cfMaximumBackEndLoad;
    }

    public void setCfMaximumBackEndLoad(BigDecimal cfMaximumBackEndLoad) {
        this.cfMaximumBackEndLoad = cfMaximumBackEndLoad;
    }

    @Override
    public String getCfMaximumBackEndLoadCurrency() {
        return cfMaximumBackEndLoadCurrency;
    }

    public void setCfMaximumBackEndLoadCurrency(String cfMaximumBackEndLoadCurrency) {
        this.cfMaximumBackEndLoadCurrency = cfMaximumBackEndLoadCurrency;
    }

    @Override
    public String getCfMaximumBackEndLoadPercentSign() {
        return cfMaximumBackEndLoadPercentSign;
    }

    public void setCfMaximumBackEndLoadPercentSign(String cfMaximumBackEndLoadPercentSign) {
        this.cfMaximumBackEndLoadPercentSign = cfMaximumBackEndLoadPercentSign;
    }

    @Override
    public String getCfMaximumBackEndLoadReferenceValue() {
        return cfMaximumBackEndLoadReferenceValue;
    }

    public void setCfMaximumBackEndLoadReferenceValue(String cfMaximumBackEndLoadReferenceValue) {
        this.cfMaximumBackEndLoadReferenceValue = cfMaximumBackEndLoadReferenceValue;
    }

    @Override
    public DateTime getCfTotalFundCostsDateFrom() {
        return cfTotalFundCostsDateFrom;
    }

    public void setCfTotalFundCostsDateFrom(DateTime cfTotalFundCostsDateFrom) {
        this.cfTotalFundCostsDateFrom = cfTotalFundCostsDateFrom;
    }

    @Override
    public DateTime getCfTotalFundCostsDateTo() {
        return cfTotalFundCostsDateTo;
    }

    public void setCfTotalFundCostsDateTo(DateTime cfTotalFundCostsDateTo) {
        this.cfTotalFundCostsDateTo = cfTotalFundCostsDateTo;
    }

    @Override
    public BigDecimal getCfTotalFundCostsTransaction() {
        return cfTotalFundCostsTransaction;
    }

    public void setCfTotalFundCostsTransaction(BigDecimal cfTotalFundCostsTransaction) {
        this.cfTotalFundCostsTransaction = cfTotalFundCostsTransaction;
    }

    @Override
    public BigDecimal getCfTotalFundCostsRunning() {
        return cfTotalFundCostsRunning;
    }

    public void setCfTotalFundCostsRunning(BigDecimal cfTotalFundCostsRunning) {
        this.cfTotalFundCostsRunning = cfTotalFundCostsRunning;
    }

    @Override
    public BigDecimal getCfTotalFundCostsEventRelated() {
        return cfTotalFundCostsEventRelated;
    }

    public void setCfTotalFundCostsEventRelated(BigDecimal cfTotalFundCostsEventRelated) {
        this.cfTotalFundCostsEventRelated = cfTotalFundCostsEventRelated;
    }

    @Override
    public String getCfTotalFundCostsCorrectionID() {
        return cfTotalFundCostsCorrectionID;
    }

    public void setCfTotalFundCostsCorrectionID(String cfTotalFundCostsCorrectionID) {
        this.cfTotalFundCostsCorrectionID = cfTotalFundCostsCorrectionID;
    }

    @Override
    public String getCspFairValueInstrument() {
        return cspFairValueInstrument;
    }

    public void setCspFairValueInstrument(String cspFairValueInstrument) {
        this.cspFairValueInstrument = cspFairValueInstrument;
    }

    @Override
    public String getCspInstrumentWithRunningCosts() {
        return cspInstrumentWithRunningCosts;
    }

    public void setCspInstrumentWithRunningCosts(String cspInstrumentWithRunningCosts) {
        this.cspInstrumentWithRunningCosts = cspInstrumentWithRunningCosts;
    }

    @Override
    public BigDecimal getCspEstRunningCostsPrFv() {
        return cspEstRunningCostsPrFv;
    }

    public void setCspEstRunningCostsPrFv(BigDecimal cspEstRunningCostsPrFv) {
        this.cspEstRunningCostsPrFv = cspEstRunningCostsPrFv;
    }

    @Override
    public String getCspEstRunningCostsPrFvCurrency() {
        return cspEstRunningCostsPrFvCurrency;
    }

    public void setCspEstRunningCostsPrFvCurrency(String cspEstRunningCostsPrFvCurrency) {
        this.cspEstRunningCostsPrFvCurrency = cspEstRunningCostsPrFvCurrency;
    }

    @Override
    public String getCspEstRunningCostsPrFvPercentSign() {
        return cspEstRunningCostsPrFvPercentSign;
    }

    public void setCspEstRunningCostsPrFvPercentSign(String cspEstRunningCostsPrFvPercentSign) {
        this.cspEstRunningCostsPrFvPercentSign = cspEstRunningCostsPrFvPercentSign;
    }

    @Override
    public DateTime getCspEstRunningCostsPrFvDate() {
        return cspEstRunningCostsPrFvDate;
    }

    public void setCspEstRunningCostsPrFvDate(DateTime cspEstRunningCostsPrFvDate) {
        this.cspEstRunningCostsPrFvDate = cspEstRunningCostsPrFvDate;
    }

    @Override
    public BigDecimal getCspEntryCostsPrFv() {
        return cspEntryCostsPrFv;
    }

    public void setCspEntryCostsPrFv(BigDecimal cspEntryCostsPrFv) {
        this.cspEntryCostsPrFv = cspEntryCostsPrFv;
    }

    @Override
    public String getCspEntryCostsPrFvCurrency() {
        return cspEntryCostsPrFvCurrency;
    }

    public void setCspEntryCostsPrFvCurrency(String cspEntryCostsPrFvCurrency) {
        this.cspEntryCostsPrFvCurrency = cspEntryCostsPrFvCurrency;
    }

    @Override
    public String getCspEntryCostsPrFvPercentSign() {
        return cspEntryCostsPrFvPercentSign;
    }

    public void setCspEntryCostsPrFvPercentSign(String cspEntryCostsPrFvPercentSign) {
        this.cspEntryCostsPrFvPercentSign = cspEntryCostsPrFvPercentSign;
    }

    @Override
    public DateTime getCspEntryCostsPrFvTime() {
        return cspEntryCostsPrFvTime;
    }

    public void setCspEntryCostsPrFvTime(DateTime cspEntryCostsPrFvTime) {
        this.cspEntryCostsPrFvTime = cspEntryCostsPrFvTime;
    }

    @Override
    public String getCspEntryCostsPrFvCorrectionId() {
        return cspEntryCostsPrFvCorrectionId;
    }

    public void setCspEntryCostsPrFvCorrectionId(String cspEntryCostsPrFvCorrectionId) {
        this.cspEntryCostsPrFvCorrectionId = cspEntryCostsPrFvCorrectionId;
    }

    @Override
    public BigDecimal getCspExitCostsPrFv() {
        return cspExitCostsPrFv;
    }

    public void setCspExitCostsPrFv(BigDecimal cspExitCostsPrFv) {
        this.cspExitCostsPrFv = cspExitCostsPrFv;
    }

    @Override
    public String getCspExitCostsPrFvCurrency() {
        return cspExitCostsPrFvCurrency;
    }

    public void setCspExitCostsPrFvCurrency(String cspExitCostsPrFvCurrency) {
        this.cspExitCostsPrFvCurrency = cspExitCostsPrFvCurrency;
    }

    @Override
    public String getCspExitCostsPrFvPercentSign() {
        return cspExitCostsPrFvPercentSign;
    }

    public void setCspExitCostsPrFvPercentSign(String cspExitCostsPrFvPercentSign) {
        this.cspExitCostsPrFvPercentSign = cspExitCostsPrFvPercentSign;
    }

    @Override
    public DateTime getCspExitCostsPrFvTime() {
        return cspExitCostsPrFvTime;
    }

    public void setCspExitCostsPrFvTime(DateTime cspExitCostsPrFvTime) {
        this.cspExitCostsPrFvTime = cspExitCostsPrFvTime;
    }

    @Override
    public String getCspExitCostsPrFvCorrectionId() {
        return cspExitCostsPrFvCorrectionId;
    }

    public void setCspExitCostsPrFvCorrectionId(String cspExitCostsPrFvCorrectionId) {
        this.cspExitCostsPrFvCorrectionId = cspExitCostsPrFvCorrectionId;
    }

    @Override
    public BigDecimal getCspRunningIncrementalCostsPrFv() {
        return cspRunningIncrementalCostsPrFv;
    }

    public void setCspRunningIncrementalCostsPrFv(BigDecimal cspRunningIncrementalCostsPrFv) {
        this.cspRunningIncrementalCostsPrFv = cspRunningIncrementalCostsPrFv;
    }

    @Override
    public String getCspRunningIncrementalCostsPrFvCurrency() {
        return cspRunningIncrementalCostsPrFvCurrency;
    }

    public void setCspRunningIncrementalCostsPrFvCurrency(String cspRunningIncrementalCostsPrFvCurrency) {
        this.cspRunningIncrementalCostsPrFvCurrency = cspRunningIncrementalCostsPrFvCurrency;
    }

    @Override
    public String getCspRunningIncrementalCostsPrFvPercentSign() {
        return cspRunningIncrementalCostsPrFvPercentSign;
    }

    public void setCspRunningIncrementalCostsPrFvPercentSign(String cspRunningIncrementalCostsPrFvPercentSign) {
        this.cspRunningIncrementalCostsPrFvPercentSign = cspRunningIncrementalCostsPrFvPercentSign;
    }

    @Override
    public DateTime getCspRunningIncrementalCostsPrFvDate() {
        return cspRunningIncrementalCostsPrFvDate;
    }

    public void setCspRunningIncrementalCostsPrFvDate(DateTime cspRunningIncrementalCostsPrFvDate) {
        this.cspRunningIncrementalCostsPrFvDate = cspRunningIncrementalCostsPrFvDate;
    }

    @Override
    public String getCspRunningIncrementalCostsPrFvCorrectionId() {
        return cspRunningIncrementalCostsPrFvCorrectionId;
    }

    public void setCspRunningIncrementalCostsPrFvCorrectionId(String cspRunningIncrementalCostsPrFvCorrectionId) {
        this.cspRunningIncrementalCostsPrFvCorrectionId = cspRunningIncrementalCostsPrFvCorrectionId;
    }

    public void setPriipsID(String priipsID) {
        this.priipsID = priipsID;
    }

    @Override
    public String getPriipsID() {
        return priipsID;
    }

    public void setPriipsText(String priipsText) {
        this.priipsText = priipsText;
    }

    @Override
    public String getPriipsText() {
        return priipsText;
    }

    @Override
    public String toString() {
        return "RegulatoryReportingRecordImpl{" +
                "mifidCapitalClass='" + mifidCapitalClass + '\'' +
                ", mifidAssetClass='" + mifidAssetClass + '\'' +
                ", mifidAssetClassAddition='" + mifidAssetClassAddition + '\'' +
                ", mifidClFiRts28='" + mifidClFiRts28 + '\'' +
                ", mifidLeverageProductIdentifier='" + mifidLeverageProductIdentifier + '\'' +
                ", mifidTickSize='" + mifidTickSize + '\'' +
                ", mifidProductCategory='" + mifidProductCategory + '\'' +
                ", mifidProductApprovalProcessIdentifier='" + mifidProductApprovalProcessIdentifier + '\'' +
                ", tmSourceOfTargetMarketData='" + tmSourceOfTargetMarketData + '\'' +
                ", tmCustomerCategory='" + tmCustomerCategory + '\'' +
                ", tmInvestmentObjectives='" + tmInvestmentObjectives + '\'' +
                ", tmInvestmentHorizon='" + tmInvestmentHorizon + '\'' +
                ", tmRiskIndicator='" + tmRiskIndicator + '\'' +
                ", tmCalculationMethodOfRiskIndicator='" + tmCalculationMethodOfRiskIndicator + '\'' +
                ", tmRiskProfile='" + tmRiskProfile + '\'' +
                ", tmFinancialLossBearingCapacity='" + tmFinancialLossBearingCapacity + '\'' +
                ", tmKnowledgeAndExperience='" + tmKnowledgeAndExperience + '\'' +
                ", tmDistributionStrategy='" + tmDistributionStrategy + '\'' +
                ", tmSpecialRequirements='" + tmSpecialRequirements + '\'' +
                ", tmAddendumToTheSpecialRequirements='" + tmAddendumToTheSpecialRequirements + '\'' +
                ", ntmCustomerCategory='" + ntmCustomerCategory + '\'' +
                ", ntmInvestmentObjectives='" + ntmInvestmentObjectives + '\'' +
                ", ntmInvestmentHorizon='" + ntmInvestmentHorizon + '\'' +
                ", ntmRiskIndicator='" + ntmRiskIndicator + '\'' +
                ", ntmRiskAndYieldProfile='" + ntmRiskAndYieldProfile + '\'' +
                ", ntmFinancialLossBearingCapacity='" + ntmFinancialLossBearingCapacity + '\'' +
                ", ntmKnowledgeAndExperience='" + ntmKnowledgeAndExperience + '\'' +
                ", ntmDistributionStrategy='" + ntmDistributionStrategy + '\'' +
                ", ntmSpecialRequirements='" + ntmSpecialRequirements + '\'' +
                ", cfPerformanceFeeIdentifier='" + cfPerformanceFeeIdentifier + '\'' +
                ", cfSwingPricingIdentifier='" + cfSwingPricingIdentifier + '\'' +
                ", cfRunningFundCostsEst=" + cfRunningFundCostsEst +
                ", cfRunningFundCostsEstDate=" + cfRunningFundCostsEstDate +
                ", cfTransactionCostsFundsEst=" + cfTransactionCostsFundsEst +
                ", cfTransactionCostsFundsEstDate=" + cfTransactionCostsFundsEstDate +
                ", cfEventRelatedCostsFundsEst=" + cfEventRelatedCostsFundsEst +
                ", cfEventRelatedCostsFundsEstDate=" + cfEventRelatedCostsFundsEstDate +
                ", cfActualRedemptionCostsFund=" + cfActualRedemptionCostsFund +
                ", cfActualRedemptionCostsFundDate=" + cfActualRedemptionCostsFundDate +
                ", cfMinimumBackEndLoad=" + cfMinimumBackEndLoad +
                ", cfMinimumBackEndLoadCurrency='" + cfMinimumBackEndLoadCurrency + '\'' +
                ", cfMinimumBackEndLoadPercentSign='" + cfMinimumBackEndLoadPercentSign + '\'' +
                ", cfMinimumBackEndLoadReferenceValue='" + cfMinimumBackEndLoadReferenceValue + '\'' +
                ", cfMaximumBackEndLoad=" + cfMaximumBackEndLoad +
                ", cfMaximumBackEndLoadCurrency='" + cfMaximumBackEndLoadCurrency + '\'' +
                ", cfMaximumBackEndLoadPercentSign='" + cfMaximumBackEndLoadPercentSign + '\'' +
                ", cfMaximumBackEndLoadReferenceValue='" + cfMaximumBackEndLoadReferenceValue + '\'' +
                ", cfTotalFundCostsDateFrom=" + cfTotalFundCostsDateFrom +
                ", cfTotalFundCostsDateTo=" + cfTotalFundCostsDateTo +
                ", cfTotalFundCostsTransaction=" + cfTotalFundCostsTransaction +
                ", cfTotalFundCostsRunning=" + cfTotalFundCostsRunning +
                ", cfTotalFundCostsEventRelated=" + cfTotalFundCostsEventRelated +
                ", cfTotalFundCostsCorrectionID='" + cfTotalFundCostsCorrectionID + '\'' +
                ", cspFairValueInstrument='" + cspFairValueInstrument + '\'' +
                ", cspInstrumentWithRunningCosts='" + cspInstrumentWithRunningCosts + '\'' +
                ", cspEstRunningCostsPrFv=" + cspEstRunningCostsPrFv +
                ", cspEstRunningCostsPrFvCurrency='" + cspEstRunningCostsPrFvCurrency + '\'' +
                ", cspEstRunningCostsPrFvPercentSign='" + cspEstRunningCostsPrFvPercentSign + '\'' +
                ", cspEstRunningCostsPrFvDate=" + cspEstRunningCostsPrFvDate +
                ", cspEntryCostsPrFv=" + cspEntryCostsPrFv +
                ", cspEntryCostsPrFvCurrency='" + cspEntryCostsPrFvCurrency + '\'' +
                ", cspEntryCostsPrFvPercentSign='" + cspEntryCostsPrFvPercentSign + '\'' +
                ", cspEntryCostsPrFvTime=" + cspEntryCostsPrFvTime +
                ", cspEntryCostsPrFvCorrectionId='" + cspEntryCostsPrFvCorrectionId + '\'' +
                ", cspExitCostsPrFv=" + cspExitCostsPrFv +
                ", cspExitCostsPrFvCurrency='" + cspExitCostsPrFvCurrency + '\'' +
                ", cspExitCostsPrFvPercentSign='" + cspExitCostsPrFvPercentSign + '\'' +
                ", cspExitCostsPrFvTime=" + cspExitCostsPrFvTime +
                ", cspExitCostsPrFvCorrectionId='" + cspExitCostsPrFvCorrectionId + '\'' +
                ", cspRunningIncrementalCostsPrFv=" + cspRunningIncrementalCostsPrFv +
                ", cspRunningIncrementalCostsPrFvCurrency='" + cspRunningIncrementalCostsPrFvCurrency + '\'' +
                ", cspRunningIncrementalCostsPrFvPercentSign='" + cspRunningIncrementalCostsPrFvPercentSign + '\'' +
                ", cspRunningIncrementalCostsPrFvDate=" + cspRunningIncrementalCostsPrFvDate +
                ", cspRunningIncrementalCostsPrFvCorrectionId='" + cspRunningIncrementalCostsPrFvCorrectionId + '\'' +
                ", priipsID='" + priipsID + '\'' +
                ", priipsText='" + priipsText + '\'' +
                '}';
    }
}
