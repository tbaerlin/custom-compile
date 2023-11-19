/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import org.joda.time.DateTime;

/**
 * Regulatory Reporting Data provided by the MDP / Core Databases (e.g. MiFID II)
 */
public interface RegulatoryReportingRecord {

    String getMifidCapitalClass();

    String getMifidAssetClass();

    String getMifidAssetClassAddition();

    String getMifidClFiRts28();

    String getMifidLeverageProductIdentifier();

    String getMifidTickSize();

    String getMifidProductCategory();

    String getMifidProductApprovalProcessIdentifier();

    String getTmSourceOfTargetMarketData();

    String getTmCustomerCategory();

    String getTmInvestmentObjectives();

    String getTmInvestmentHorizon();

    String getTmRiskIndicator();

    String getTmCalculationMethodOfRiskIndicator();

    String getTmRiskProfile();

    String getTmFinancialLossBearingCapacity();

    String getTmKnowledgeAndExperience();

    String getTmDistributionStrategy();

    String getTmSpecialRequirements();

    String getTmAddendumToTheSpecialRequirements();

    String getNtmCustomerCategory();

    String getNtmInvestmentObjectives();

    String getNtmInvestmentHorizon();

    String getNtmRiskIndicator();

    String getNtmRiskAndYieldProfile();

    String getNtmFinancialLossBearingCapacity();

    String getNtmKnowledgeAndExperience();

    String getNtmDistributionStrategy();

    String getNtmSpecialRequirements();

    String getCfPerformanceFeeIdentifier();

    String getCfSwingPricingIdentifier();

    BigDecimal getCfRunningFundCostsEst();

    DateTime getCfRunningFundCostsEstDate();

    BigDecimal getCfTransactionCostsFundsEst();

    DateTime getCfTransactionCostsFundsEstDate();

    BigDecimal getCfEventRelatedCostsFundsEst();

    DateTime getCfEventRelatedCostsFundsEstDate();

    BigDecimal getCfActualRedemptionCostsFund();

    DateTime getCfActualRedemptionCostsFundDate();

    BigDecimal getCfMinimumBackEndLoad();

    String getCfMinimumBackEndLoadCurrency();

    String getCfMinimumBackEndLoadPercentSign();

    String getCfMinimumBackEndLoadReferenceValue();

    BigDecimal getCfMaximumBackEndLoad();

    String getCfMaximumBackEndLoadCurrency();

    String getCfMaximumBackEndLoadPercentSign();

    String getCfMaximumBackEndLoadReferenceValue();

    DateTime getCfTotalFundCostsDateFrom();

    DateTime getCfTotalFundCostsDateTo();

    BigDecimal getCfTotalFundCostsTransaction();

    BigDecimal getCfTotalFundCostsRunning();

    BigDecimal getCfTotalFundCostsEventRelated();

    String getCfTotalFundCostsCorrectionID();

    String getCspFairValueInstrument();

    String getCspInstrumentWithRunningCosts();

    BigDecimal getCspEstRunningCostsPrFv();

    String getCspEstRunningCostsPrFvCurrency();

    String getCspEstRunningCostsPrFvPercentSign();

    DateTime getCspEstRunningCostsPrFvDate();

    BigDecimal getCspEntryCostsPrFv();

    String getCspEntryCostsPrFvCurrency();

    String getCspEntryCostsPrFvPercentSign();

    DateTime getCspEntryCostsPrFvTime();

    String getCspEntryCostsPrFvCorrectionId();

    BigDecimal getCspExitCostsPrFv();

    String getCspExitCostsPrFvCurrency();

    String getCspExitCostsPrFvPercentSign();

    DateTime getCspExitCostsPrFvTime();

    String getCspExitCostsPrFvCorrectionId();

    BigDecimal getCspRunningIncrementalCostsPrFv();

    String getCspRunningIncrementalCostsPrFvCurrency();

    String getCspRunningIncrementalCostsPrFvPercentSign();

    DateTime getCspRunningIncrementalCostsPrFvDate();

    String getCspRunningIncrementalCostsPrFvCorrectionId();

    String getPriipsID();

    String getPriipsText();
}
