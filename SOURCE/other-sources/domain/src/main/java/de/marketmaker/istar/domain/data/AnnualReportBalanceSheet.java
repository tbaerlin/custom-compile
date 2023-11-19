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
public interface AnnualReportBalanceSheet {

    BigDecimal getInterestIncome();

    BigDecimal getInterestExpenses();

    BigDecimal getNetInterestIncome();

    BigDecimal getIncomeFromStocksAndParticipations();

    BigDecimal getNetCommissionIncome();

    BigDecimal getNetTradingIncome();

    BigDecimal getGrossOperatingIncomeBanking();

    BigDecimal getGeneralAdministrativeExpenses();

    BigDecimal getTurnover();

    BigDecimal getExternalCosts();

    BigDecimal getPersonnelCosts();

    BigDecimal getOperationalCosts();

    BigDecimal getBrutSalesMargin();

    BigDecimal getDepreciationsAndAmortizations();

    BigDecimal getOperationalBankResult();

    BigDecimal getProvisions();

    BigDecimal getOtherRevenuesAndExpensesBanking();

    BigDecimal getResultBankingBeforeTaxes();

    BigDecimal getTotalGrossPremiums();

    BigDecimal getTechnicalResult();

    BigDecimal getInvestmentActivitiesNotIncludedInTechnicalResult();

    BigDecimal getOperationalResultInsurance();

    BigDecimal getOtherIncomeAndCostsInsurance();

    BigDecimal getResultInsuranceBeforeTaxes();

    BigDecimal getOtherIncomce();

    BigDecimal getTotalCurrentResultBeforeTaxes();

    BigDecimal getConsolidatedIncomeFromPrivateEquityParticipations();

    BigDecimal getResultFromCurrentOperations();

    BigDecimal getOperationalIncome();

    BigDecimal getFinacialResult();

    BigDecimal getFinacialRevenues();

    BigDecimal getFinancialExpenditures();

    BigDecimal getOtherRevenuesAndExpenditures();

    BigDecimal getCompaniesUnderEquity();

    BigDecimal getResultBeforeTaxes();

    BigDecimal getTaxes();

    BigDecimal getExtraordinaryResult();

    BigDecimal getMinorityInterests();

    BigDecimal getNetIncome();

    BigDecimal getDividendOnPreferredShares();

    boolean isProvisional();
}