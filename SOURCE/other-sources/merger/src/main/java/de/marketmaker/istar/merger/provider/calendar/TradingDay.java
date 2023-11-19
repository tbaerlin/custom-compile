/*
 * TradingDay.java
 *
 * Created on 11.09.2006 10:11:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TradingDay {

    LocalDate day();

    /**
     * Returns the trading session(s) for this day
     * @return
     */
    TradingSession[] sessions();
}
