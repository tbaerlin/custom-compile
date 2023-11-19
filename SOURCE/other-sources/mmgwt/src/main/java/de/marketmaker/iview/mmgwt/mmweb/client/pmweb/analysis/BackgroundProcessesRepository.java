/*
 * BackgroundProcessesRepository.java
 *
 * Created on 25.06.2015 07:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Keeps EvaluationController and AnalysisController free from registering and unregistering as interested parties
 * of privacy mode changes.
 *
 * @author mdick
 */
public class BackgroundProcessesRepository implements PrivacyMode.InterestedParty {
    public static final BackgroundProcessesRepository INSTANCE = new BackgroundProcessesRepository();

    private final Set<BackgroundProcess> nonePrivacyModeBackgroundProcesses = new HashSet<>();
    private final Set<BackgroundProcess> backgroundProcesses = new HashSet<>();

    public BackgroundProcessesRepository() {
        PrivacyMode.subscribe(this);
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive, PrivacyMode.StateChangeProcessedCallback processed) {
        try {
            cleanUpDeletedOrCanceledBackgroundProcesses();

            if (privacyModeActive) {
                // Background processes started in non-privacy mode should kept running, but should not show a
                // notification in privacy mode.
                copy(this.backgroundProcesses, this.nonePrivacyModeBackgroundProcesses);
                for (BackgroundProcess backgroundProcess : this.nonePrivacyModeBackgroundProcesses) {
                    backgroundProcess.removeNotification();
                }
            }
            else {
                // Cancel all running background processes if leaving privacy mode, because background processes started
                // in privacy mode should not be available in non-privacy mode.
                cancelAll();
                // After leaving privacy mode, these background processes started in non-privacy mode should be
                // available again.
                copy(this.nonePrivacyModeBackgroundProcesses, this.backgroundProcesses);
                for (BackgroundProcess backgroundProcess : this.backgroundProcesses) {
                    backgroundProcess.addHiddenNotification();
                }
            }
        }
        finally {
            processed.privacyModeStateChangeProcessed(this);
        }
    }

    public boolean add(BackgroundProcess backgroundProcess) {
        cleanUpDeletedOrCanceledBackgroundProcesses();
        return this.backgroundProcesses.add(backgroundProcess);
    }

    private static void copy(Set<BackgroundProcess> source, Set<BackgroundProcess> destination) {
        destination.clear();
        destination.addAll(source);
        source.clear();
    }

    private void cancelAll() {
        for (BackgroundProcess backgroundProcess : this.backgroundProcesses) {
            if(!backgroundProcess.isCanceled()) {
                backgroundProcess.cancel();
            }
        }
    }

    private void cleanUpDeletedOrCanceledBackgroundProcesses() {
        // do some basic cleanup (necessary, because there is no event that tells us if the notification has been closed
        // by the user or by the program just while changing the privacy state.

        //use this construct to avoid
        final Iterator<BackgroundProcess> iterator = this.backgroundProcesses.iterator();
        while(iterator.hasNext()) {
            final BackgroundProcess backgroundProcess = iterator.next();
            if(backgroundProcess.isNotificationRemoved() || backgroundProcess.isCanceled()) {
                iterator.remove();
            }
        }
    }
}
