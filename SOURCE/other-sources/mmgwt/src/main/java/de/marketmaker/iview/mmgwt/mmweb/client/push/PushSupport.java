package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Coordinates pushing.
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 * @author oflege
 */
class PushSupport implements PushActivationHandler, RequestCompletedHandler, PricesUpdatedHandler, PlaceChangeHandler {

    private static final int AUTO_RELOAD_INTERVAL_MILLIS = 60000; // 60s

    private static final int TIME_TO_CLEAR_MILLIS = 1500; // 1.5s

    private HashSet<String> registeredVwdCodes = new HashSet<>();

    private ArrayList<DmxmlContext.Block> blocksToReload = new ArrayList<>();

    private ArrayList<AsyncCallback<ResponseType>> reloadCallbacks = new ArrayList<>();

    private final AbstractReceiver receiver;

    private HandlerRegistration regCompleted;

    private HandlerRegistration regUpdated;

    private final Timer autoReloadTimer = new Timer() {
        public void run() {
            autoReload();
        }
    };

    /**
     * Makes sure raising/falling prices with a resp. background color get a "normal" color
     * again after some time even if no push updates are received.
     */
    private final Timer clearTimer = new Timer() {
        public void run() {
            clearItems();
        }
    };

    // used to reload blocks that are fully pushable
    private final DmxmlContext context = new DmxmlContext();

    private boolean active = false;

    private PushBrowserTitle pushBrowserTitle;

    PushSupport(SessionData sessionData) {
        this.receiver = AbstractReceiver.create(this, sessionData);
        EventBusRegistry.get().addHandler(PushActivationEvent.getType(), this);
        this.pushBrowserTitle = new PushBrowserTitle();
    }

    @Override
    public void onPushActivated(PushActivationEvent event) {
        if (event.isActive()) {
            activate();
        }
        else {
            deactivate();
        }
    }

    boolean isActive() {
        return this.active;
    }

    private void activate() {
        Firebug.log("activate push");
        this.active = true;
        this.regCompleted = EventBusRegistry.get().addHandler(RequestCompletedEvent.getType(), this);
        this.regUpdated = EventBusRegistry.get().addHandler(PricesUpdatedEvent.getType(), this);
        updatePushedKeys();
    }

    private void deactivate() {
        Firebug.log("deactivate push");
        this.active = false;
        this.pushBrowserTitle.onPushStop();
        this.autoReloadTimer.cancel();
        this.clearTimer.cancel();
        clearItems();
        this.regCompleted.removeHandler();
        this.regUpdated.removeHandler();
        stopPush();
    }

    private void stopPush() {
        this.registeredVwdCodes.clear();
        this.receiver.stopPush();
        PriceStore.INSTANCE.removePreviousPrices();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        // auto reload should not interfere with new page loading
        this.autoReloadTimer.cancel();
    }

    @SuppressWarnings("Convert2MethodRef")
    public void onRequestCompleted(RequestCompletedEvent event) {
        Scheduler.get().scheduleDeferred((Command) () -> updatePushedKeys());
    }

    private void updatePushedKeys() {
        final PushRegisterEvent event = new PushRegisterEvent();
        EventBusRegistry.get().fireEvent(event);
        updatePushedKeys(event.getVwdcodes());
        this.blocksToReload = event.getBlocksToReload();
        this.reloadCallbacks = event.getReloadCallbacks();
        if (!this.reloadCallbacks.isEmpty()) {
            this.autoReloadTimer.schedule(AUTO_RELOAD_INTERVAL_MILLIS);
        }
    }

    private void updatePushedKeys(HashSet<String> codesToBePushed) {
        if (this.registeredVwdCodes.equals(codesToBePushed)) {
            return;
        }

        if (codesToBePushed.isEmpty()) {
            stopPush();
            return;
        }

        final HashSet<String> toRegister = new HashSet<>(codesToBePushed);
        toRegister.removeAll(this.registeredVwdCodes);
        Firebug.log("toRegister: " + toRegister.toString()); // $NON-NLS-0$

        this.registeredVwdCodes.removeAll(codesToBePushed);
        Firebug.log("toUnregister: " + this.registeredVwdCodes.toString()); // $NON-NLS-0$

        final PushChangeRequest request = createChangeRequest(toRegister, this.registeredVwdCodes);

        this.registeredVwdCodes = codesToBePushed;
        Firebug.log("pushed: " + codesToBePushed.toString()); // $NON-NLS-0$

        this.receiver.changePush(request);
    }

    private PushChangeRequest createChangeRequest(HashSet<String> toRegister,
            HashSet<String> toUnregister) {
        final PushChangeRequest request = new PushChangeRequest();
        request.setToRegister(toRegister);
        request.setToUnregister(toUnregister);
        return request;
    }

    PushChangeRequest createChangeRequestToRecreateSession() {
        return createChangeRequest(this.registeredVwdCodes, null);
    }

    private void autoReload() {
        Firebug.log("Autoreload"); // $NON-NLS-0$
        this.context.withBlocks(this.blocksToReload).issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable throwable) {
                for (AsyncCallback<ResponseType> callback : reloadCallbacks) {
                    callback.onFailure(throwable);
                }
            }

            public void onSuccess(ResponseType responseType) {
                for (AsyncCallback<ResponseType> callback : reloadCallbacks) {
                    callback.onSuccess(responseType);
                }
            }
        });
    }

    private void clearItems() {
        PriceStore.INSTANCE.onPush(null);
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (event.isPushedUpdate()) {
            this.clearTimer.cancel();
            this.clearTimer.schedule(TIME_TO_CLEAR_MILLIS);
        }
    }
}