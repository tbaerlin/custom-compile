/*
 * ZoneHandlerMapping.java
 *
 * Created on 19.08.2009 09:00:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * A HandlerMapping that uses HandlerInterceptors as provided by
 * {@link de.marketmaker.istar.merger.web.Zone#getInterceptors(String)} -- if no interceptors
 * are defined by the zone, this object's default interceptors from the application
 * context will be used.<br>
 * For requests w/o an associated Zone, this mapping will always return a null handler.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ZoneHandlerMapping extends SimpleUrlHandlerMapping {

    @Override
    protected HandlerExecutionChain getHandlerExecutionChain(Object o,
            HttpServletRequest request) {
        final Zone z = getZone(request);
        final String name = HttpRequestUtil.getRequestName(request);

        final HandlerInterceptor[] interceptors = z.getInterceptors(name);
        if (interceptors != null) {
            return new HandlerExecutionChain(o, interceptors);
        }
        return super.getHandlerExecutionChain(o, request);
    }

    private Zone getZone(HttpServletRequest request) {
        return (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
    }

    @Override
    protected Object lookupHandler(String s, HttpServletRequest request) throws Exception {
        final Zone z = getZone(request);
        if (z == null) {
            return null;
        }
        return super.lookupHandler(s, request);
    }
}
