package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import org.joda.time.DateTime;

public class NullRegulatoryReportingRecord implements RegulatoryReportingRecord {

  public final static RegulatoryReportingRecord INSTANCE = new NullRegulatoryReportingRecord();

  private NullRegulatoryReportingRecord() {
  }

  @Override
  public String getMifidCapitalClass() {
    return null;
  }

  @Override
  public String getMifidAssetClass() {
    return null;
  }

  @Override
  public String getMifidAssetClassAddition() {
    return null;
  }

  @Override
  public String getMifidClFiRts28() {
    return null;
  }

  @Override
  public String getMifidLeverageProductIdentifier() {
    return null;
  }

  @Override
  public String getMifidTickSize() {
    return null;
  }

  @Override
  public String getMifidProductCategory() {
    return null;
  }

  @Override
  public String getMifidProductApprovalProcessIdentifier() {
    return null;
  }

  @Override
  public String getTmSourceOfTargetMarketData() {
    return null;
  }

  @Override
  public String getTmCustomerCategory() {
    return null;
  }

  @Override
  public String getTmInvestmentObjectives() {
    return null;
  }

  @Override
  public String getTmInvestmentHorizon() {
    return null;
  }

  @Override
  public String getTmRiskIndicator() {
    return null;
  }

  @Override
  public String getTmCalculationMethodOfRiskIndicator() {
    return null;
  }

  @Override
  public String getTmRiskProfile() {
    return null;
  }

  @Override
  public String getTmFinancialLossBearingCapacity() {
    return null;
  }

  @Override
  public String getTmKnowledgeAndExperience() {
    return null;
  }

  @Override
  public String getTmDistributionStrategy() {
    return null;
  }

  @Override
  public String getTmSpecialRequirements() {
    return null;
  }

  @Override
  public String getTmAddendumToTheSpecialRequirements() {
    return null;
  }

  @Override
  public String getNtmCustomerCategory() {
    return null;
  }

  @Override
  public String getNtmInvestmentObjectives() {
    return null;
  }

  @Override
  public String getNtmInvestmentHorizon() {
    return null;
  }

  @Override
  public String getNtmRiskIndicator() {
    return null;
  }

  @Override
  public String getNtmRiskAndYieldProfile() {
    return null;
  }

  @Override
  public String getNtmFinancialLossBearingCapacity() {
    return null;
  }

  @Override
  public String getNtmKnowledgeAndExperience() {
    return null;
  }

  @Override
  public String getNtmDistributionStrategy() {
    return null;
  }

  @Override
  public String getNtmSpecialRequirements() {
    return null;
  }

  @Override
  public String getCfPerformanceFeeIdentifier() {
    return null;
  }

  @Override
  public String getCfSwingPricingIdentifier() {
    return null;
  }

  @Override
  public BigDecimal getCfRunningFundCostsEst() {
    return null;
  }

  @Override
  public DateTime getCfRunningFundCostsEstDate() {
    return null;
  }

  @Override
  public BigDecimal getCfTransactionCostsFundsEst() {
    return null;
  }

  @Override
  public DateTime getCfTransactionCostsFundsEstDate() {
    return null;
  }

  @Override
  public BigDecimal getCfEventRelatedCostsFundsEst() {
    return null;
  }

  @Override
  public DateTime getCfEventRelatedCostsFundsEstDate() {
    return null;
  }

  @Override
  public BigDecimal getCfActualRedemptionCostsFund() {
    return null;
  }

  @Override
  public DateTime getCfActualRedemptionCostsFundDate() {
    return null;
  }

  @Override
  public BigDecimal getCfMinimumBackEndLoad() {
    return null;
  }

  @Override
  public String getCfMinimumBackEndLoadCurrency() {
    return null;
  }

  @Override
  public String getCfMinimumBackEndLoadPercentSign() {
    return null;
  }

  @Override
  public String getCfMinimumBackEndLoadReferenceValue() {
    return null;
  }

  @Override
  public BigDecimal getCfMaximumBackEndLoad() {
    return null;
  }

  @Override
  public String getCfMaximumBackEndLoadCurrency() {
    return null;
  }

  @Override
  public String getCfMaximumBackEndLoadPercentSign() {
    return null;
  }

  @Override
  public String getCfMaximumBackEndLoadReferenceValue() {
    return null;
  }

  @Override
  public DateTime getCfTotalFundCostsDateFrom() {
    return null;
  }

  @Override
  public DateTime getCfTotalFundCostsDateTo() {
    return null;
  }

  @Override
  public BigDecimal getCfTotalFundCostsTransaction() {
    return null;
  }

  @Override
  public BigDecimal getCfTotalFundCostsRunning() {
    return null;
  }

  @Override
  public BigDecimal getCfTotalFundCostsEventRelated() {
    return null;
  }

  @Override
  public String getCfTotalFundCostsCorrectionID() {
    return null;
  }

  @Override
  public String getCspFairValueInstrument() {
    return null;
  }

  @Override
  public String getCspInstrumentWithRunningCosts() {
    return null;
  }

  @Override
  public BigDecimal getCspEstRunningCostsPrFv() {
    return null;
  }

  @Override
  public String getCspEstRunningCostsPrFvCurrency() {
    return null;
  }

  @Override
  public String getCspEstRunningCostsPrFvPercentSign() {
    return null;
  }

  @Override
  public DateTime getCspEstRunningCostsPrFvDate() {
    return null;
  }

  @Override
  public BigDecimal getCspEntryCostsPrFv() {
    return null;
  }

  @Override
  public String getCspEntryCostsPrFvCurrency() {
    return null;
  }

  @Override
  public String getCspEntryCostsPrFvPercentSign() {
    return null;
  }

  @Override
  public DateTime getCspEntryCostsPrFvTime() {
    return null;
  }

  @Override
  public String getCspEntryCostsPrFvCorrectionId() {
    return null;
  }

  @Override
  public BigDecimal getCspExitCostsPrFv() {
    return null;
  }

  @Override
  public String getCspExitCostsPrFvCurrency() {
    return null;
  }

  @Override
  public String getCspExitCostsPrFvPercentSign() {
    return null;
  }

  @Override
  public DateTime getCspExitCostsPrFvTime() {
    return null;
  }

  @Override
  public String getCspExitCostsPrFvCorrectionId() {
    return null;
  }

  @Override
  public BigDecimal getCspRunningIncrementalCostsPrFv() {
    return null;
  }

  @Override
  public String getCspRunningIncrementalCostsPrFvCurrency() {
    return null;
  }

  @Override
  public String getCspRunningIncrementalCostsPrFvPercentSign() {
    return null;
  }

  @Override
  public DateTime getCspRunningIncrementalCostsPrFvDate() {
    return null;
  }

  @Override
  public String getCspRunningIncrementalCostsPrFvCorrectionId() {
    return null;
  }

  @Override
  public String getPriipsID() {
    return null;
  }

  @Override
  public String getPriipsText() {
    return null;
  }
}
