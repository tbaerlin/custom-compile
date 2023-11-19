/*
 * DmxmlFacade.java
 *
 * Created on 06.12.13 09:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.fusion.dmxml;

import javax.servlet.http.HttpServletRequest;

/**
 * @author oflege
 */
public interface DmxmlFacade {

    /**
     * Evaluates the dmxml request and assigns the result to each of the blocks in the request.
     * @param request to be evaluated
     * @return true iff evaluation succeeded.
     */
    boolean evaluate(DmxmlRequest request);

    /**
     * Evaluates the dmxml request and assigns the result to each of the blocks in the request.
     * Implementations may require the <tt>servletRequest</tt> parameter, e.g., if the evaluation
     * relies on request forwarding, a tomcat server will only forward requests that have
     * certain (internal) parameters/attributes. In order to supply those values, it is necessary
     * to wrap the original servlet request.
     *
     * @param servletRequest context the original http request that triggered the evaluation
     * @param request to be evaluated
     * @return true iff evaluation succeeded.
     */
    boolean evaluate(HttpServletRequest servletRequest, DmxmlRequest request);
}
