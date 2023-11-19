/*
 * FirebugLoggerAdapter.java
 *
 * Created on 06.04.2016 08:20
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.logging;

import java.util.Map;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * @author mdick
 */
public class FirebugLoggerAdapter implements Logger {
    @Override
    public void log(String s) {
        Firebug.log(s);
    }

    @Override
    public void debug(String s) {
        Firebug.debug(s);
    }

    @Override
    public void info(String s) {
        Firebug.info(s);
    }

    @Override
    public void warn(String s) {
        Firebug.warn(s);
    }

    @Override
    public void warn(String s, Throwable t) {
        Firebug.warn(s, t);
    }

    @Override
    public void error(String s) {
        Firebug.error(s);
    }

    @Override
    public void error(String s, Throwable t) {
        Firebug.error(s, t);
    }

    @Override
    public void groupStart(String groupName) {
        Firebug.groupStart(groupName);
    }

    @Override
    public void groupEnd() {
        Firebug.groupEnd();
    }

    @Override
    public void group(String groupName, String... sArray) {
        Firebug.group(groupName, sArray);
    }

    @Override
    public void groupCollapsed(String groupName, String... sArray) {
        Firebug.groupCollapsed(groupName, sArray);
    }

    @Override
    public void logAsGroup(String description, Map<?, ?> map) {
        Firebug.logAsGroup(description, map);
    }
}
