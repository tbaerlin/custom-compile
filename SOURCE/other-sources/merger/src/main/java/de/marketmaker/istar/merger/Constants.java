/*
 * Constants.java
 *
 * Created on 09.08.2006 11:03:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class Constants {
    public static final BigDecimal MINUS_ONE = BigDecimal.ZERO.subtract(BigDecimal.ONE);

    public static final BigDecimal ONE_PERCENT = BigDecimal.valueOf(1, 2); // 0.01

    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    public static final LocalDate EARLIEST_CHART_DAY = new LocalDate(1900, 1, 1);

    public static final Period ONE_DAY = ISOPeriodFormat.standard().parsePeriod("P1D");
}
