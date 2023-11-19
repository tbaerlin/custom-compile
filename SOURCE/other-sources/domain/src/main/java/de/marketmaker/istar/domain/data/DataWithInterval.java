/*
 * DataWithTimeperiod.java
 *
 * Created on 01.03.2005 12:05:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Period;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DataWithInterval<K> {
    K getData();
    ReadableInterval getInterval();
}
