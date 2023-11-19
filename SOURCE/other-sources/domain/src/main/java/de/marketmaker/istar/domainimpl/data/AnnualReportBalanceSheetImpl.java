/*
 * AnnualReportDataImpl.java
 *
 * Created on 17.03.2010 15:56:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.data.AnnualReportBalanceSheet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class AnnualReportBalanceSheetImpl implements AnnualReportBalanceSheet, Serializable {
    protected static final long serialVersionUID = 1L;

    public static class Builder {
        private BigDecimal interestIncome;

        private BigDecimal interestExpenses;

        private BigDecimal netInterestIncome;

        private BigDecimal incomeFromStocksAndParticipations;

        private BigDecimal netCommissionIncome;

        private BigDecimal netTradingIncome;

        private BigDecimal grossOperatingIncomeBanking;

        private BigDecimal generalAdministrativeExpenses;

        private BigDecimal turnover;

        private BigDecimal externalCosts;

        private BigDecimal personnelCosts;

        private BigDecimal operationalCosts;

        private BigDecimal brutSalesMargin;

        private BigDecimal depreciationsAndAmortizations;

        private BigDecimal operationalBankResult;

        private BigDecimal provisions;

        private BigDecimal otherRevenuesAndExpensesBanking;

        private BigDecimal resultBankingBeforeTaxes;

        private BigDecimal totalGrossPremiums;

        private BigDecimal technicalResult;

        private BigDecimal investmentActivitiesNotIncludedInTechnicalResult;

        private BigDecimal operationalResultInsurance;

        private BigDecimal otherIncomeAndCostsInsurance;

        private BigDecimal resultInsuranceBeforeTaxes;

        private BigDecimal otherIncomce;

        private BigDecimal totalCurrentResultBeforeTaxes;

        private BigDecimal consolidatedIncomeFromPrivateEquityParticipations;

        private BigDecimal resultFromCurrentOperations;

        private BigDecimal operationalIncome;

        private BigDecimal finacialResult;

        private BigDecimal finacialRevenues;

        private BigDecimal financialExpenditures;

        private BigDecimal otherRevenuesAndExpenditures;

        private BigDecimal companiesUnderEquity;

        private BigDecimal resultBeforeTaxes;

        private BigDecimal taxes;

        private BigDecimal extraordinaryResult;

        private BigDecimal minorityInterests;

        private BigDecimal netIncome;

        private BigDecimal dividendOnPreferredShares;

        private boolean isProvisional;

        public AnnualReportBalanceSheetImpl build() {
            return new AnnualReportBalanceSheetImpl(this);
        }

        public void setInterestIncome(BigDecimal interestIncome) {
            this.interestIncome = interestIncome;
        }

        public void setInterestExpenses(BigDecimal interestExpenses) {
            this.interestExpenses = interestExpenses;
        }

        public void setNetInterestIncome(BigDecimal netInterestIncome) {
            this.netInterestIncome = netInterestIncome;
        }

        public void setIncomeFromStocksAndParticipations(
                BigDecimal incomeFromStocksAndParticipations) {
            this.incomeFromStocksAndParticipations = incomeFromStocksAndParticipations;
        }

        public void setNetCommissionIncome(BigDecimal netCommissionIncome) {
            this.netCommissionIncome = netCommissionIncome;
        }

        public void setNetTradingIncome(BigDecimal netTradingIncome) {
            this.netTradingIncome = netTradingIncome;
        }

        public void setGrossOperatingIncomeBanking(BigDecimal grossOperatingIncomeBanking) {
            this.grossOperatingIncomeBanking = grossOperatingIncomeBanking;
        }

        public void setGeneralAdministrativeExpenses(BigDecimal generalAdministrativeExpenses) {
            this.generalAdministrativeExpenses = generalAdministrativeExpenses;
        }

        public void setTurnover(BigDecimal turnover) {
            this.turnover = turnover;
        }

        public void setExternalCosts(BigDecimal externalCosts) {
            this.externalCosts = externalCosts;
        }

        public void setPersonnelCosts(BigDecimal personnelCosts) {
            this.personnelCosts = personnelCosts;
        }

        public void setOperationalCosts(BigDecimal operationalCosts) {
            this.operationalCosts = operationalCosts;
        }

        public void setBrutSalesMargin(BigDecimal brutSalesMargin) {
            this.brutSalesMargin = brutSalesMargin;
        }

        public void setDepreciationsAndAmortizations(BigDecimal depreciationsAndAmortizations) {
            this.depreciationsAndAmortizations = depreciationsAndAmortizations;
        }

        public void setOperationalBankResult(BigDecimal operationalBankResult) {
            this.operationalBankResult = operationalBankResult;
        }

        public void setProvisions(BigDecimal provisions) {
            this.provisions = provisions;
        }

        public void setOtherRevenuesAndExpensesBanking(BigDecimal otherRevenuesAndExpensesBanking) {
            this.otherRevenuesAndExpensesBanking = otherRevenuesAndExpensesBanking;
        }

        public void setResultBankingBeforeTaxes(BigDecimal resultBankingBeforeTaxes) {
            this.resultBankingBeforeTaxes = resultBankingBeforeTaxes;
        }

        public void setTotalGrossPremiums(BigDecimal totalGrossPremiums) {
            this.totalGrossPremiums = totalGrossPremiums;
        }

        public void setTechnicalResult(BigDecimal technicalResult) {
            this.technicalResult = technicalResult;
        }

        public void setInvestmentActivitiesNotIncludedInTechnicalResult(
                BigDecimal investmentActivitiesNotIncludedInTechnicalResult) {
            this.investmentActivitiesNotIncludedInTechnicalResult = investmentActivitiesNotIncludedInTechnicalResult;
        }

        public void setOperationalResultInsurance(BigDecimal operationalResultInsurance) {
            this.operationalResultInsurance = operationalResultInsurance;
        }

        public void setOtherIncomeAndCostsInsurance(BigDecimal otherIncomeAndCostsInsurance) {
            this.otherIncomeAndCostsInsurance = otherIncomeAndCostsInsurance;
        }

        public void setResultInsuranceBeforeTaxes(BigDecimal resultInsuranceBeforeTaxes) {
            this.resultInsuranceBeforeTaxes = resultInsuranceBeforeTaxes;
        }

        public void setOtherIncomce(BigDecimal otherIncomce) {
            this.otherIncomce = otherIncomce;
        }

        public void setTotalCurrentResultBeforeTaxes(BigDecimal totalCurrentResultBeforeTaxes) {
            this.totalCurrentResultBeforeTaxes = totalCurrentResultBeforeTaxes;
        }

        public void setConsolidatedIncomeFromPrivateEquityParticipations(
                BigDecimal consolidatedIncomeFromPrivateEquityParticipations) {
            this.consolidatedIncomeFromPrivateEquityParticipations = consolidatedIncomeFromPrivateEquityParticipations;
        }

        public void setResultFromCurrentOperations(BigDecimal resultFromCurrentOperations) {
            this.resultFromCurrentOperations = resultFromCurrentOperations;
        }

        public void setOperationalIncome(BigDecimal operationalIncome) {
            this.operationalIncome = operationalIncome;
        }

        public void setFinacialResult(BigDecimal finacialResult) {
            this.finacialResult = finacialResult;
        }

        public void setFinacialRevenues(BigDecimal finacialRevenues) {
            this.finacialRevenues = finacialRevenues;
        }

        public void setFinancialExpenditures(BigDecimal financialExpenditures) {
            this.financialExpenditures = financialExpenditures;
        }

        public void setOtherRevenuesAndExpenditures(BigDecimal otherRevenuesAndExpenditures) {
            this.otherRevenuesAndExpenditures = otherRevenuesAndExpenditures;
        }

        public void setCompaniesUnderEquity(BigDecimal companiesUnderEquity) {
            this.companiesUnderEquity = companiesUnderEquity;
        }

        public void setResultBeforeTaxes(BigDecimal resultBeforeTaxes) {
            this.resultBeforeTaxes = resultBeforeTaxes;
        }

        public void setTaxes(BigDecimal taxes) {
            this.taxes = taxes;
        }

        public void setExtraordinaryResult(BigDecimal extraordinaryResult) {
            this.extraordinaryResult = extraordinaryResult;
        }

        public void setMinorityInterests(BigDecimal minorityInterests) {
            this.minorityInterests = minorityInterests;
        }

        public void setNetIncome(BigDecimal netIncome) {
            this.netIncome = netIncome;
        }

        public void setDividendOnPreferredShares(BigDecimal dividendOnPreferredShares) {
            this.dividendOnPreferredShares = dividendOnPreferredShares;
        }

        public void setIsProvisional(boolean provisional) {
            isProvisional = provisional;
        }
    }

    private final BigDecimal interestIncome;

    private final BigDecimal interestExpenses;

    private final BigDecimal netInterestIncome;

    private final BigDecimal incomeFromStocksAndParticipations;

    private final BigDecimal netCommissionIncome;

    private final BigDecimal netTradingIncome;

    private final BigDecimal grossOperatingIncomeBanking;

    private final BigDecimal generalAdministrativeExpenses;

    private final BigDecimal turnover;

    private final BigDecimal externalCosts;

    private final BigDecimal personnelCosts;

    private final BigDecimal operationalCosts;

    private final BigDecimal brutSalesMargin;

    private final BigDecimal depreciationsAndAmortizations;

    private final BigDecimal operationalBankResult;

    private final BigDecimal provisions;

    private final BigDecimal otherRevenuesAndExpensesBanking;

    private final BigDecimal resultBankingBeforeTaxes;

    private final BigDecimal totalGrossPremiums;

    private final BigDecimal technicalResult;

    private final BigDecimal investmentActivitiesNotIncludedInTechnicalResult;

    private final BigDecimal operationalResultInsurance;

    private final BigDecimal otherIncomeAndCostsInsurance;

    private final BigDecimal resultInsuranceBeforeTaxes;

    private final BigDecimal otherIncomce;

    private final BigDecimal totalCurrentResultBeforeTaxes;

    private final BigDecimal consolidatedIncomeFromPrivateEquityParticipations;

    private final BigDecimal resultFromCurrentOperations;

    private final BigDecimal operationalIncome;

    private final BigDecimal finacialResult;

    private final BigDecimal finacialRevenues;

    private final BigDecimal financialExpenditures;

    private final BigDecimal otherRevenuesAndExpenditures;

    private final BigDecimal companiesUnderEquity;

    private final BigDecimal resultBeforeTaxes;

    private final BigDecimal taxes;

    private final BigDecimal extraordinaryResult;

    private final BigDecimal minorityInterests;

    private final BigDecimal netIncome;

    private final BigDecimal dividendOnPreferredShares;

    private final boolean isProvisional;

    public AnnualReportBalanceSheetImpl(Builder b) {
        this.interestIncome = b.interestIncome;
        this.interestExpenses = b.interestExpenses;
        this.netInterestIncome = b.netInterestIncome;
        this.incomeFromStocksAndParticipations = b.incomeFromStocksAndParticipations;
        this.netCommissionIncome = b.netCommissionIncome;
        this.netTradingIncome = b.netTradingIncome;
        this.grossOperatingIncomeBanking = b.grossOperatingIncomeBanking;
        this.generalAdministrativeExpenses = b.generalAdministrativeExpenses;
        this.turnover = b.turnover;
        this.externalCosts = b.externalCosts;
        this.personnelCosts = b.personnelCosts;
        this.operationalCosts = b.operationalCosts;
        this.brutSalesMargin = b.brutSalesMargin;
        this.depreciationsAndAmortizations = b.depreciationsAndAmortizations;
        this.operationalBankResult = b.operationalBankResult;
        this.provisions = b.provisions;
        this.otherRevenuesAndExpensesBanking = b.otherRevenuesAndExpensesBanking;
        this.resultBankingBeforeTaxes = b.resultBankingBeforeTaxes;
        this.totalGrossPremiums = b.totalGrossPremiums;
        this.technicalResult = b.technicalResult;
        this.investmentActivitiesNotIncludedInTechnicalResult = b.investmentActivitiesNotIncludedInTechnicalResult;
        this.operationalResultInsurance = b.operationalResultInsurance;
        this.otherIncomeAndCostsInsurance = b.otherIncomeAndCostsInsurance;
        this.resultInsuranceBeforeTaxes = b.resultInsuranceBeforeTaxes;
        this.otherIncomce = b.otherIncomce;
        this.totalCurrentResultBeforeTaxes = b.totalCurrentResultBeforeTaxes;
        this.consolidatedIncomeFromPrivateEquityParticipations = b.consolidatedIncomeFromPrivateEquityParticipations;
        this.resultFromCurrentOperations = b.resultFromCurrentOperations;
        this.operationalIncome = b.operationalIncome;
        this.finacialResult = b.finacialResult;
        this.finacialRevenues = b.finacialRevenues;
        this.financialExpenditures = b.financialExpenditures;
        this.otherRevenuesAndExpenditures = b.otherRevenuesAndExpenditures;
        this.companiesUnderEquity = b.companiesUnderEquity;
        this.resultBeforeTaxes = b.resultBeforeTaxes;
        this.taxes = b.taxes;
        this.extraordinaryResult = b.extraordinaryResult;
        this.minorityInterests = b.minorityInterests;
        this.netIncome = b.netIncome;
        this.dividendOnPreferredShares = b.dividendOnPreferredShares;
        this.isProvisional = b.isProvisional;
    }

    public BigDecimal getInterestIncome() {
        return interestIncome;
    }

    public BigDecimal getInterestExpenses() {
        return interestExpenses;
    }

    public BigDecimal getNetInterestIncome() {
        return netInterestIncome;
    }

    public BigDecimal getIncomeFromStocksAndParticipations() {
        return incomeFromStocksAndParticipations;
    }

    public BigDecimal getNetCommissionIncome() {
        return netCommissionIncome;
    }

    public BigDecimal getNetTradingIncome() {
        return netTradingIncome;
    }

    public BigDecimal getGrossOperatingIncomeBanking() {
        return grossOperatingIncomeBanking;
    }

    public BigDecimal getGeneralAdministrativeExpenses() {
        return generalAdministrativeExpenses;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public BigDecimal getExternalCosts() {
        return externalCosts;
    }

    public BigDecimal getPersonnelCosts() {
        return personnelCosts;
    }

    public BigDecimal getOperationalCosts() {
        return operationalCosts;
    }

    public BigDecimal getBrutSalesMargin() {
        return brutSalesMargin;
    }

    public BigDecimal getDepreciationsAndAmortizations() {
        return depreciationsAndAmortizations;
    }

    public BigDecimal getOperationalBankResult() {
        return operationalBankResult;
    }

    public BigDecimal getProvisions() {
        return provisions;
    }

    public BigDecimal getOtherRevenuesAndExpensesBanking() {
        return otherRevenuesAndExpensesBanking;
    }

    public BigDecimal getResultBankingBeforeTaxes() {
        return resultBankingBeforeTaxes;
    }

    public BigDecimal getTotalGrossPremiums() {
        return totalGrossPremiums;
    }

    public BigDecimal getTechnicalResult() {
        return technicalResult;
    }

    public BigDecimal getInvestmentActivitiesNotIncludedInTechnicalResult() {
        return investmentActivitiesNotIncludedInTechnicalResult;
    }

    public BigDecimal getOperationalResultInsurance() {
        return operationalResultInsurance;
    }

    public BigDecimal getOtherIncomeAndCostsInsurance() {
        return otherIncomeAndCostsInsurance;
    }

    public BigDecimal getResultInsuranceBeforeTaxes() {
        return resultInsuranceBeforeTaxes;
    }

    public BigDecimal getOtherIncomce() {
        return otherIncomce;
    }

    public BigDecimal getTotalCurrentResultBeforeTaxes() {
        return totalCurrentResultBeforeTaxes;
    }

    public BigDecimal getConsolidatedIncomeFromPrivateEquityParticipations() {
        return consolidatedIncomeFromPrivateEquityParticipations;
    }

    public BigDecimal getResultFromCurrentOperations() {
        return resultFromCurrentOperations;
    }

    public BigDecimal getOperationalIncome() {
        return operationalIncome;
    }

    public BigDecimal getFinacialResult() {
        return finacialResult;
    }

    public BigDecimal getFinacialRevenues() {
        return finacialRevenues;
    }

    public BigDecimal getFinancialExpenditures() {
        return financialExpenditures;
    }

    public BigDecimal getOtherRevenuesAndExpenditures() {
        return otherRevenuesAndExpenditures;
    }

    public BigDecimal getCompaniesUnderEquity() {
        return companiesUnderEquity;
    }

    public BigDecimal getResultBeforeTaxes() {
        return resultBeforeTaxes;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public BigDecimal getExtraordinaryResult() {
        return extraordinaryResult;
    }

    public BigDecimal getMinorityInterests() {
        return minorityInterests;
    }

    public BigDecimal getNetIncome() {
        return netIncome;
    }

    public BigDecimal getDividendOnPreferredShares() {
        return dividendOnPreferredShares;
    }

    public boolean isProvisional() {
        return isProvisional;
    }

    @Override
    public String toString() {
        return "AnnualReportBalanceSheetImpl{" +
                "interestIncome=" + interestIncome +
                ", interestExpenses=" + interestExpenses +
                ", netInterestIncome=" + netInterestIncome +
                ", incomeFromStocksAndParticipations=" + incomeFromStocksAndParticipations +
                ", netCommissionIncome=" + netCommissionIncome +
                ", netTradingIncome=" + netTradingIncome +
                ", grossOperatingIncomeBanking=" + grossOperatingIncomeBanking +
                ", generalAdministrativeExpenses=" + generalAdministrativeExpenses +
                ", turnover=" + turnover +
                ", externalCosts=" + externalCosts +
                ", personnelCosts=" + personnelCosts +
                ", operationalCosts=" + operationalCosts +
                ", brutSalesMargin=" + brutSalesMargin +
                ", depreciationsAndAmortizations=" + depreciationsAndAmortizations +
                ", operationalBankResult=" + operationalBankResult +
                ", provisions=" + provisions +
                ", otherRevenuesAndExpensesBanking=" + otherRevenuesAndExpensesBanking +
                ", resultBankingBeforeTaxes=" + resultBankingBeforeTaxes +
                ", totalGrossPremiums=" + totalGrossPremiums +
                ", technicalResult=" + technicalResult +
                ", investmentActivitiesNotIncludedInTechnicalResult=" + investmentActivitiesNotIncludedInTechnicalResult +
                ", operationalResultInsurance=" + operationalResultInsurance +
                ", otherIncomeAndCostsInsurance=" + otherIncomeAndCostsInsurance +
                ", resultInsuranceBeforeTaxes=" + resultInsuranceBeforeTaxes +
                ", otherIncomce=" + otherIncomce +
                ", totalCurrentResultBeforeTaxes=" + totalCurrentResultBeforeTaxes +
                ", consolidatedIncomeFromPrivateEquityParticipations=" + consolidatedIncomeFromPrivateEquityParticipations +
                ", resultFromCurrentOperations=" + resultFromCurrentOperations +
                ", operationalIncome=" + operationalIncome +
                ", finacialResult=" + finacialResult +
                ", finacialRevenues=" + finacialRevenues +
                ", financialExpenditures=" + financialExpenditures +
                ", otherRevenuesAndExpenditures=" + otherRevenuesAndExpenditures +
                ", companiesUnderEquity=" + companiesUnderEquity +
                ", resultBeforeTaxes=" + resultBeforeTaxes +
                ", taxes=" + taxes +
                ", extraordinaryResult=" + extraordinaryResult +
                ", minorityInterests=" + minorityInterests +
                ", netIncome=" + netIncome +
                ", dividendOnPreferredShares=" + dividendOnPreferredShares +
                ", isProvisional=" + isProvisional +
                '}';
    }
}