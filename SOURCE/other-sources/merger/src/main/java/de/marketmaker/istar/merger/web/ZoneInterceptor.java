/*
 * ProfileInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ZoneInterceptor extends ApplicationObjectSupport implements InitializingBean,
        HandlerInterceptor {
    /**
     * Well-known name for the ZoneResolver object in the bean factory for this namespace.
     */
    public static final String ZONE_RESOLVER_BEAN_NAME = "zoneResolver";

    public static final String ZONE_REQUEST_ATTRIBUTE = Zone.class.getName() + ".REQUEST";

    private ZoneResolver zoneResolver;

    private boolean requireZone = false;

    public void setRequireZone(boolean requireZone) {
        this.requireZone = requireZone;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.zoneResolver != null) {
            this.logger.info("<afterPropertiesSet> using explict zoneResolver");
            return;
        }
        this.zoneResolver = (ZoneResolver)
                getApplicationContext().getBean(ZONE_RESOLVER_BEAN_NAME, ZoneResolver.class);
        this.logger.info("Using ZoneResolver [" + this.zoneResolver + "]");
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object o) throws Exception {
        final Zone zone = resolveZone(request);
        if (zone == null) {
            if (this.requireZone) {
                this.logger.warn("<preHandle> no zone for " + request.getRequestURI());
                throw new HttpException(HttpServletResponse.SC_NOT_FOUND, "no zone found");
            }
            return true;
        }
        request.setAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE, zone);
        request.setAttribute(ZONE_REQUEST_ATTRIBUTE, createZoneRequest(request, zone));
        return true;
    }

    private HttpServletRequest createZoneRequest(HttpServletRequest request, Zone zone) {
        final String name = HttpRequestUtil.getRequestName(request);
        //noinspection unchecked
        final Map<String, String[]> map = new HashMap<>(request.getParameterMap());
        addAttribute(request, map, ProfileResolver.AUTHENTICATION_KEY);
        addAttribute(request, map, ProfileResolver.AUTHENTICATION_TYPE_KEY);
        return RequestWrapper.create(request, zone.getParameterMap(map, name));
    }

    private void addAttribute(HttpServletRequest request, Map<String, String[]> map, String key) {
        final String s = (String) request.getAttribute(key);
        if (s != null) {
            map.put(key, new String[]{s});
        }
    }

    protected final Zone resolveZone(HttpServletRequest request) {
        if (this.zoneResolver != null) {
            return this.zoneResolver.resolveZone(request);
        }
        return null;
    }

    public void postHandle(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Object o,
            ModelAndView modelAndView) throws Exception {
        // empty
    }

    public void afterCompletion(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // empty
    }
}