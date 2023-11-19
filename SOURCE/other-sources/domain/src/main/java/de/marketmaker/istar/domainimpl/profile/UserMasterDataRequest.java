/*
 * UserMasterDataRequest.java
 *
 * Created on 14.07.2008 14:11:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMasterDataRequest extends AbstractIstarRequest {
    public enum IdType {
        VWD, GENO, LOGIN
    }

    static final long serialVersionUID = 1L;

    /** @deprecated */
    public static UserMasterDataRequest forGenoId(String id) {
        return new UserMasterDataRequest(id, IdType.GENO);
    }

    public static UserMasterDataRequest forVwdId(String id) {
        return new UserMasterDataRequest(id, IdType.VWD);
    }

    public static UserMasterDataRequest forLogin(String id, String clientId) {
        return new UserMasterDataRequest(id, IdType.LOGIN, clientId);
    }

    private String appId;

    private final String id;

    private String requesterId;

    private String clientId;

    private final IdType type;

    private UserMasterDataRequest(String id, IdType type) {
        this(id, type, null);
    }

    private UserMasterDataRequest(String id, IdType type, String clientId) {
        if (id == null) {
            throw new NullPointerException("id is null");
        }
        this.id = id;
        this.type = type;
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + getClientInfo()
                + ", id=" + this.id + "(" + this.type + ")"
                + ", requesterId=" + this.requesterId
                + ", clientId=" + this.clientId
                + ", appId=" + this.appId
                + "]";
    }

    public String getAppId() {
        return appId;
    }

    public String getId() {
        return id;
    }

    /** @deprecated */
    public String getRequesterId() {
        return requesterId;
    }

   public IdType getType() {
        return type;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    /** @deprecated */
    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getClientId() {
        return this.clientId;
    }
}
