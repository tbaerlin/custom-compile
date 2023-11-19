/*
 * DebugUtil.java
 *
 * Created on 08.05.2008 08:45:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.ServerLogger;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Version;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

import static de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController.INSTANCE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DebugUtil {
    public static boolean DEBUG = false;

    public static void logToServer(String msg, Throwable throwable) {
        ServerLogger.log(prependContext(msg), throwable);
    }

    public static void logToServer(String msg) {
        ServerLogger.log(prependContext(msg));
    }

    private static String prependContext(String msg) {
        return "uid=" + getUserId() + ", ctx=" + getContextPath() + ", v=" + Version.INSTANCE.build() + " " + msg; // $NON-NLS$
    }

    private static String getContextPath() {
        return (INSTANCE != null && INSTANCE.contextPath != null) ? INSTANCE.contextPath : "?";
    }

    private static String getUserId() {
        final User user = SessionData.INSTANCE.getUser();
        return (user != null) ? user.getUid() : "?";
    }

    public static void displayServerError(final DmxmlContext.Block block) {
        final ErrorType error = block.getError();
        if (error == null) {
            Notifications.add(I18n.I.serverError(), I18n.I.contactCustomerService());
            return;
        }
        Firebug.warn(error.getDescription());
        if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            Notifications.add(block.getKey(), new ScrollPanel(new Label(error.getDescription())));
        }
        else {
            Notifications.add(I18n.I.serverError(), I18n.I.contactCustomerService());
        }
    }

    public static void showDeveloperNotification(String header) {
        showDeveloperNotification(header, null);
    }

    public static void showDeveloperNotification(String header, Throwable t) {
        if (!SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            return;
        }
        final FlowPanel panel = new FlowPanel();
        if (t != null) {
            addDeveloperException(panel, "", t);
        }
        Notifications.add("Developer info: " + header, panel); // $NON-NLS$
    }

    private static void addDeveloperException(FlowPanel panel, String prefix, Throwable t) {
        if (!(t instanceof UmbrellaException)) {
            panel.add(new Label(prefix + t.getClass().getSimpleName() + ": " + t.getLocalizedMessage()));
        }
        if (t.getCause() != null) {
            addDeveloperException(panel, "Caused by: ", t.getCause()); // $NON-NLS$
        }
    }

}
