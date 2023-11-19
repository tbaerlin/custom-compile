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
public interface AnnualReportLiabilities {

    BigDecimal getShareholdersEquity();

    BigDecimal getPreferentEquity();

    BigDecimal getMinorityInterests();

    BigDecimal getGroupEquity();

    BigDecimal getSubordinatedLoans();

    BigDecimal getGeneralRiskAllowance();

    BigDecimal getLiableCapital();

    BigDecimal getLiabilitiesDueToBanks();

    BigDecimal getCustomerDepositsSavings();

    BigDecimal getCustomerDepositsOther();

    BigDecimal getDebtSecurities();

    BigDecimal getTotalBanking();

    BigDecimal getTechnicalProvisions();

    BigDecimal getTechnicalProvisionsWithInvestmentsNotForOwnRisk();

    BigDecimal getTotalTechnicalProvisions();

    BigDecimal getDepositsReceivedFromReinsurers();

    BigDecimal getTotalInsurance();

    BigDecimal getProvisions();

    BigDecimal getLongTermDebts();

    BigDecimal getAccountsPayable();

    BigDecimal getBank();

    BigDecimal getOtherShortTermLiabilities();

    BigDecimal getShortTermLiabilities();

    BigDecimal getOtherLiabilities();

    BigDecimal getAccrualsAndDeferredIncome();

    BigDecimal getTotalLiabilities();

    boolean isProvisional();
}