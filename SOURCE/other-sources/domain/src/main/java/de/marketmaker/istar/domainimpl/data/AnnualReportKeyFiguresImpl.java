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

import de.marketmaker.istar.domain.data.AnnualReportKeyFigures;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class AnnualReportKeyFiguresImpl implements AnnualReportKeyFigures, Serializable {
    protected static final long serialVersionUID = 1L;

    public static class Builder {
        private BigDecimal currentResultBankingBeforeTaxes;

        private BigDecimal currentResultInsuranceBeforeTaxes;

        private BigDecimal totalGrossPremiums;

        private BigDecimal turnover;

        private BigDecimal netIncome;

        private BigDecimal cashflow;

        private BigDecimal interestToGrossBankResultRatio;

        private BigDecimal provisionsToGrossBankResultRatio;

        private BigDecimal netTradingIncomeToGrossBankResultRatio;

        private BigDecimal resultBankingToGrossBankResultRatio;

        private BigDecimal resultInsuranceToTotalResultBeforeTaxesRatio;

        private BigDecimal returnOnEquity;

        private BigDecimal returnOnAssets;

        private BigDecimal grossMarginRelative;

        private BigDecimal netMarginRelative;

        private BigDecimal profitMarginRelative;

        private BigDecimal currentRatio;

        private BigDecimal quickRatio;

        private BigDecimal netCashPosition;

        private BigDecimal longTermSolvabilityRelative;

        private BigDecimal interestCoverage;

        private BigDecimal daysReceivableRatio;

        private BigDecimal daysInventoryRatio;

        private BigDecimal technicalResultToPremiumIncomeRatio;

        private BigDecimal netIncomeToPremiumIncomeRatio;

        private BigDecimal liableAssetsToTotalBalanceSheetRatio;

        private BigDecimal bisCapital;

        private BigDecimal tier1Ratio;

        private BigDecimal tier2Ratio;

        private BigDecimal shareholdersEquityToPremiumIncomeRatio;

        private BigDecimal numberOfEmployees;

        private BigDecimal turnoverPerEmployee;

        private BigDecimal expensesPerEmployee;

        private BigDecimal resultBeforeTaxesToEmployeeCostsInThousands;

        private BigDecimal numberOfSharesInThousands;

        private BigDecimal netIncomcePerShare;

        private BigDecimal dividendPerShare;

        private BigDecimal dividendReturn;

        private BigDecimal cashflowPerShare;

        private BigDecimal intrinsicValuePerShare;

        private BigDecimal payoutRatio;

        private BigDecimal priceEarningRatio;

        private BigDecimal priceBookValue;

        private BigDecimal financialYearEnd;

        private BigDecimal currentProfitPerShare;

        private BigDecimal currentCashflowPerShare;

        private BigDecimal averageNumberOfShares;

        private boolean isProvisional;

        public AnnualReportKeyFiguresImpl build() {
            return new AnnualReportKeyFiguresImpl(this);
        }

        public void setCurrentResultBankingBeforeTaxes(BigDecimal currentResultBankingBeforeTaxes) {
            this.currentResultBankingBeforeTaxes = currentResultBankingBeforeTaxes;
        }

        public void setCurrentResultInsuranceBeforeTaxes(
                BigDecimal currentResultInsuranceBeforeTaxes) {
            this.currentResultInsuranceBeforeTaxes = currentResultInsuranceBeforeTaxes;
        }

        public void setTotalGrossPremiums(BigDecimal totalGrossPremiums) {
            this.totalGrossPremiums = totalGrossPremiums;
        }

        public void setTurnover(BigDecimal turnover) {
            this.turnover = turnover;
        }

        public void setNetIncome(BigDecimal netIncome) {
            this.netIncome = netIncome;
        }

        public void setCashflow(BigDecimal cashflow) {
            this.cashflow = cashflow;
        }

        public void setInterestToGrossBankResultRatio(BigDecimal interestToGrossBankResultRatio) {
            this.interestToGrossBankResultRatio = interestToGrossBankResultRatio;
        }

        public void setProvisionsToGrossBankResultRatio(
                BigDecimal provisionsToGrossBankResultRatio) {
            this.provisionsToGrossBankResultRatio = provisionsToGrossBankResultRatio;
        }

        public void setNetTradingIncomeToGrossBankResultRatio(
                BigDecimal netTradingIncomeToGrossBankResultRatio) {
            this.netTradingIncomeToGrossBankResultRatio = netTradingIncomeToGrossBankResultRatio;
        }

        public void setResultBankingToGrossBankResultRatio(
                BigDecimal resultBankingToGrossBankResultRatio) {
            this.resultBankingToGrossBankResultRatio = resultBankingToGrossBankResultRatio;
        }

        public void setResultInsuranceToTotalResultBeforeTaxesRatio(
                BigDecimal resultInsuranceToTotalResultBeforeTaxesRatio) {
            this.resultInsuranceToTotalResultBeforeTaxesRatio = resultInsuranceToTotalResultBeforeTaxesRatio;
        }

        public void setReturnOnEquity(BigDecimal returnOnEquity) {
            this.returnOnEquity = returnOnEquity;
        }

        public void setReturnOnAssets(BigDecimal returnOnAssets) {
            this.returnOnAssets = returnOnAssets;
        }

        public void setGrossMarginRelative(BigDecimal grossMarginRelative) {
            this.grossMarginRelative = grossMarginRelative;
        }

        public void setNetMarginRelative(BigDecimal netMarginRelative) {
            this.netMarginRelative = netMarginRelative;
        }

        public void setProfitMarginRelative(BigDecimal profitMarginRelative) {
            this.profitMarginRelative = profitMarginRelative;
        }

        public void setCurrentRatio(BigDecimal currentRatio) {
            this.currentRatio = currentRatio;
        }

        public void setQuickRatio(BigDecimal quickRatio) {
            this.quickRatio = quickRatio;
        }

        public void setNetCashPosition(BigDecimal netCashPosition) {
            this.netCashPosition = netCashPosition;
        }

        public void setLongTermSolvabilityRelative(BigDecimal longTermSolvabilityRelative) {
            this.longTermSolvabilityRelative = longTermSolvabilityRelative;
        }

        public void setInterestCoverage(BigDecimal interestCoverage) {
            this.interestCoverage = interestCoverage;
        }

        public void setDaysReceivableRatio(BigDecimal daysReceivableRatio) {
            this.daysReceivableRatio = daysReceivableRatio;
        }

        public void setDaysInventoryRatio(BigDecimal daysInventoryRatio) {
            this.daysInventoryRatio = daysInventoryRatio;
        }

        public void setTechnicalResultToPremiumIncomeRatio(
                BigDecimal technicalResultToPremiumIncomeRatio) {
            this.technicalResultToPremiumIncomeRatio = technicalResultToPremiumIncomeRatio;
        }

        public void setNetIncomeToPremiumIncomeRatio(BigDecimal netIncomeToPremiumIncomeRatio) {
            this.netIncomeToPremiumIncomeRatio = netIncomeToPremiumIncomeRatio;
        }

        public void setLiableAssetsToTotalBalanceSheetRatio(
                BigDecimal liableAssetsToTotalBalanceSheetRatio) {
            this.liableAssetsToTotalBalanceSheetRatio = liableAssetsToTotalBalanceSheetRatio;
        }

        public void setBisCapital(BigDecimal bisCapital) {
            this.bisCapital = bisCapital;
        }

        public void setTier1Ratio(BigDecimal tier1Ratio) {
            this.tier1Ratio = tier1Ratio;
        }

        public void setTier2Ratio(BigDecimal tier2Ratio) {
            this.tier2Ratio = tier2Ratio;
        }

        public void setShareholdersEquityToPremiumIncomeRatio(
                BigDecimal shareholdersEquityToPremiumIncomeRatio) {
            this.shareholdersEquityToPremiumIncomeRatio = shareholdersEquityToPremiumIncomeRatio;
        }

        public void setNumberOfEmployees(BigDecimal numberOfEmployees) {
            this.numberOfEmployees = numberOfEmployees;
        }

        public void setTurnoverPerEmployee(BigDecimal turnoverPerEmployee) {
            this.turnoverPerEmployee = turnoverPerEmployee;
        }

        public void setExpensesPerEmployee(BigDecimal expensesPerEmployee) {
            this.expensesPerEmployee = expensesPerEmployee;
        }

        public void setResultBeforeTaxesToEmployeeCostsInThousands(
                BigDecimal resultBeforeTaxesToEmployeeCostsInThousands) {
            this.resultBeforeTaxesToEmployeeCostsInThousands = resultBeforeTaxesToEmployeeCostsInThousands;
        }

        public void setNumberOfSharesInThousands(BigDecimal numberOfSharesInThousands) {
            this.numberOfSharesInThousands = numberOfSharesInThousands;
        }

        public void setNetIncomcePerShare(BigDecimal netIncomcePerShare) {
            this.netIncomcePerShare = netIncomcePerShare;
        }

        public void setDividendPerShare(BigDecimal dividendPerShare) {
            this.dividendPerShare = dividendPerShare;
        }

        public void setDividendReturn(BigDecimal dividendReturn) {
            this.dividendReturn = dividendReturn;
        }

        public void setCashflowPerShare(BigDecimal cashflowPerShare) {
            this.cashflowPerShare = cashflowPerShare;
        }

        public void setIntrinsicValuePerShare(BigDecimal intrinsicValuePerShare) {
            this.intrinsicValuePerShare = intrinsicValuePerShare;
        }

        public void setPayoutRatio(BigDecimal payoutRatio) {
            this.payoutRatio = payoutRatio;
        }

        public void setPriceEarningRatio(BigDecimal priceEarningRatio) {
            this.priceEarningRatio = priceEarningRatio;
        }

        public void setPriceBookValue(BigDecimal priceBookValue) {
            this.priceBookValue = priceBookValue;
        }

        public void setFinancialYearEnd(BigDecimal financialYearEnd) {
            this.financialYearEnd = financialYearEnd;
        }

        public void setCurrentProfitPerShare(BigDecimal currentProfitPerShare) {
            this.currentProfitPerShare = currentProfitPerShare;
        }

        public void setCurrentCashflowPerShare(BigDecimal currentCashflowPerShare) {
            this.currentCashflowPerShare = currentCashflowPerShare;
        }

        public void setAverageNumberOfShares(BigDecimal averageNumberOfShares) {
            this.averageNumberOfShares = averageNumberOfShares;
        }

        public void setIsProvisional(boolean provisional) {
            isProvisional = provisional;
        }
    }

    private final BigDecimal currentResultBankingBeforeTaxes;

    private final BigDecimal currentResultInsuranceBeforeTaxes;

    private final BigDecimal totalGrossPremiums;

    private final BigDecimal turnover;

    private final BigDecimal netIncome;

    private final BigDecimal cashflow;

    private final BigDecimal interestToGrossBankResultRatio;

    private final BigDecimal provisionsToGrossBankResultRatio;

    private final BigDecimal netTradingIncomeToGrossBankResultRatio;

    private final BigDecimal resultBankingToGrossBankResultRatio;

    private final BigDecimal resultInsuranceToTotalResultBeforeTaxesRatio;

    private final BigDecimal returnOnEquity;

    private final BigDecimal returnOnAssets;

    private final BigDecimal grossMarginRelative;

    private final BigDecimal netMarginRelative;

    private final BigDecimal profitMarginRelative;

    private final BigDecimal currentRatio;

    private final BigDecimal quickRatio;

    private final BigDecimal netCashPosition;

    private final BigDecimal longTermSolvabilityRelative;

    private final BigDecimal interestCoverage;

    private final BigDecimal daysReceivableRatio;

    private final BigDecimal daysInventoryRatio;

    private final BigDecimal technicalResultToPremiumIncomeRatio;

    private final BigDecimal netIncomeToPremiumIncomeRatio;

    private final BigDecimal liableAssetsToTotalBalanceSheetRatio;

    private final BigDecimal bisCapital;

    private final BigDecimal tier1Ratio;

    private final BigDecimal tier2Ratio;

    private final BigDecimal shareholdersEquityToPremiumIncomeRatio;

    private final BigDecimal numberOfEmployees;

    private final BigDecimal turnoverPerEmployee;

    private final BigDecimal expensesPerEmployee;

    private final BigDecimal resultBeforeTaxesToEmployeeCostsInThousands;

    private final BigDecimal numberOfSharesInThousands;

    private final BigDecimal netIncomcePerShare;

    private final BigDecimal dividendPerShare;

    private final BigDecimal dividendReturn;

    private final BigDecimal cashflowPerShare;

    private final BigDecimal intrinsicValuePerShare;

    private final BigDecimal payoutRatio;

    private final BigDecimal priceEarningRatio;

    private final BigDecimal priceBookValue;

    private final BigDecimal financialYearEnd;

    private final BigDecimal currentProfitPerShare;

    private final BigDecimal currentCashflowPerShare;

    private final BigDecimal averageNumberOfShares;

    private final boolean isProvisional;

    public AnnualReportKeyFiguresImpl(Builder b) {
        this.currentResultBankingBeforeTaxes = b.currentResultBankingBeforeTaxes;
        this.currentResultInsuranceBeforeTaxes = b.currentResultInsuranceBeforeTaxes;
        this.totalGrossPremiums = b.totalGrossPremiums;
        this.turnover = b.turnover;
        this.netIncome = b.netIncome;
        this.cashflow = b.cashflow;
        this.interestToGrossBankResultRatio = b.interestToGrossBankResultRatio;
        this.provisionsToGrossBankResultRatio = b.provisionsToGrossBankResultRatio;
        this.netTradingIncomeToGrossBankResultRatio = b.netTradingIncomeToGrossBankResultRatio;
        this.resultBankingToGrossBankResultRatio = b.resultBankingToGrossBankResultRatio;
        this.resultInsuranceToTotalResultBeforeTaxesRatio = b.resultInsuranceToTotalResultBeforeTaxesRatio;
        this.returnOnEquity = b.returnOnEquity;
        this.returnOnAssets = b.returnOnAssets;
        this.grossMarginRelative = b.grossMarginRelative;
        this.netMarginRelative = b.netMarginRelative;
        this.profitMarginRelative = b.profitMarginRelative;
        this.currentRatio = b.currentRatio;
        this.quickRatio = b.quickRatio;
        this.netCashPosition = b.netCashPosition;
        this.longTermSolvabilityRelative = b.longTermSolvabilityRelative;
        this.interestCoverage = b.interestCoverage;
        this.daysReceivableRatio = b.daysReceivableRatio;
        this.daysInventoryRatio = b.daysInventoryRatio;
        this.technicalResultToPremiumIncomeRatio = b.technicalResultToPremiumIncomeRatio;
        this.netIncomeToPremiumIncomeRatio = b.netIncomeToPremiumIncomeRatio;
        this.liableAssetsToTotalBalanceSheetRatio = b.liableAssetsToTotalBalanceSheetRatio;
        this.bisCapital = b.bisCapital;
        this.tier1Ratio = b.tier1Ratio;
        this.tier2Ratio = b.tier2Ratio;
        this.shareholdersEquityToPremiumIncomeRatio = b.shareholdersEquityToPremiumIncomeRatio;
        this.numberOfEmployees = b.numberOfEmployees;
        this.turnoverPerEmployee = b.turnoverPerEmployee;
        this.expensesPerEmployee = b.expensesPerEmployee;
        this.resultBeforeTaxesToEmployeeCostsInThousands = b.resultBeforeTaxesToEmployeeCostsInThousands;
        this.numberOfSharesInThousands = b.numberOfSharesInThousands;
        this.netIncomcePerShare = b.netIncomcePerShare;
        this.dividendPerShare = b.dividendPerShare;
        this.dividendReturn = b.dividendReturn;
        this.cashflowPerShare = b.cashflowPerShare;
        this.intrinsicValuePerShare = b.intrinsicValuePerShare;
        this.payoutRatio = b.payoutRatio;
        this.priceEarningRatio = b.priceEarningRatio;
        this.priceBookValue = b.priceBookValue;
        this.financialYearEnd = b.financialYearEnd;
        this.currentProfitPerShare = b.currentProfitPerShare;
        this.currentCashflowPerShare = b.currentCashflowPerShare;
        this.averageNumberOfShares = b.averageNumberOfShares;
        this.isProvisional = b.isProvisional;
    }

    public BigDecimal getCurrentResultBankingBeforeTaxes() {
        return currentResultBankingBeforeTaxes;
    }

    public BigDecimal getCurrentResultInsuranceBeforeTaxes() {
        return currentResultInsuranceBeforeTaxes;
    }

    public BigDecimal getTotalGrossPremiums() {
        return totalGrossPremiums;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public BigDecimal getNetIncome() {
        return netIncome;
    }

    public BigDecimal getCashflow() {
        return cashflow;
    }

    public BigDecimal getInterestToGrossBankResultRatio() {
        return interestToGrossBankResultRatio;
    }

    public BigDecimal getProvisionsToGrossBankResultRatio() {
        return provisionsToGrossBankResultRatio;
    }

    public BigDecimal getNetTradingIncomeToGrossBankResultRatio() {
        return netTradingIncomeToGrossBankResultRatio;
    }

    public BigDecimal getResultBankingToGrossBankResultRatio() {
        return resultBankingToGrossBankResultRatio;
    }

    public BigDecimal getResultInsuranceToTotalResultBeforeTaxesRatio() {
        return resultInsuranceToTotalResultBeforeTaxesRatio;
    }

    public BigDecimal getReturnOnEquity() {
        return returnOnEquity;
    }

    public BigDecimal getReturnOnAssets() {
        return returnOnAssets;
    }

    public BigDecimal getGrossMarginRelative() {
        return grossMarginRelative;
    }

    public BigDecimal getNetMarginRelative() {
        return netMarginRelative;
    }

    public BigDecimal getProfitMarginRelative() {
        return profitMarginRelative;
    }

    public BigDecimal getCurrentRatio() {
        return currentRatio;
    }

    public BigDecimal getQuickRatio() {
        return quickRatio;
    }

    public BigDecimal getNetCashPosition() {
        return netCashPosition;
    }

    public BigDecimal getLongTermSolvabilityRelative() {
        return longTermSolvabilityRelative;
    }

    public BigDecimal getInterestCoverage() {
        return interestCoverage;
    }

    public BigDecimal getDaysReceivableRatio() {
        return daysReceivableRatio;
    }

    public BigDecimal getDaysInventoryRatio() {
        return daysInventoryRatio;
    }

    public BigDecimal getTechnicalResultToPremiumIncomeRatio() {
        return technicalResultToPremiumIncomeRatio;
    }

    public BigDecimal getNetIncomeToPremiumIncomeRatio() {
        return netIncomeToPremiumIncomeRatio;
    }

    public BigDecimal getLiableAssetsToTotalBalanceSheetRatio() {
        return liableAssetsToTotalBalanceSheetRatio;
    }

    public BigDecimal getBisCapital() {
        return bisCapital;
    }

    public BigDecimal getTier1Ratio() {
        return tier1Ratio;
    }

    public BigDecimal getTier2Ratio() {
        return tier2Ratio;
    }

    public BigDecimal getShareholdersEquityToPremiumIncomeRatio() {
        return shareholdersEquityToPremiumIncomeRatio;
    }

    public BigDecimal getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public BigDecimal getTurnoverPerEmployee() {
        return turnoverPerEmployee;
    }

    public BigDecimal getExpensesPerEmployee() {
        return expensesPerEmployee;
    }

    public BigDecimal getResultBeforeTaxesToEmployeeCostsInThousands() {
        return resultBeforeTaxesToEmployeeCostsInThousands;
    }

    public BigDecimal getNumberOfSharesInThousands() {
        return numberOfSharesInThousands;
    }

    public BigDecimal getNetIncomcePerShare() {
        return netIncomcePerShare;
    }

    public BigDecimal getDividendPerShare() {
        return dividendPerShare;
    }

    public BigDecimal getDividendReturn() {
        return dividendReturn;
    }

    public BigDecimal getCashflowPerShare() {
        return cashflowPerShare;
    }

    public BigDecimal getIntrinsicValuePerShare() {
        return intrinsicValuePerShare;
    }

    public BigDecimal getPayoutRatio() {
        return payoutRatio;
    }

    public BigDecimal getPriceEarningRatio() {
        return priceEarningRatio;
    }

    public BigDecimal getPriceBookValue() {
        return priceBookValue;
    }

    public BigDecimal getFinancialYearEnd() {
        return financialYearEnd;
    }

    public BigDecimal getCurrentProfitPerShare() {
        return currentProfitPerShare;
    }

    public BigDecimal getCurrentCashflowPerShare() {
        return currentCashflowPerShare;
    }

    public BigDecimal getAverageNumberOfShares() {
        return averageNumberOfShares;
    }

    public boolean isProvisional() {
        return isProvisional;
    }

    @Override
    public String toString() {
        return "AnnualReportKeyFiguresImpl{" +
                "currentResultBankingBeforeTaxes=" + currentResultBankingBeforeTaxes +
                ", currentResultInsuranceBeforeTaxes=" + currentResultInsuranceBeforeTaxes +
                ", totalGrossPremiums=" + totalGrossPremiums +
                ", turnover=" + turnover +
                ", netIncome=" + netIncome +
                ", cashflow=" + cashflow +
                ", interestToGrossBankResultRatio=" + interestToGrossBankResultRatio +
                ", provisionsToGrossBankResultRatio=" + provisionsToGrossBankResultRatio +
                ", netTradingIncomeToGrossBankResultRatio=" + netTradingIncomeToGrossBankResultRatio +
                ", resultBankingToGrossBankResultRatio=" + resultBankingToGrossBankResultRatio +
                ", resultInsuranceToTotalResultBeforeTaxesRatio=" + resultInsuranceToTotalResultBeforeTaxesRatio +
                ", returnOnEquity=" + returnOnEquity +
                ", returnOnAssets=" + returnOnAssets +
                ", grossMarginRelative=" + grossMarginRelative +
                ", netMarginRelative=" + netMarginRelative +
                ", profitMarginRelative=" + profitMarginRelative +
                ", currentRatio=" + currentRatio +
                ", quickRatio=" + quickRatio +
                ", netCashPosition=" + netCashPosition +
                ", longTermSolvabilityRelative=" + longTermSolvabilityRelative +
                ", interestCoverage=" + interestCoverage +
                ", daysReceivableRatio=" + daysReceivableRatio +
                ", daysInventoryRatio=" + daysInventoryRatio +
                ", technicalResultToPremiumIncomeRatio=" + technicalResultToPremiumIncomeRatio +
                ", netIncomeToPremiumIncomeRatio=" + netIncomeToPremiumIncomeRatio +
                ", liableAssetsToTotalBalanceSheetRatio=" + liableAssetsToTotalBalanceSheetRatio +
                ", bisCapital=" + bisCapital +
                ", tier1Ratio=" + tier1Ratio +
                ", tier2Ratio=" + tier2Ratio +
                ", shareholdersEquityToPremiumIncomeRatio=" + shareholdersEquityToPremiumIncomeRatio +
                ", numberOfEmployees=" + numberOfEmployees +
                ", turnoverPerEmployee=" + turnoverPerEmployee +
                ", expensesPerEmployee=" + expensesPerEmployee +
                ", resultBeforeTaxesToEmployeeCostsInThousands=" + resultBeforeTaxesToEmployeeCostsInThousands +
                ", numberOfSharesInThousands=" + numberOfSharesInThousands +
                ", netIncomcePerShare=" + netIncomcePerShare +
                ", dividendPerShare=" + dividendPerShare +
                ", dividendReturn=" + dividendReturn +
                ", cashflowPerShare=" + cashflowPerShare +
                ", intrinsicValuePerShare=" + intrinsicValuePerShare +
                ", payoutRatio=" + payoutRatio +
                ", priceEarningRatio=" + priceEarningRatio +
                ", priceBookValue=" + priceBookValue +
                ", financialYearEnd=" + financialYearEnd +
                ", currentProfitPerShare=" + currentProfitPerShare +
                ", currentCashflowPerShare=" + currentCashflowPerShare +
                ", averageNumberOfShares=" + averageNumberOfShares +
                ", isProvisional=" + isProvisional +
                '}';
    }
}