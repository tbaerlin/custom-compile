/*
 * CompanyDateProvider.java
 *
 * Created on 19.07.2008 16:11:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CompanyDateProvider {
    CompanyDateResponse getCompanyDates(CompanyDateRequest request);

    CompanyDateDaysResponse getDaysWithCompanyDates(CompanyDateDaysRequest request);
}
