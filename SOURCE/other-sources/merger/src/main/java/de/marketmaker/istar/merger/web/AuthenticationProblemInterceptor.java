/*
 * ProfileInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.util.StringUtils;

import static de.marketmaker.istar.merger.web.ProfileResolver.*;

/**
 * HACK to be able to process requests that come without authentication/-Type
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AuthenticationProblemInterceptor extends HandlerInterceptorAdapter {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private int numProblemsMarketmanager;

    private int numProblemsPSplus;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object o) throws Exception {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);

        if (z != null) {
            if ("marketmanager".equals(z.getName())) {
                ensureMarketmanagerAuthentication(request);
            }
            else if ("psplus".equals(z.getName())) {
                ensurePSplusAuthentication(request);
            }
        }

        return true;
    }

    private void ensureMarketmanagerAuthentication(HttpServletRequest request) {
        if (!StringUtils.hasText(getAuthentication(request)) || !StringUtils.hasText(getAuthenticationType(request))) {
            request.setAttribute(AUTHENTICATION_KEY, "mm-xml");
            request.setAttribute(AUTHENTICATION_TYPE_KEY, "resource");
            if (this.numProblemsMarketmanager++ % 100 == 0) {
                this.logger.warn("<ensureMarketmanagerAuthentication> " + this.numProblemsMarketmanager
                        + "x, missing authentication or authenticationType for zone marketmanager => setting to mm-xml/resource");
            }
        }
    }

    private void ensurePSplusAuthentication(HttpServletRequest request) {
        final String authentication = getAuthentication(request);
        if (authentication != null && authentication.contains(":")) {
            // psplus has an old version of its software in production, which cannot set the
            // authenticationType to vwd-ent:ByVwdId => detect vwdId-based request through
            // the appId/vwdId delimiter (colon) and overwrite the authenticationType
            request.setAttribute(AUTHENTICATION_TYPE_KEY, "vwd-ent:ByVwdId");
        }
        else if (!StringUtils.hasText(getAuthenticationType(request))) {
            request.setAttribute(AUTHENTICATION_TYPE_KEY, "resource");
            if (this.numProblemsPSplus++ % 100 == 0) {
                this.logger.warn("<ensurePSplusAuthentication> " + this.numProblemsPSplus
                        + "x , missing authenticationType for zone psplus => setting to resource");
            }
        }
    }

    protected String getAuthentication(HttpServletRequest request) {
        return getValue(request, AUTHENTICATION_KEY);
    }

    protected String getAuthenticationType(HttpServletRequest request) {
        return getValue(request, AUTHENTICATION_TYPE_KEY);
    }

    private String getValue(HttpServletRequest request, final String key) {
        return HttpRequestUtil.getValue(request, key);
    }
}
