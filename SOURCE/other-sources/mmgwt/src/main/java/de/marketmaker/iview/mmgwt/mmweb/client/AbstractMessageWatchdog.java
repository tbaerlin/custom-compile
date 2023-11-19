/*
 * AbstractMessageWatchdog.java
 *
 * Created on 19.08.2009 13:34:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractMessageWatchdog implements RequestCompletedHandler {
    private boolean showing = false;

    protected abstract String getPropertyKey();

    protected abstract String getTitle();

    protected abstract String getMessage();

    protected abstract String getDirectAccessItem();

    protected AbstractMessageWatchdog(HandlerManager eventBus) {
        eventBus.addHandler(RequestCompletedEvent.getType(), this);
    }

    public void onRequestCompleted(RequestCompletedEvent event) {
        if (event.isSuccessful()) {
            onAfterReceive(event.getResponse());
        }
    }

    private void onAfterReceive(MmwebResponse response) {
        final String key = getPropertyKey();
        final String last = SessionData.INSTANCE.getUser().getAppConfig().getProperty(key);
        final String current = response.getProperty(key);
        if (current != null && !current.equals(last)) {
            SessionData.INSTANCE.getUser().getAppConfig().addProperty(key, current);
            if (last != null) {
                showNewMessageDialog();
            }
        }
    }

    protected void showNewMessageDialog() {
        if (this.showing) {
            return;
        }

        Dialog.confirm(getTitle(), "dialog-info", getMessage(), new Command() { // $NON-NLS$
            @Override
            public void execute() {
                PlaceUtil.goTo(getDirectAccessItem());
            }
        }).withCloseCommand(new Command() {
            @Override
            public void execute() {
                showing = false;
            }
        });
    }
}
