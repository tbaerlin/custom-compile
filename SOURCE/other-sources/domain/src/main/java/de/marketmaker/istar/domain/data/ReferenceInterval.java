/*
 * ReferenceIntervalImpl.java
 *
 * Created on 20.09.2006 15:45:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ReferenceInterval {
    Interval getInterval();
    boolean isShortenedFiscalYear();
}
