/*
 * DefaultProfileResolver.java
 *
 * Created on 04.02.2008 13:35:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;

/**
 * A ProfileResolver that uses a ProfileService to query Profiles.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultProfileResolver implements ProfileResolver, Ordered {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int order = Integer.MAX_VALUE;

    private ProfileProvider profileProvider;

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public Profile resolveProfile(HttpServletRequest request) {
        final String type = getAuthenticationType(request);
        if (!StringUtils.hasText(type)) {
            return null;
        }

        if (type.equals(ROOT_AUTHENTICATION_TYPE)) {
            this.logger.info("<resolveProfile> root login from " + request.getRemoteAddr());
            return ProfileFactory.valueOf(true);
        }

        final String key = getAuthentication(request, type);
        if (!StringUtils.hasText(key)) {
            return null;
        }

        final String allowedAuthentication = getValue(request, ALLOWED_AUTHENTICATIONS_KEY);
        if (StringUtils.hasText(allowedAuthentication)) {
            final Set set = StringUtils.commaDelimitedListToSet(allowedAuthentication);
            if (!set.contains(key)) {
                this.logger.info("<resolveProfile> authentication not allowed: " + key);
                return null;
            }
        }

        final ProfileRequest pr = createRequest(request, type, key);
        return getProfile(request, pr);
    }

    private ProfileRequest createRequest(HttpServletRequest request, String type, String key) {
        // auth format is either one of:
        // auth
        // appId:auth
        // clientId:appId:auth
        final String[] tokens = key.split(":");

        int n = tokens.length - 1;
        final String auth = tokens[n--].trim();
        String appId = getIfDefined(tokens, n--);
        String clientId = getIfDefined(tokens, n);

        if (appId == null) {
            appId = getValue(request, APPLICATION_ID_KEY);
        }
        if (clientId == null) {
            clientId = getValue(request, CLIENT_ID_KEY);
        }

        final ProfileRequest result = new ProfileRequest(request.getRequestURI(), type.trim(), auth);
        result.setClientId(clientId);
        result.setApplicationId(appId);
        return result;
    }

    private static String getIfDefined(String[] values, int n) {
        if (n >= 0 && n < values.length && StringUtils.hasText(values[n])) {
            return values[n];
        }
        return null;
    }

    protected Profile getProfile(HttpServletRequest request, ProfileRequest pr) {
        final ProfileResponse response = this.profileProvider.getProfile(pr);
        if (!response.isValid()) {
            if (!pr.isOftenFailingGenobrokerRequest()) {
                this.logger.warn("<getProfile> invalid response for " + pr + " from " + response.getServerInfo());
            }
            return handleInvalidProfileRequest();
        }
        return response.getProfile();
    }

    protected Profile handleInvalidProfileRequest() {
        throw new NoProfileException("no valid profile found");
    }

    protected String getAuthentication(HttpServletRequest request, String type) {
        final String valueBySession = HttpRequestUtil.getValueBySession(request, AUTHENTICATION_KEY);
        if (valueBySession!=null) {
            return valueBySession;
        }

        final String credential = getValue(request, CREDENTIAL_KEY);
        if (credential != null) {
            return ProfileUtil.decodeAuthentication(credential);
        }

        String value = getValue(request, AUTHENTICATION_KEY);
        if (value == null && type.startsWith("pmpub")) {
            value = getValue(request, PM_AUTHENTICATION_KEY);
        }

        if (value != null) {
            final String prefix = request.getParameter("authenticationPrefix");
            return (prefix != null) ? (prefix + value) : value;
        }
        return null;
    }

    protected String getAuthenticationType(HttpServletRequest request) {
        final String valueBySession = HttpRequestUtil.getValueBySession(request, AUTHENTICATION_TYPE_KEY);
        if (valueBySession!=null) {
            return valueBySession;
        }

        final String credential = getValue(request, CREDENTIAL_KEY);
        if (credential != null) {
            return ProfileUtil.decodeAuthenticationType(credential);
        }

        return getValue(request, AUTHENTICATION_TYPE_KEY);
    }

    private String getValue(HttpServletRequest request, final String key) {
        return HttpRequestUtil.getValue(request, key);
    }
}
