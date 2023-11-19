/*
 * CompanyDate.java
 *
 * Created on 16.07.2006 16:32:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.YearMonthDay;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CompanyDate {
    YearMonthDay getDate();

    LocalizedString getEvent();

    Long getInstrumentid();
}
