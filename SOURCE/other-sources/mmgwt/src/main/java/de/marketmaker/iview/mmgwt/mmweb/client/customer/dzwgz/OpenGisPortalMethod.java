/*
 * OpenGisPortalMethod.java
 *
 * Created on 29.03.2016 12:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import java.util.Date;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.MD5Util;

/**
 * @author mdick
 */
@SuppressWarnings("Duplicates")
public class OpenGisPortalMethod implements OpenGisPortalCommand {
    static final String KEY = "c2eafee8016c9ebde44f6e752a05d736"; // $NON-NLS$
    static final String PORTAL_BASE_URL = "http://gisweb.vwd.com/gisportal/index.php"; // $NON-NLS$

    private static HandlerRegistration shutdownHandlerRegistration;

    static class GisPortalShutdownHandler implements ShutdownHandler {
        @Override
        public void onShutdown(ShutdownEvent event) {
            Firebug.debug("<OpenGisPortalMethod.GisPortalShutdownHandler.onShutdown>");
            final Frame frame = new Frame(getLogoutLink() + "&logout=1"); // $NON-NLS$
            frame.setSize("1px", "1px"); // $NON-NLS$
            RootPanel.get().add(frame);
        }
    }

    @Override
    public void execute() {
        Firebug.debug("Open GIS Portal Window");
        Window.open(getLink(), "_blank", "resizable=yes"); //$NON-NLS$
        if (shutdownHandlerRegistration == null) {
            Firebug.debug("<OpenGisPortalMethod.execute> adding ShutdownHandler for GisPortal logout");
            shutdownHandlerRegistration = EventBusRegistry.get().addHandler(ShutdownEvent.getType(), new GisPortalShutdownHandler());
        }
    }

    static String getLink() {
        return encodeUrl(PORTAL_BASE_URL, true);
    }

    static String getLogoutLink() {
        final String genoId = SessionData.INSTANCE.getUser().getLogin();
        final String date = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()); // $NON-NLS$
        return PORTAL_BASE_URL + "?option=com_community&view=sso&noredirect=1&genoid=" + // $NON-NLS$
                genoId +
                "&sso=" + // $NON-NLS$
                MD5Util.md5(genoId + ":" + date + ":" + KEY);
    }

    static String encodeUrl(String url, boolean redirect) {
        final String genoId = SessionData.INSTANCE.getUser().getLogin();
        final String date = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()); // $NON-NLS$
        if(!url.contains("?")) { //$NON-NLS$
            url += "?"; //$NON-NLS$
        }
        else {
            url += "&"; //$NON-NLS$
        }
        if(!redirect) {
            url += "noredirect=1&"; //$NON-NLS$
        }
        return url + "genoid=" + genoId + "&sso=" + MD5Util.md5(genoId + ":" + date + ":" + KEY); //$NON-NLS$
    }
}
