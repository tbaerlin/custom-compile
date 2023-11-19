/*
 * Slf4jMDC.java
 *
 * Created on 08.10.13 12:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

/**
 * @author oflege
 */
class Slf4jMDC implements MDC.Provider {
    Slf4jMDC() throws ClassNotFoundException {
        Class.forName("org.slf4j.MDC");
    }

    @Override
    public String get(String s) {
        return org.slf4j.MDC.get(s);
    }

    @Override
    public void put(String key, String value) {
        if (value != null) {
            org.slf4j.MDC.put(key, value);
        }
        else {
            org.slf4j.MDC.remove(key);
        }
    }
}
