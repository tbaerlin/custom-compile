/*
 * Zone.java
 *
 * Created on 14.08.2006 16:59:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategies;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import org.springframework.web.servlet.HandlerInterceptor;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Zone {
    public static final String CONTEXT_ATTRIBUTE = Zone.class.getName() + ".CONTEXT";

    public static final String ATOM_CONTEXT_ATTRIBUTE = Zone.class.getName() + ".ATOM_CONTEXT";

    /**
     * Returns a map of parameters in which zone specific parameters are merged with
     * requestParameters.
     * @param requestParameters parameters that are not zone specific
     * @param name used to identify a particular set of zone specific parameters
     */
    Map<String, String[]> getParameterMap(Map<String, String[]> requestParameters, String name);

    /**
     * Returns context parameters from the zone, which are made available in the model.
     * @param name identifies zone member, use null for global context objects
     * @return context parameters
     */
    Map<String, Object> getContextMap(String name);

    /**
     * Returns this zone's name
     * @return name
     */
    String getName();

    /**
     * Returns this zone's template base (directory where this zone's templates are stored).
     * Usually, this is the zone's name, but zones that extend others might want to use
     * their parent's name as template base
     * @return template base
     */
    String getTemplateBase();

    /**
     * Returns interceptors defined for the zone member with the given name
     * @param name identifies zone member
     * @return interceptors or null if undefined
     */
    HandlerInterceptor[] getInterceptors(String name);

    /**
     * Creates a request context for this Zone which includes the given profile
     * @param request
     * @param p profile
     * @return RequestContext
     */
    RequestContext getRequestContext(HttpServletRequest request, Profile p);

    /**
     * @return the error page for the given request and http error code
     */
    ErrorPage getErrorPage(HttpServletRequest request, int errorCode, String errorMessage);

    /**
     * @return the configured MarketStrategy
     */
    MarketStrategy getMarketStrategy();
}
