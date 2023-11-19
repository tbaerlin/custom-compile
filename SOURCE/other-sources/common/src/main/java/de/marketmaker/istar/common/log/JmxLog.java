/*
 * JmxLog.java
 *
 * Created on 08.08.2005 10:08:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * The purpose of this class is to provide a means for writing something into
 * the application's log file. This can be used, for example, to log some message
 * for nagios that prevents it from recognizing any ERROR lines prior to the line
 * just logged as problems. Manipulting the logfile from outside the application
 * is not a good thing and does not work reliably.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class JmxLog {

    public static final String OK_DEFAULT = "__OK__";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String okDefault = OK_DEFAULT;

    public void setOkDefault(String okDefault) {
        this.okDefault = okDefault;
    }

    @ManagedOperation
    public void writeOk() {
        writeLog(OK_DEFAULT);
    }

        @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "text", description = "text to log or empty for default")
    })
    public void writeLog(String s) {
        final String msg;
        if (StringUtils.hasText(s)) {
            if (StringUtils.hasText(this.okDefault) && !s.contains(this.okDefault)) {
                msg = this.okDefault + " " + s;
            }
            else {
                msg = s;
            }
        }
        else {
            msg = this.okDefault;
        }

        if (this.logger.isInfoEnabled()) {
            this.logger.info(msg);
        }
        else if (this.logger.isWarnEnabled()) {
            this.logger.warn("<writeLog> failed, info level not allowed");
        }
        else if (this.logger.isErrorEnabled()) {
            this.logger.error("<writeLog> failed, info level not allowed");
        }
    }

}
