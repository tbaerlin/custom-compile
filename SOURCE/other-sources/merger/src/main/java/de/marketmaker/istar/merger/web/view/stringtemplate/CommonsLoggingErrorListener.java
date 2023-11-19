/*
 * CommonsLoggingErrorListener.java
 *
 * Created on 19.06.2006 15:25:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.stringtemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.ErrorType;
import org.stringtemplate.v4.misc.STMessage;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import de.marketmaker.istar.common.featureflags.FeatureFlags;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class CommonsLoggingErrorListener implements STErrorListener {

    private final Logger logger;

    CommonsLoggingErrorListener(String group) {
        this.logger = LoggerFactory.getLogger(STGroup.class.getName() + "$" + group);
    }

    public void compileTimeError(STMessage msg) {
        this.logger.error("<compileTimeError> " + msg);
    }

    public void runTimeError(STMessage msg) {
        if (msg.error == ErrorType.NO_SUCH_PROPERTY) {
            if (FeatureFlags.Flag.PROD.isEnabled()
                    || !(msg.cause instanceof STNoSuchPropertyException)) {
                return;
            }
        }
        this.logger.error("<runTimeError> " + msg);
        StringTemplateView.RUN_TIME_ERRORS.set(msg);
    }

    public void IOError(STMessage msg) {
        this.logger.error("<IOError> " + msg);
    }

    public void internalError(STMessage msg) {
        this.logger.error("<internalError> " + msg);
    }

    public void error(String s) {
        error(s, null);
    }

    public void error(String s, Throwable e) {
        this.logger.error("<error> " + s, e);
    }
}
