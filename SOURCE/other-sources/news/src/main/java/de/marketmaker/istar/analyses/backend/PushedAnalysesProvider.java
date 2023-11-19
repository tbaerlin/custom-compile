/*
 * PushedAnalysesProvider.java
 *
 * Created on 11.05.12 10:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.util.concurrent.Future;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationListener;

import de.marketmaker.istar.feed.connect.FeedConnectionEvent;

/**
 * AnalysesProvider that gets its updates from a feed, i.e., whenever this component is not
 * running, it might miss updates (in contrast to a {@link PassiveAnalysesProvider}, which finds
 * all updates in its incoming directory). To recover missed updates, this component initiates
 * a sync as soon as its feedConnector is connected to the feed source.
 *
 * @author oflege
 */
abstract class PushedAnalysesProvider extends AnalysesProvider implements ApplicationListener<FeedConnectionEvent> {

    private Object connectionEventSource;

    private volatile Future<?> pendingSync;

    public void setConnectionEventSource(Object connectionEventSource) {
        this.connectionEventSource = connectionEventSource;
    }

    @Override
    public void onApplicationEvent(FeedConnectionEvent event) {
        if (this.connectionEventSource == null || this.connectionEventSource == event.getSource()) {
            if (event.isConnected() && (this.pendingSync == null || this.pendingSync.isDone())) {
                this.pendingSync = syncWithBackup(new DateTime().minusDays(366));
            }
        }
    }

}
