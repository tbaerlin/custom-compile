/*
 * BasicHistoricRatios.java
 *
 * Created on 12.07.2006 22:54:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface QuarterlyYield {
    Interval getReference();
    BigDecimal getYield();
}