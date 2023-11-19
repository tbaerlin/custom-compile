/*
 * DividendData.java
 *
 * Created on 21.10.2014 15:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author jkirchg
 */
public interface DividendData {

    int getYear();

    BigDecimal getDividendPayment();

    BigDecimal getDividendPayoutRatio();

    BigDecimal getDividendYield();

    BigDecimal getDividendCoverage();

    BigDecimal getDividendPerShare();

    BigDecimal getDividendPerShareGrowth5y();

}
