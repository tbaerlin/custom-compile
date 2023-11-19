/*
 * ClientConfig.java
 *
 * Created on 26.05.2009 13:13:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.UserMasterData;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configuration for a particular client ("Mandant"), also acts as a default UserMasterData.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ClientConfig implements UserMasterData {

    public static class SessionSelectorDescription {
        private final String name;
        private final String xpath;

        private SessionSelectorDescription(String name, String xpath) {
            this.name = name;
            this.xpath = xpath;
        }

        public String getName() {
            return name;
        }

        public String getXpath() {
            return xpath;
        }
    }

    enum SessionMode {
        NONE, // no session will be created
        EXCLUSIVE, // max 1 session per user in db, new login terminates previous session if any
        SHARED // unlimited logins per user in db, logins are not tracked
    }


    private String appId;
    private String appTitle;
    private int changePasswordAfterDays;
    private String clientId;
    private final int id;
    private String idOfMusterdepotUser;
    private String idOfTemplateUser;
    private String initialPasswordHash;
    private String initialPassword;
    private String moduleName;
    private Gender gender;
    private String customerName;
    private SessionMode sessionMode = SessionMode.NONE;
    private String loginAuthType = "vwd-ent:ByLogin";
    private final List<SessionSelectorDescription> sessionSelectors = new ArrayList<>();

    public ClientConfig(int id) {
        this.id = id;
    }

    public void add(String name, String xpath) {
        this.sessionSelectors.add(new SessionSelectorDescription(name, xpath));
    }

    public List<SessionSelectorDescription> getSessionSelectors() {
        return this.sessionSelectors;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String getAppTitle() {
        return this.appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    @Override
    public String getGisCustomerId() {
        return null;
    }

    public String getCentralBank() {
        return null;
    }

    public int getChangePasswordAfterDays() {
        return this.changePasswordAfterDays;
    }

    void setChangePasswordAfterDays(int changePasswordAfterDays) {
        this.changePasswordAfterDays = changePasswordAfterDays;
    }

    public String getClientId() {
        return this.clientId;
    }

    void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getMandatorId() {
        return getClientId();
    }

    @Override
    public String getFirstName() {
        return null;
    }

    @Override
    public String getGenoId() {
        return null;
    }

    public int getId() {
        return this.id;
    }

    public String getIdOfMusterdepotUser() {
        return this.idOfMusterdepotUser;
    }

    void setIdOfMusterdepotUser(String idOfMusterdepotUser) {
        this.idOfMusterdepotUser = idOfMusterdepotUser;
    }

    public String getIdOfTemplateUser() {
        return this.idOfTemplateUser;
    }

    void setIdOfTemplateUser(String idOfTemplateUser) {
        this.idOfTemplateUser = idOfTemplateUser;
    }

    public String getInitialPasswordHash() {
        return this.initialPasswordHash;
    }

    void setInitialPasswordHash(String initialPasswordHash) {
        this.initialPasswordHash = initialPasswordHash;
    }

    public String getInitialPassword() {
        return initialPassword;
    }

    void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    @Override
    public String getLastName() {
        return null;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public Map<String, String> getStaticAccounts() {
        return Collections.emptyMap();
    }

    @Override
    public String getVdbLogin() {
        return null;
    }

    @Override
    public String getVdbPassword() {
        return null;
    }

    @Override
    public String getVwdId() {
        return null;
    }

    public SessionMode getSessionMode() {
        return sessionMode;
    }

    void setSessionMode(String sessionMode) {
        this.sessionMode = StringUtils.hasText(sessionMode)
            ? SessionMode.valueOf(sessionMode.toUpperCase().trim())
            : SessionMode.NONE;
    }

    public boolean isWithSession() {
        return this.sessionMode !=  SessionMode.NONE;
    }

    public boolean isWithExclusiveSession() {
        return this.sessionMode == SessionMode.EXCLUSIVE;
    }

    public boolean isCountLogin() {
        return this.sessionMode == SessionMode.EXCLUSIVE;
    }

    public String getLoginAuthType() {
        return this.loginAuthType;
    }

    public void setLoginAuthType(String loginAuthType) {
        if (StringUtils.hasText(loginAuthType)) {
            this.loginAuthType = loginAuthType;
        }
    }

    @Override
    public String nodeText(String s) {
        return null;
    }

    @Override
    public String attributeText(String s) {
        return null;
    }

    @Override
    public String toString() {
        return "ClientConfig[" + this.id
                + ", module=" + this.moduleName
                + ", appId=" + this.appId
                + ", clientId=" + this.clientId
                + ", customerName=" + this.customerName
                + "]";
    }

    public String[] getUserSelectors(UserMasterData data) {
        final String[] result = new String[4];
        int i = 0;
        for (SessionSelectorDescription description : this.sessionSelectors) {
            result[i] = data.nodeText(description.getXpath());
            if (++i == result.length) {
                break;
            }
        }
        return result;
    }
}
