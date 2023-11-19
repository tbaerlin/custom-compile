/*
 * PibRequest.java
 *
 * Created on 28.03.11 16:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author zzhao
 */
public class DocDataRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = -3283301722081953985L;

    private final String symbol;

    private final String client;

    private final String userInfo;

    private final String address;

    private final String vwdId;

    private Profile profile;

    private final boolean admin;

    private final Map<String, String> props = new HashMap<>(8);

    /**
     * @param symbol identifies instrument
     * @param client identifies client
     * @param userInfo user information
     * @return PibRequest for getting PIBs by symbol
     */
    static public DocDataRequest bySymbol(String symbol, String client, String userInfo,
            String address) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return new DocDataRequest(symbol, client, userInfo, address,
                profile.isAllowed(Selector.DOCM_EDIT_ADM)
                        || profile.isAllowed(Selector.DOCM_EDIT_USER)
        );
    }

    private DocDataRequest(String symbol, String client, String userInfo, String address,
            boolean admin) {
        this.symbol = symbol;
        this.client = client;
        this.userInfo = userInfo;
        this.address = address;
        this.admin = admin;

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            this.profile = requestContext.getProfile();
            this.vwdId = this.profile instanceof VwdProfile ? ((VwdProfile) this.profile).getVwdId() : null;
        }
        else {
            this.profile = null;
            this.vwdId = null;
        }
    }

    public boolean isAdmin() {
        return admin;
    }

    public void putProperty(String key, String value) {
        this.props.put(key, value);
    }

    public boolean isEmpty() {
        return this.props.isEmpty();
    }

    public Map<String, String> getProps() {
        return new HashMap<>(props);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getClient() {
        return client;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getAddress() {
        return address;
    }

    public String getVwdId() {
        return vwdId;
    }

    public Profile getProfile() {
        return profile;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", symbol='").append(symbol).append('\'')
                .append(", client=").append(client)
                .append(", userInfo=").append(userInfo)
                .append(", address=").append(address)
                .append(", profile=").append(profile)
                .append(", vwdId=").append(vwdId)
                .append(", props=").append(props.keySet());
    }
}
