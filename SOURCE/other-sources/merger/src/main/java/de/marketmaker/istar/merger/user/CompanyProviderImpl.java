/*
 * CompanyProviderImpl.java
 *
 * Created on 02.08.2006 16:15:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.RemoteCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.merger.provider.CompanyProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyProviderImpl implements CompanyProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CompanyDao companyDao;

    private Ehcache companyCache;

    public void setCompanyCache(Ehcache companyCache) {
        this.companyCache = companyCache;
    }

    public void setCompanyDao(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    public Company getCompany(long id) {
        final Element element = this.companyCache.get(id);
        if (element != null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getCompany> from cache: " + id);
            }
            return (Company) element.getValue();
        }
        final Company company = this.companyDao.selectCompany(id);
        if (company == null) {
            throw new NoSuchCompanyException("Invalid company id: " + id, id);
        }
        try {
            this.companyCache.put(new Element(id, company));
        } catch (RemoteCacheException e) {
            this.logger.warn("<getCompany> remote cache exception: " + e.getMessage());
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getCompany> from dao: " + id);
        }
        return company;
    }
}
