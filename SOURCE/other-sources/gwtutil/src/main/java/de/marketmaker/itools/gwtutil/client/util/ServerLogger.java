/*
 * ServerLogger.java
 *
 * Created on 3/13/15 11:46 AM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

/**
 * @author kmilyut
 */
public class ServerLogger {

    private static final Logger remoteLogger = Logger.getLogger("gwtclient.logger");  // $NON-NLS-0$

    public static void log(String msg, Throwable throwable) {
        remoteLogger.log(Level.SEVERE, prependContext(msg), throwable);
    }

    public static void log(String msg) {
        remoteLogger.log(Level.INFO, prependContext(msg));
    }

    private static String prependContext(String msg) {
        return "#" + History.getToken() + ", ua=" + Window.Navigator.getUserAgent().toLowerCase() + " " + msg; // $NON-NLS$
    }
}
