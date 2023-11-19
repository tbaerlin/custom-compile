/*
 * CachingControllerHandlerAdapter.java
 *
 * Created on 04.11.2008 10:03:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;

/**
 * A HandlerAdapter similar to {@link org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter},
 * but that is aware that a {@link de.marketmaker.istar.merger.web.CachingInterceptor} may be
 * part of the interceptor chain and that a request to handle might already contain a cached
 * result as an attribute.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CacheAwareControllerHandlerAdapter implements HandlerAdapter, Ordered {

    private CachingInterceptor cachingInterceptor;

    private int order = Integer.MAX_VALUE;

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public void setCachingInterceptor(CachingInterceptor cachingInterceptor) {
        this.cachingInterceptor = cachingInterceptor;
    }

    public boolean supports(Object handler) {
        return (handler instanceof Controller);
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
            Object handler)
            throws Exception {
        final Object cached = request.getAttribute(CachingInterceptor.CACHED_MUV_ATTRIBUTE);
        if (cached != null) {
            return (ModelAndView) cached;
        }
        return ((Controller) handler).handleRequest(request, response);
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        if (this.cachingInterceptor != null) {
            final long cachedLastModified = this.cachingInterceptor.getLastModified(request);
            if (cachedLastModified != -1L) {
                return cachedLastModified;
            }
        }
        if (handler instanceof LastModified) {
            return ((LastModified) handler).getLastModified(request);
        }
        return -1L;
    }

}

