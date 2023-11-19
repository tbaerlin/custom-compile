/*
 * User.java
 *
 * Created on 29.04.2008 10:19:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class User implements Serializable {
    protected static final long serialVersionUID = 1L;

    public enum Gender {FEMALE, MALE, UNKNOWN}

    private String login;

    private String vwdId;

    private String gisCustomerId;

    private String genoId;

    private String firstName;

    private String lastName;

    private String centralBank;

    private String vdbLogin;

    private String vdbPassword;

    private String appId;

    private String appTitle;

    private int client;

    private String customerName;

    private Gender gender = Gender.UNKNOWN;

    /**
     * Determines which menus are available, whether user can create own pages, how many of those
     * with how many snippets, etc.
     */
    private AppProfile appProfile;

    /**
     * Contains application content like watchlists, portfolios, workspaces etc.
     */
    private AppConfig appConfig;

    /**
     * the three most recent passwords, current is at index 0.
     * transient because password is not needed in client
     */
    private transient String[] passwords;

    private boolean passwordChangeRequired;

    private Date passwordChangeDate;

    private HttpBasicAuthCredentials gisPortalHttpBasicAuthCredentials;

    public User() {
    }

    public User(User user) {
        this.appConfig = user.appConfig;
        this.appId = user.appId;
        this.appProfile = user.appProfile;
        this.appTitle = user.appTitle;
        this.centralBank = user.centralBank;
        this.client = user.client;
        this.customerName = user.customerName;
        this.firstName = user.firstName;
        this.gender = user.gender;
        this.lastName = user.lastName;
        this.login = user.login;
        this.gisCustomerId = user.gisCustomerId;
        this.genoId = user.genoId;
        this.passwordChangeDate = user.passwordChangeDate;
        this.passwordChangeRequired = user.passwordChangeRequired;
        this.passwords = user.passwords;
        this.vdbLogin = user.vdbLogin;
        this.vdbPassword = user.vdbPassword;
        this.vwdId = user.vwdId;
        this.gisPortalHttpBasicAuthCredentials = user.gisPortalHttpBasicAuthCredentials;
    }

    public String toString() {
        return "User[" + this.login + ", " + this.appProfile + ", " + this.appConfig + "]"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public String getVwdId() {
        return this.vwdId;
    }

    public void setVwdId(String vwdId) {
        this.vwdId = vwdId;
    }

    public String getGisCustomerId() {
        return gisCustomerId;
    }

    public void setGisCustomerId(String gisCustomerId) {
        this.gisCustomerId = gisCustomerId;
    }

    public String getGenoId() {
        return genoId;
    }

    public void setGenoId(String genoId) {
        this.genoId = genoId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public AppConfig getAppConfig() {
        return this.appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public AppProfile getAppProfile() {
        return this.appProfile;
    }

    public void setAppProfile(AppProfile appProfile) {
        this.appProfile = appProfile;
    }

    public String getCentralBank() {
        return this.centralBank;
    }

    public void setCentralBank(String centralBank) {
        this.centralBank = centralBank;
    }

    public String[] getPasswords() {
        return this.passwords;
    }

    public String getPassword() {
        return this.passwords[0];
    }

    public void setPasswords(String[] passwords) {
        this.passwords = passwords;
    }

    public boolean isPasswordChangeRequired() {
        return this.passwordChangeRequired;
    }

    public void setPasswordChangeRequired(boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getVdbLogin() {
        return this.vdbLogin;
    }

    public void setVdbLogin(String vdbLogin) {
        this.vdbLogin = vdbLogin;
    }

    public String getVdbPassword() {
        return this.vdbPassword;
    }

    public void setVdbPassword(String vdbPassword) {
        this.vdbPassword = vdbPassword;
    }

    public String getAppTitle() {
        return this.appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public Date getPasswordChangeDate() {
        return this.passwordChangeDate;
    }

    public void setPasswordChangeDate(Date passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     *  returns a unique id that identifies a user.
     *  eg. login for "advisory solution" or vwdid for "mmf[web]".
     */
    public String getUid() {
        return getVwdId();
    }

    /**
     * HTTP Basic Auth credentials for authenticating GIS Portal via a pre-flight XHR request before opening the GIS Portal window.
     * Note: It is highly insecure to transport the password to the client to trigger a XHR request with it.
     * It would be more secure to use OAuth or some kind of a security token, but we do not have enough time to implement such a solution.
     * Additionally, GIS Portal is not accessible via a secure HTTP connection, so every man in the middle attacker can easily read the transmitted passwords.
     * FFM insists on HTTP Basic Auth for the GIS Portal, because it is the easiest way for them and they do not need to touch their out-dated JOOMLA.
     */
    public HttpBasicAuthCredentials getGisPortalHttpBasicAuthCredentials() {
        return this.gisPortalHttpBasicAuthCredentials;
    }

    public void setGisPortalHttpBasicAuthCredentials(
            HttpBasicAuthCredentials gisPortalHttpBasicAuthCredentials) {
        this.gisPortalHttpBasicAuthCredentials = gisPortalHttpBasicAuthCredentials;
    }
}
