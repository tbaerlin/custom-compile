/*
 * StopWatchHandlerInterceptor.java
 *
 * Created on 06.03.2008 14:30:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.util.StopWatch;

import de.marketmaker.istar.merger.web.easytrade.StopWatchHolder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StopWatchHandlerInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Object o) throws Exception {
        StopWatchHolder.setStopWatch(new StopWatch());
        return true;
    }

    public void afterCompletion(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        StopWatchHolder.setStopWatch(null);
    }
}
