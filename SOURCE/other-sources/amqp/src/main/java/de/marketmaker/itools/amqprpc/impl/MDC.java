/*
 * MappedDiagnosticContextSupport.java
 *
 * Created on 08.10.13 11:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import org.apache.commons.logging.LogFactory;

/**
 * Helper for mapped diagnostic contexts offered by different logging frameworks.
 * @author oflege
 */
class MDC {
    interface Provider {
        public String get(String s);

        public void put(String key, String value);
    }

    private static final Provider NULL = new Provider() {
        @Override
        public String get(String s) {
            return null;
        }

        @Override
        public void put(String key, String value) {
        }
    };

    static final Provider INSTANCE;

    static {
        Provider tmp = NULL;
        try {
            tmp = new Slf4jMDC();
        } catch (ClassNotFoundException e) {
            // ignore
        }
        INSTANCE = tmp;
    }

    public static void main(String[] args) {
        INSTANCE.put("foo", "bar");
    }
}
