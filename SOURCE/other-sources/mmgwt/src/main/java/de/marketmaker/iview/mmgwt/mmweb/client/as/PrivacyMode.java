/*
 * PrivacyMode.java
 *
 * Created on 05.05.2015 15:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mdick
 */
public final class PrivacyMode {
    static final PrivacyMode INSTANCE = GWT.create(PrivacyMode.class);

    private final HashSet<InterestedParty> interestedParties = new HashSet<>();
    private final HashSet<String> allowedObjectIds = new HashSet<>();
    private boolean active;

    private PrivacyMode() {
        //do nothing
    }

    void setActive(boolean active, final Command finished) {
        if(this.active == active) {
            finished.execute();
            return;
        }
        this.active = active;

        final StateChangeProcessedCallback processed = new StateChangeProcessedCallback() {
            final HashSet<InterestedParty> callbackParties = new HashSet<>(PrivacyMode.this.interestedParties);

            @Override
            public void privacyModeStateChangeProcessed(InterestedParty party) {
                this.callbackParties.remove(party);
                if(this.callbackParties.isEmpty()) {
                    finished.execute();
                }
            }
        };

        for (InterestedParty interestedParty : this.interestedParties) {
            interestedParty.privacyModeStateChanged(active, processed);
        }
    }

    private void doSubscribe(InterestedParty party) {
        if(party == null) {
            return;
        }
        this.interestedParties.add(party);
    }

    private void doUnsubscribe(InterestedParty party) {
        if(party == null) {
            return;
        }
        this.interestedParties.remove(party);
    }

    void setAllowedObjectIds(Set<String> objectIds) {
        this.allowedObjectIds.clear();
        if(objectIds == null) {
            return;
        }
        this.allowedObjectIds.addAll(objectIds);
    }

    private boolean doIsObjectIdAllowed(String objectId) {
        return !this.active || this.allowedObjectIds.contains(objectId);
    }

    private Set<String> doGetObjectIdAllowed() {
        return Collections.unmodifiableSet(this.allowedObjectIds);
    }

    void resetAllowedObjectIds() {
        this.allowedObjectIds.clear();
    }

    public static boolean isActive() {
        return INSTANCE.active;
    }

    public static Set<String> getAllowedObjectIds() {
        return INSTANCE.doGetObjectIdAllowed();
    }

    public static boolean isObjectIdAllowed(String objectId) {
        return INSTANCE.doIsObjectIdAllowed(objectId);
    }

    public static void subscribe(InterestedParty party) {
        INSTANCE.doSubscribe(party);
    }

    public static void unsubscribe(InterestedParty party) {
        INSTANCE.doUnsubscribe(party);
    }

    public static int getSubscriptionsCount() {
        return INSTANCE.interestedParties.size();
    }

    /**
     * Parties interested in a privacy mode change must call {@linkplain de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode.StateChangeProcessedCallback#privacyModeStateChangeProcessed(InterestedParty)}
     * if they finished their processing necessary for a privacy mode change.
     * If the call is missing, the privacy mode change will never finish.
     */
    public interface InterestedParty {
        void privacyModeStateChanged(boolean privacyModeActive, StateChangeProcessedCallback processed);
    }

    /**
     * Parties interested in a privacy mode change must call the method of this interface to finish their processing
     * of the privacy mode change. If the call is missing, the privacy mode change will never finish.
     */
    public interface StateChangeProcessedCallback {
        void privacyModeStateChangeProcessed(InterestedParty party);
    }
}
