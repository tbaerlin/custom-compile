/*
 * ProfitAndLoss.java
 *
 * Created on 12.07.2006 22:28:32
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
public interface ProfitAndLoss {
    ReferenceInterval getReference();

    String getCurrency();

    BigDecimal getProfitYear();

    BigDecimal getDilutedEarningPerShare();

    BigDecimal getEarningPerShare();

    BigDecimal getDividend();

    BigDecimal getDividendYield();

    BigDecimal getSales();
}
