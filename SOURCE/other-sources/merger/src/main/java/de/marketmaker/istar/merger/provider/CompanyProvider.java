/*
 * CompanyProvider.java
 *
 * Created on 02.08.2006 10:11:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.merger.user.Company;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CompanyProvider {
    Company getCompany(long id);
}
