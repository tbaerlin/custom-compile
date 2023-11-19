package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadHandler;

/**
 * Created on 02.09.13 11:15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

/**
 * If you want to save and load data (which is not represented in the url)
 * in a historyThread, say hello to ThreadStateSupport!
 * <p/>
 * Use this class to save/load e.g. a controllers custom preferences
 * that should be thread-based-persistent during thread changes.
 * The save and load methods are called BEFORE {@link de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler}
 * onPlaceChange() gets called in implication of the fired PlaceChangeEvent.
 * So it's possible to save and load additional information (compared to the url-params)
 * before anything (controller, snippets,...) is triggered by the PlaceChangeEvent.
 */

public class ThreadStateSupport implements HistoryThreadHandler {

    private final ThreadStateHandler handler;
    private final HistoryThreadManager htm;
    private final HandlerRegistration registration;

    public ThreadStateSupport(final ThreadStateHandler handler) {
        this(handler, true);
    }

    public ThreadStateSupport(final ThreadStateHandler handler, boolean loadImmediately) {
        this.registration = EventBusRegistry.get().addHandler(HistoryThreadEvent.getType(), this);
        this.handler = handler;
        this.htm = AbstractMainController.INSTANCE.getHistoryThreadManager();
        if (loadImmediately) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    htm.loadState(htm.getActiveThreadHistoryItem(), handler);
                }
            });
        }
    }

    @Override
    public void onHistoryThreadChange(HistoryThreadEvent event) {
        final HistoryThreadEvent.Reason reason = event.getReason();

        // Do not save state for reason DELETE, because delete will explicitly fire SWITCH_TO right after the deletion
        // and so we will save the state twice.
        if(reason == HistoryThreadEvent.Reason.DELETE) {
            return;
        }

        //if privacy mode is left do not save state but load state
        if (reason != HistoryThreadEvent.Reason.REPLACED_ALL && this.htm.getPreviousThread() != null && this.htm.getPreviousThread() != this.htm.getActiveThread()) {
            this.htm.saveState(this.htm.getPreviousThreadHistoryItem(), this.handler);
        }
        this.htm.loadState(this.htm.getActiveThreadHistoryItem(), this.handler);
    }

    public void privacyModeStateChanged(boolean privacyModeActive) {
        // It does not make sense to save the state if the privacy mode is left, because those threads will be thrown away.
        if(privacyModeActive) {
            this.htm.saveState(this.htm.getActiveThreadHistoryItem(), this.handler);
        }
    }

    public void unregister() {
        this.registration.removeHandler();
    }

}