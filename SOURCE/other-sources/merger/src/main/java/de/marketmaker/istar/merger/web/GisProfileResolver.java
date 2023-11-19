/*
 * GisProfileResolver.java
 *
 * Created on 04.02.2008 13:35:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;

/**
 * A ProfileResolver that depends on a "Xun" or "guid" request parameter as authentication
 * information.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisProfileResolver extends DefaultProfileResolver {
    private String authenticationType;

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    @Override
    protected String getAuthentication(HttpServletRequest request, String type) {
        final String prefix = request.getParameter("authenticationPrefix");
        if (avoidKISRequest(prefix)) {
            return null;
        }

        final String xun = request.getParameter("Xun");
        final String id = StringUtils.hasText(xun) ? xun : request.getParameter("guid");

        return prefix != null && id != null ? prefix + id : id;
    }

    @Override
    protected String getAuthenticationType(HttpServletRequest request) {
        final String prefix = request.getParameter("authenticationPrefix");
        if (avoidKISRequest(prefix)) {
            return null;
        }

        if (prefix == null) {
            final String authentication = getAuthentication(request, this.authenticationType);
            if (authentication == null) {
                return null;
            }
            if ("gis".equals(this.authenticationType)) { //do not log web.xl requests
                this.logger.warn("<getAuthenticationType> evaluated old KIS relevant request for '"
                        + authentication + "' (requestURI: " + request.getRequestURI() + ")");
            }
            return this.authenticationType;
        }

        return request.getParameter("authenticationType");
    }

    private boolean avoidKISRequest(String prefix) {
        return "NONE".equals(prefix);
    }

    @Override
    protected Profile getProfile(HttpServletRequest request, ProfileRequest pr) {
        Profile profile = super.getProfile(request, pr);
        if (profile != null) {
            this.logger.debug("<getProfile> valid profile " + profile.getName()
                    + " for " + request.getRequestURI()
                    + (StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : ""));
        }
        return profile;
    }

    @Override
    protected Profile handleInvalidProfileRequest() {
        return null;
    }
}