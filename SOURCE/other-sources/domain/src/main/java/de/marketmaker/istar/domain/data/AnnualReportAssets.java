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
public interface AnnualReportAssets {

    BigDecimal getIntangibleAssets();

    BigDecimal getTangibleAssets();

    BigDecimal getFinancialAssets();

    BigDecimal getFixedAssets();

    BigDecimal getInventories();

    BigDecimal getTraceAccountsReceivable();

    BigDecimal getOtherAccountsReceivable();

    BigDecimal getCashAndEquivalents();

    BigDecimal getOtherCurrentAssets();

    BigDecimal getCash();

    BigDecimal getGovernmentDebtSecurities();

    BigDecimal getDueFromBanks();

    BigDecimal getDueFromCustomers();

    BigDecimal getDebenturesAndOtherFixedIncomes();

    BigDecimal getNumberOfSharesInThousand();

    BigDecimal getEquityAndNonFixedIncome();

    BigDecimal getOtherFinancialInvestments();

    BigDecimal getTotalBanking();

    BigDecimal getInvestmentsInsuranceBusiness();

    BigDecimal getInvestmentsNotAtOwnRisk();

    BigDecimal getReceivables();

    BigDecimal getTotalInsurance();

    BigDecimal getTangibleAndIntangibleAssets();

    BigDecimal getOtherAssets();

    BigDecimal getPrepaymentAndAccruedIncome();

    BigDecimal getTotalAssets();

    boolean isProvisional();
}
