/*
 * DayProvider.java
 *
 * Created on 08.12.2004 12:32:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import org.joda.time.YearMonthDay;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: DayProvider.java,v 1.1 2004/12/08 16:36:35 oliver Exp $
 */
public interface DayProvider {
    int getDayAsYyyyMmDd();

    YearMonthDay getDay();
}
