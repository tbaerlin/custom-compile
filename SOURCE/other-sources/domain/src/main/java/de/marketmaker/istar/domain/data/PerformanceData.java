/*
 * PerformanceData.java
 *
 * Created on 16.07.2006 16:21:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;

import java.util.List;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PerformanceData {
    boolean isRelative();
    Interval getInterval();
    BigDecimal getPerformance();
}
