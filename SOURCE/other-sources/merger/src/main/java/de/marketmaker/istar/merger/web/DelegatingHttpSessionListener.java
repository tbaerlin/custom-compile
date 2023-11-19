package de.marketmaker.istar.merger.web;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 24.01.13 15:29
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class DelegatingHttpSessionListener implements HttpSessionListener, ServletContextListener {

    private volatile List<HttpSessionListener> listeners = new ArrayList<>();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        initListeners(servletContextEvent.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    private void initListeners(ServletContext servletContext) {
        final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        final Map<String, HttpSessionListener> beans = context.getBeansOfType(HttpSessionListener.class);
        this.listeners.addAll(beans.values());
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        for (HttpSessionListener listener : listeners) {
            listener.sessionCreated(httpSessionEvent);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        for (HttpSessionListener listener : listeners) {
            listener.sessionDestroyed(httpSessionEvent);
        }
    }
}
