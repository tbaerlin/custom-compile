/*
 * CompanyDaoDb.java
 *
 * Created on 02.08.2006 16:32:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyDaoDb extends JdbcDaoSupport implements CompanyDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private SelectCompanyProperties selectCompanyProperties;
    private SelectCompany selectCompany;


    private class SelectCompanyProperties extends MappingSqlQuery {

        public SelectCompanyProperties() {
            super(getDataSource(), "SELECT id, code, value FROM companyproperties " +
                    "WHERE companyid=? OR companyid=0");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return new Property(rs.getLong(1), rs.getString(2), rs.getString(3));
        }

        protected Map<String, Property> getProperties(long id) {
            //noinspection unchecked
            final List<Property> properties = execute(id);
            if (properties == null || properties.isEmpty()) {
                return null;
            }
            final Map<String, Property> result = new HashMap<>();
            for (Property property : properties) {
                result.put(property.getKey(), property);
            }
            return result;
        }
    }

    private class SelectCompany extends MappingSqlQuery {
        public SelectCompany() {
            super(getDataSource(), "SELECT id, name FROM companies WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            final Company result = new Company();
            result.setId(rs.getLong(1));
            result.setName(rs.getString(2));
            return result;
        }
    }

    protected void initDao() throws Exception {
        super.initDao();
        this.selectCompany = new SelectCompany();
        this.selectCompanyProperties = new SelectCompanyProperties();
    }

    public Company selectCompany(long id) {
        final Company company = (Company) this.selectCompany.findObject(id);
        if (company == null) {
            return null;
        }
        final Map<String, Property> properties = this.selectCompanyProperties.getProperties(id);
        company.setProperties(properties);
        return company;
    }

}
