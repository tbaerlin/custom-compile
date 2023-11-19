/*
 * User.java
 *
 * Created on 27.07.2006 11:33:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class User implements Serializable {
    private long id;

    private long companyid;

    private String login;

    private String password;

    private DateTime createdon;

    private DateTime lastloginon;

    private DateTime deletedon;

    private DateTime blockedsince;

    private int numlogins;

    private String salutation;

    private String firstname;

    private String lastname;

    private String street;

    private String city;

    private String country;

    private String zipcode;

    private String email;

    private String phone;

    private String fax;

    private String costcenter;

    private String appTitle;

    private Portfolios portfolios;

    private Map<String, Property> properties;

    User() {
    }

    User deepCopy() {
        final User result = new User();
        result.id = id;
        result.companyid = this.companyid;
        result.login = this.login;
        result.password = this.password;
        result.createdon = this.createdon;
        result.lastloginon = this.lastloginon;
        result.deletedon = this.deletedon;
        result.blockedsince = this.blockedsince;
        result.numlogins = this.numlogins;
        result.salutation = this.salutation;
        result.firstname = this.firstname;
        result.lastname = this.lastname;
        result.street = this.street;
        result.city = this.city;
        result.country = this.country;
        result.zipcode = this.zipcode;
        result.email = this.email;
        result.phone = this.phone;
        result.fax = this.fax;
        result.costcenter = this.costcenter;
        result.appTitle = this.appTitle;
        result.portfolios = this.portfolios.deepCopy();
        if (this.properties != null) {
            result.properties = new HashMap(this.properties);
        }
        return result;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer(1000);
        sb.append("User[").append(this.id)
                .append(", companyid=").append(this.companyid)
                .append(", login=").append(this.login)
                .append(", password=").append(this.password)
                .append(", createdon=").append(this.createdon)
                .append(", lastloginon=").append(this.lastloginon)
                .append(", deletedon=").append(this.deletedon)
                .append(", blockedsince=").append(this.blockedsince)
                .append(", numlogins=").append(this.numlogins)
                .append(", saluation=").append(this.salutation)
                .append(", firstname=").append(this.firstname)
                .append(", lastname=").append(this.lastname)
                .append(", street=").append(this.street)
                .append(", city=").append(this.city)
                .append(", country=").append(this.country)
                .append(", zipcode=").append(this.zipcode)
                .append(", email=").append(this.email)
                .append(", phone=").append(this.phone)
                .append(", fax=").append(this.fax)
                .append(", costcenter=").append(this.costcenter)
                .append(", appTitle=").append(this.appTitle)
                .append(", portfolios=").append(this.portfolios)
                .append(", properties=").append(this.properties)
                .append("]");
        return sb.toString();
    }

    void setBlockedsince(DateTime blockedsince) {
        this.blockedsince = blockedsince;
    }

    void setCity(String city) {
        this.city = city;
    }

    void setCostcenter(String costcenter) {
        this.costcenter = costcenter;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    void setCountry(String country) {
        this.country = country;
    }

    void setCreatedon(DateTime createdon) {
        this.createdon = createdon;
    }

    void setDeletedon(DateTime deletedon) {
        this.deletedon = deletedon;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setFax(String fax) {
        this.fax = fax;
    }

    void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    void setId(long id) {
        this.id = id;
    }

    void setLastloginon(DateTime lastloginon) {
        this.lastloginon = lastloginon;
    }

    void setLastname(String lastname) {
        this.lastname = lastname;
    }

    void setLogin(String login) {
        this.login = login;
    }

    void setNumlogins(int numlogins) {
        this.numlogins = numlogins;
    }

    void setPassword(String password) {
        this.password = password;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    void setPortfolios(Portfolios portfolios) {
        this.portfolios = portfolios;
    }

    void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    void setStreet(String street) {
        this.street = street;
    }

    void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    long getCompanyid() {
        return companyid;
    }

    void setCompanyid(long companyid) {
        this.companyid = companyid;
    }

    public DateTime getBlockedsince() {
        return blockedsince;
    }

    public String getCity() {
        return city;
    }

    public String getCostcenter() {
        return costcenter;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public String getCountry() {
        return country;
    }

    public DateTime getCreatedon() {
        return createdon;
    }

    public DateTime getDeletedon() {
        return deletedon;
    }

    public String getEmail() {
        return email;
    }

    public String getFax() {
        return fax;
    }

    public String getFirstname() {
        return firstname;
    }

    public DateTime getLastloginon() {
        return lastloginon;
    }

    public String getLastname() {
        return lastname;
    }

    public String getLogin() {
        return login;
    }

    public int getNumlogins() {
        return numlogins;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public Map<String, Property> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public String getSalutation() {
        return salutation;
    }

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public long getId() {
        return id;
    }

    public List<Portfolio> getWatchlists() {
        return this.portfolios.getWatchlists();
    }

    public List<Portfolio> getPortfolios() {
        return this.portfolios.getPortfolios();
    }

    public Portfolio getPortfolio(long id) {
        return this.portfolios.getPortfolio(id);
    }

    public Portfolio getPortfolioOrWatchlist(long id) {
        return this.portfolios.get(id);
    }

    public Portfolio getWatchlist(long id) {
        return this.portfolios.getWatchlist(id);
    }

    void add(Portfolio p) {
        this.portfolios.add(p);
    }

    void remove(Portfolio p) {
        this.portfolios.remove(p.getId());
    }

    public String getProperty(String key) {
        if (this.properties == null) {
            return null;
        }
        final Property property = this.properties.get(key);
        return property != null ? property.getValue() : null;
    }
}
