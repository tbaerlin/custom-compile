/*
 * StopWatchInterceptor.java
 *
 * Created on 11.02.2009 12:07:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.util.StopWatch;

import de.marketmaker.istar.merger.web.easytrade.StopWatchHolder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StopWatchInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse,
            Object o) throws Exception {
        StopWatchHolder.setStopWatch(new StopWatch());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse,
            Object o, Exception e) throws Exception {
        StopWatchHolder.setStopWatch(null);
    }
}
