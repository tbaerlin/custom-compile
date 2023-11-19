/*
 * ProfileRequest.java
 *
 * Created on 04.02.2008 11:34:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfileRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    static final String VWD_ENT_BY_VWD_ID = "vwd-ent:ByVwdId";

    private final String authenticationType;

    private final String authentication;

    private String clientId;

    private String applicationId;

    private boolean useCache = true;

    public static ProfileRequest byVwdLogin(String login, String clientId, String applicationId) {
        final ProfileRequest result = new ProfileRequest("vwd-ent:ByLogin", login);
        result.setClientId(clientId);
        result.setApplicationId(applicationId);
        return result;
    }

    public static ProfileRequest byVwdId(String vwdId, String applicationId) {
        ProfileRequest result = new ProfileRequest(VWD_ENT_BY_VWD_ID, vwdId);
        result.setApplicationId(applicationId);
        return result;
    }

    public ProfileRequest(String authenticationType, String authentication) {
        this.authenticationType = authenticationType;
        this.authentication = authentication;
    }

    public ProfileRequest(String clientInfo, String authenticationType, String authentication) {
        super(clientInfo, true);
        this.authenticationType = authenticationType;
        this.authentication = authentication;
    }

    public boolean isUseCache() {
        return this.useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getAuthentication() {
        return this.authentication;
    }

    public String getAuthenticationType() {
        return this.authenticationType;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getClientInfo()
                + ", auth=" + getAuthentication()
                + ", type=" + getAuthenticationType()
                + ", client=" + getClientId()
                + ", app=" + getApplicationId()
                + "]";
    }

    public String toCacheKey() {
        StringBuilder sb = new StringBuilder(30)
                .append(getAuthenticationType()).append("$").append(getAuthentication());
        if (getClientId() != null) {
            sb.append("$").append(getClientId());
        }
        if (getApplicationId() != null) {
            sb.append("/").append(getApplicationId());
        }
        return sb.toString();
    }

    public boolean isOftenFailingGenobrokerRequest() {
        return getAuthentication() != null && getAuthentication().endsWith("$E")
                && getClientInfo() != null && getClientInfo().contains("gb-request.xml");
    }
}
