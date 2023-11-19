/*
 * HighLow.java
 *
 * Created on 12.07.2006 11:02:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Period;
import org.joda.time.Interval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface HighLow {
    HighLow copy(PriceRecord pr);
    HighLow copy(RatioDataRecord rdr);

    Interval getInterval();
    Price getHigh();
    Price getLow();
}
