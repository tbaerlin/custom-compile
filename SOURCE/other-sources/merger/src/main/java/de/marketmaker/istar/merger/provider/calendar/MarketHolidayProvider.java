/*
 * MarketHolidayProvider.java
 *
 * Created on 11.09.2006 14:40:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MarketHolidayProvider {
    boolean isHoliday(LocalDate date, long marketid);
}
