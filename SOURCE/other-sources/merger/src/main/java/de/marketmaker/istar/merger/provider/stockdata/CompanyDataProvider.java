/*
 * CompanyDataProvider.java
 *
 * Created on 16.03.2010 16:02:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.stockdata;

import java.util.List;
import java.util.Locale;

import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.data.CompanyProfile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CompanyDataProvider {
    List<AnnualReportData> getAnnualReportData(CompanyDataRequest request);

    CompanyProfile getCompanyProfile(CompanyDataRequest request);
}
