/*
 * LoggingUncaughtExceptionHandler.java
 *
 * Created on 21.11.2006 09:08:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * An uncaught exception handler that logs uncaught exceptions.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler,
    InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile Thread.UncaughtExceptionHandler oldHandler;

    private boolean forwardToPreviousHandler = false;

    public void setForwardToPreviousHandler(boolean forwardToPreviousHandler) {
        this.forwardToPreviousHandler = forwardToPreviousHandler;
    }

    public void afterPropertiesSet() throws Exception {
        this.oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread t, Throwable e) {
        this.logger.error("<uncaughtException> in thread '" + t.getName(), e);
        if (!this.forwardToPreviousHandler) {
            return;
        }
        if (this.oldHandler == null) {
            e.printStackTrace(System.err);
        }
        else {
            this.oldHandler.uncaughtException(t, e);
        }
    }
}
