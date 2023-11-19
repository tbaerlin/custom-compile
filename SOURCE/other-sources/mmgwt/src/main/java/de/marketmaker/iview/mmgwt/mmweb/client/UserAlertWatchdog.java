/*
 * UserAlertWatchdog.java
 *
 * Created on 07.08.2008 11:01:28
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserAlertWatchdog implements RequestCompletedHandler {

    private String previous = null;

    public UserAlertWatchdog(HandlerManager eventBus) {
        eventBus.addHandler(RequestCompletedEvent.getType(), this);
    }

    public void onRequestCompleted(RequestCompletedEvent event) {
        if (event.isSuccessful()) {
            onAfterReceive(event.getResponse());
        }
    }

    private void onAfterReceive(MmwebResponse response) {
        final String pending = response.getProperty(AppConfig.PROP_KEY_PENDING_ALERTS);
        if (changed(pending)) {
            Firebug.warn("UserAlertWatchdog current=" + pending); // $NON-NLS-0$
            if(SessionData.isAsDesign()) {
                addAlertNotification(pending);
            }
            AbstractMainController.INSTANCE.updateLimitsIcon(pending);
            this.previous = pending;
        }
    }

    private boolean changed(String current) {
        if (this.previous == null) {
            return current != null;
        }
        return !this.previous.equals(current);
    }

    private void addAlertNotification(String pending) {
        if(pending == null) {
            return;
        }

        final int i = toInt(pending);
        final Label label = new Label(i > 0 ? I18n.I.limitsTriggered(i) : I18n.I.limitTriggered());
        label.addClickHandler(event -> PlaceUtil.goTo("B_L"));  // $NON-NLS$
        label.addStyleName("mm-link");
        Notifications.add(I18n.I.limit(), label);
    }

    private int toInt(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch(Exception e) {
            return 0;
        }
    }
}
