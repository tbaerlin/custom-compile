/*
 * ServletRequestHolder.java
 *
 * Created on 06.03.2008 12:36:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class ServletRequestHolder {
    private final static ThreadLocal<HttpServletRequest> HOLDER = new ThreadLocal<>();

    /**
     * Associate the given HttpServletRequest with the current thread.
     * @param httpServletRequest the current HttpServletRequest, or <code>null</code> to reset
     * the thread-bound context
     */
    public static void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            HOLDER.set(httpServletRequest);
        }
        else {
            HOLDER.remove();
        }
    }

    /**
     * Return the HttpServletRequest associated with the current thread,
     * if any.
     * @return the current HttpServletRequest, or <code>null</code> if none
     */
    public static HttpServletRequest getHttpServletRequest() {
        return HOLDER.get();
    }
}
