/*
 * LogbackShutdownListener.java
 *
 * Created on 18.11.13 17:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener to shut down the log system when the context is destroyed.
 * @author oflege
 */
public class LogbackShutdownListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }
}
