/*
 * DelegatingHandlerInterceptor.java
 *
 * Created on 06.10.2014 12:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;


import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mdick
 */
public class DelegatingHandlerInterceptor extends HandlerInterceptorAdapter {
    private AsyncHandlerInterceptor delegate;

    public DelegatingHandlerInterceptor() {
        super();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(this.delegate != null) {
            return this.delegate.preHandle(request, response, handler);
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(this.delegate != null) {
            this.delegate.postHandle(request, response, handler, modelAndView);
            return;
        }
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if(this.delegate != null) {
            this.delegate.afterCompletion(request, response, handler, ex);
            return;
        }
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(this.delegate != null) {
            this.delegate.afterConcurrentHandlingStarted(request, response, handler);
            return;
        }
        super.afterConcurrentHandlingStarted(request, response, handler);
    }

    public void setDelegate(AsyncHandlerInterceptor delegate) {
        this.delegate = delegate;
    }
}
