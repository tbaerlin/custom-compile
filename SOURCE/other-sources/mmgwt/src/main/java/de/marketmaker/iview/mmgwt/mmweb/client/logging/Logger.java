/*
 * Logger.java
 *
 * Created on 06.04.2016 08:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.logging;

import java.util.Map;

/**
 * General logger interface, usable for dependency injection.
 * @author mdick
 */
@SuppressWarnings("unused")
public interface Logger {
    void log(String s);

    void debug(String s);

    void info(String s);

    void warn(String s);

    void warn(String s, Throwable t);

    void error(String s);

    void error(String s, Throwable t);

    void groupStart(String groupName);

    void groupEnd();

    void group(String groupName, String... sArray);

    void groupCollapsed(String groupName, String... sArray);

    void logAsGroup(String description, Map<?, ?> map);
}
