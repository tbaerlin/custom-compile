/*
 * TradingCalendarProvider.java
 *
 * Created on 11.09.2006 10:02:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import de.marketmaker.istar.domain.Market;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TradingCalendarProvider {
    TradingCalendar calendar(Market m);
}
