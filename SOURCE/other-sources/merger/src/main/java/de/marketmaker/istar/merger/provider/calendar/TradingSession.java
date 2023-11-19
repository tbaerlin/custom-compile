/*
 * TradingSession.java
 *
 * Created on 11.09.2006 13:01:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import org.joda.time.LocalDateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TradingSession {
    /**
     * @return session interval in the corresponding market's time zone
     */
    Interval sessionInterval();

    /**
     * @return session interval translated into the gizen time zone
     */
    Interval sessionInterval(DateTimeZone zone);
}
