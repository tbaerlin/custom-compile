/*
 * OkServletContextListener.java
 *
 * Created on 06.08.15 10:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.log.JmxLog;

/**
 * Adds an __OK__ entry to the logfile
 * @author oflege
 */
public class OkServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LoggerFactory.getLogger(getClass()).info(JmxLog.OK_DEFAULT);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // empty
    }
}
