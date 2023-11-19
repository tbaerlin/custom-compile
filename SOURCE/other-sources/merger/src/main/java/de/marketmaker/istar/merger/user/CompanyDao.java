/*
 * CompanyDao.java
 *
 * Created on 02.08.2006 16:15:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CompanyDao {
    public Company selectCompany(long id);
}
