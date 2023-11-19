/*
 * VwdEntAccessMethod.java
 *
 * Created on 09.02.2010 11:39:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.domainimpl.profile.ProfileRequest;

/**
 * Represents the different possibilities for requesting a profile from the entitlement server
 */
class VwdEntAccessMethod {
    static final VwdEntAccessMethod BY_LOGIN = new VwdEntAccessMethod("Ent_ByLogin", "Login", true, true);

    static final VwdEntAccessMethod BY_KUNDEN_NR = new VwdEntAccessMethod("Ent_ByKundenNr", "KundenNr", true, true);

    static final VwdEntAccessMethod BY_GENO_ID = new VwdEntAccessMethod("Ent_ByUGenoId", "UGenoID", false, true);

    static final VwdEntAccessMethod BY_VWD_ID = new VwdEntAccessMethod("Ent_ByVwdId", "vwdID", false, true);

    static final VwdEntAccessMethod BY_VWD_ID_NO_APPID = new VwdEntAccessMethod("Ent_ByVwdId_NOAPP", "vwdID", false, false);

    /**
     * Maps authenticationType values to the respective Method.
     */
    private static final Map<String, VwdEntAccessMethod> METHOD_MAPPINGS
            = new HashMap<>();

    static {
        METHOD_MAPPINGS.put("vwd-ent:ByLogin", VwdEntAccessMethod.BY_LOGIN);
        METHOD_MAPPINGS.put("vwd-ent:ByUGenoId", VwdEntAccessMethod.BY_GENO_ID);
        METHOD_MAPPINGS.put("vwd-ent:ByVwdId", VwdEntAccessMethod.BY_VWD_ID);
        METHOD_MAPPINGS.put("vwd-ent:ByKundenNr", VwdEntAccessMethod.BY_KUNDEN_NR);
        METHOD_MAPPINGS.put("userid", VwdEntAccessMethod.BY_VWD_ID);
        // deprecated names:
        METHOD_MAPPINGS.put("vwddz", VwdEntAccessMethod.BY_VWD_ID);
        METHOD_MAPPINGS.put("vwddz-geno", VwdEntAccessMethod.BY_GENO_ID);
    }


    private final boolean addClientId;

    private final boolean addAppId;

    private final String authenticationKey;

    private final String uri;

    static VwdEntAccessMethod getMethod(ProfileRequest request) {
        final VwdEntAccessMethod method = METHOD_MAPPINGS.get(request.getAuthenticationType());
        if (method == VwdEntAccessMethod.BY_VWD_ID && request.getApplicationId() == null) {
            return BY_VWD_ID_NO_APPID;
        }
        return (method != null) ? method : VwdEntAccessMethod.BY_LOGIN;
    }

    private VwdEntAccessMethod(String name, String authenticationKey, boolean addClientId, boolean addAppId) {
        this.uri = name;
        this.authenticationKey = authenticationKey;
        this.addClientId = addClientId;
        this.addAppId = addAppId;
    }

    boolean isAddClientId() {
        return this.addClientId;
    }

    void configure(UriComponentsBuilder b, String appId, ProfileRequest request) {
        if (this.addAppId) {
            b.queryParam("AppID", appId);
        }
        if (this.addClientId) {
            b.queryParam("MandantId", request.getClientId());
        }
        b.queryParam(this.authenticationKey, request.getAuthentication());
    }

    String getUri() {
        return this.uri;
    }

    boolean isValid(ProfileRequest request) {
        if (!StringUtils.hasText(request.getAuthentication())) {
            return false;
        }
        if (this == BY_VWD_ID && !isNumericId(request.getAuthentication())) {
            return false;
        }
        if (this != BY_VWD_ID && !isValidString(request.getAuthentication())) {
            return false;
        }
        if (this.addClientId && !isNumericId(request.getClientId())) {
            return false;
        }
        if (request.getApplicationId() != null && !isNumericId(request.getApplicationId())) {
            return false;
        }
        return true;
    }

    private boolean isValidString(String s) {
        return s.indexOf('&') == -1 && s.indexOf('<') == -1;
    }

    private boolean isNumericId(String s) {
        if (!StringUtils.hasText(s)) {
            return false;
        }
        try {
            return Integer.parseInt(s) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
