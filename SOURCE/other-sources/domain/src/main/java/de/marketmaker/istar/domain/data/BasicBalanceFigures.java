/*
 * BasicBalanceFigures.java
 *
 * Created on 12.07.2006 22:59:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;
import de.marketmaker.istar.domain.Currency;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface BasicBalanceFigures {
    ReferenceInterval getReference();

    String getCurrency();

    BigDecimal getEquityCapital();

    BigDecimal getEquityCapitalPercent();

    BigDecimal getShareholdersEquity();

    BigDecimal getBalanceSheetTotal();

    Integer getNumberOfEmployees();

    BigDecimal getAssets();
}
