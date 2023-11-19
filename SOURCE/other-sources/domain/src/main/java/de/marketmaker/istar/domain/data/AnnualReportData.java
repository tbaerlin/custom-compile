/*
 * AnnualReportData.java
 *
 * Created on 18.03.2010 07:55:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface AnnualReportData {
    long getInstrumentid();

    Interval getReference();

    AnnualReportAssets getAssets();

    AnnualReportLiabilities getLiabilities();

    AnnualReportBalanceSheet getBalanceSheet();

    AnnualReportKeyFigures getKeyFigures();
}
