package de.marketmaker.iview.pmxml.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 01.02.13 15:18
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public enum PmExchangeDataLogger {
    INFO {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void log(Object o) {
            this.logger.info(String.valueOf(o));
        }
    },
    DEBUG {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void log(Object o) {
            this.logger.debug(String.valueOf(o));
        }
    },
    WARN {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void log(Object o) {
            this.logger.warn(String.valueOf(o));
        }
    },
    TRACE {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void log(Object o) {
            this.logger.trace(String.valueOf(o));
        }
    },
    FATAL {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void log(Object o) {
            this.logger.error(String.valueOf(o));
        }
    },
    SILENT {
        @Override
        public void log(Object o) {
            // empty
        }
    };

    public abstract void log(Object o);
}
