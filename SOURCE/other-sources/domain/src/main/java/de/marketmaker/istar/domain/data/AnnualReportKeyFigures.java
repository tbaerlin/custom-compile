/*
 * AnnualReportDataImpl.java
 *
 * Created on 17.03.2010 15:56:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface AnnualReportKeyFigures {

    BigDecimal getCurrentResultBankingBeforeTaxes();

    BigDecimal getCurrentResultInsuranceBeforeTaxes();

    BigDecimal getTotalGrossPremiums();

    BigDecimal getTurnover();

    BigDecimal getNetIncome();

    BigDecimal getCashflow();

    BigDecimal getInterestToGrossBankResultRatio();

    BigDecimal getProvisionsToGrossBankResultRatio();

    BigDecimal getNetTradingIncomeToGrossBankResultRatio();

    BigDecimal getResultBankingToGrossBankResultRatio();

    BigDecimal getResultInsuranceToTotalResultBeforeTaxesRatio();

    BigDecimal getReturnOnEquity();

    BigDecimal getReturnOnAssets();

    BigDecimal getGrossMarginRelative();

    BigDecimal getNetMarginRelative();

    BigDecimal getProfitMarginRelative();

    BigDecimal getCurrentRatio();

    BigDecimal getQuickRatio();

    BigDecimal getNetCashPosition();

    BigDecimal getLongTermSolvabilityRelative();

    BigDecimal getInterestCoverage();

    BigDecimal getDaysReceivableRatio();

    BigDecimal getDaysInventoryRatio();

    BigDecimal getTechnicalResultToPremiumIncomeRatio();

    BigDecimal getNetIncomeToPremiumIncomeRatio();

    BigDecimal getLiableAssetsToTotalBalanceSheetRatio();

    BigDecimal getBisCapital();

    BigDecimal getTier1Ratio();

    BigDecimal getTier2Ratio();

    BigDecimal getShareholdersEquityToPremiumIncomeRatio();

    BigDecimal getNumberOfEmployees();

    BigDecimal getTurnoverPerEmployee();

    BigDecimal getExpensesPerEmployee();

    BigDecimal getResultBeforeTaxesToEmployeeCostsInThousands();

    BigDecimal getNumberOfSharesInThousands();

    BigDecimal getNetIncomcePerShare();

    BigDecimal getDividendPerShare();

    BigDecimal getDividendReturn();

    BigDecimal getCashflowPerShare();

    BigDecimal getIntrinsicValuePerShare();

    BigDecimal getPayoutRatio();

    BigDecimal getPriceEarningRatio();

    BigDecimal getPriceBookValue();

    BigDecimal getFinancialYearEnd();

    BigDecimal getCurrentProfitPerShare();

    BigDecimal getCurrentCashflowPerShare();

    BigDecimal getAverageNumberOfShares();

    boolean isProvisional();
}