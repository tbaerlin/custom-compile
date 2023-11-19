package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.user.client.Timer;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * Created on 15.05.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *         This is a fallback if Comet/Websocket doesn't work as expected or if an pm-event didn't arrive the tomcat-webapp.
 *         It has three polling-intervals. One would use the monitoring-interval first. If there was no update in the
 *         defined time range ( set via {@link #setMillis()} ), the PullHandler turns into the shorter pull-interval.
 *         If no push connection could be established at all, the fallbackInterval is used to get the evaluation's progress.
 */

public class PullHandlerFallback implements PullHandler {
    private Integer pullMonitoringInterval = null; //wide interval to monitor if there's some ongoing evaluation
    private Integer pullInterval = null; //smaller interval to pull status if monitor deceted missing pm events
    private Integer fallbackInterval = null; //very small interval to simulate push events because of connectitity issues
    private final static int BUFFER = 500;
    private Timer timer;
    private final PmAsyncManager manager;
    private int currentInterval;
    private long lastMillis = 0;

    public PullHandlerFallback(final PmAsyncManager manager) {
        this.manager = manager;
        this.pullMonitoringInterval = Integer.valueOf(SessionData.INSTANCE.getClientProperty("pullMonitoringInterval")); // $NON-NLS$
        this.pullInterval = Integer.valueOf(SessionData.INSTANCE.getClientProperty("pullInterval")); // $NON-NLS$
        this.fallbackInterval = Integer.valueOf(SessionData.INSTANCE.getClientProperty("fallbackInterval")); // $NON-NLS$
    }

    @Override
    public void startMonitoring() {
        if (isMonitoring()) {
            return;
        }
        this.timer = createMonitoringTimer();
        Firebug.debug("PullHandler <startMonitoring> check every " + this.currentInterval + " millis.");
    }

    private boolean isMonitoring() {
        return this.currentInterval == this.pullMonitoringInterval && this.timer != null && this.timer.isRunning();
    }

    private boolean isPulling() {
        return (this.currentInterval == this.pullInterval || this.currentInterval == this.fallbackInterval)
                && this.timer != null && this.timer.isRunning();
    }

    @Override
    public void startAsFallback() {
        this.timer = createFallbackTimer();
        Firebug.debug("PullHandler <startAsFallback> pull status every " + this.currentInterval + " millis.");
    }

    @Override
    public void startPull() {
        if (isPulling()) {
            return;
        }
        this.timer = createPullTimer();
        Firebug.debug("PullHandler <startPull> pull status every " + this.currentInterval + " millis.");
    }

    private void checkStatusAndPull() {
        Firebug.debug("PullHandler <checkStatusAndPull> hasRunningHandles: " + this.manager.hasRunningHandles());
        final long diff = System.currentTimeMillis() - this.lastMillis;
        Firebug.info("Diff " + diff);
        if (!this.manager.hasRunningHandles()) {
            this.manager.closeSessionIfPossible("PullHandlerFallback.checkStatusAndPull");   // $NON-NLS$
            return;
        }
        if ((diff + BUFFER) > this.currentInterval) {
            this.manager.pullStatus();
        }
    }

    private void checkStatus() {
        Firebug.debug("PullHandler <checkStatus> hasRunningHandles: " + this.manager.hasRunningHandles());
        final long diff = System.currentTimeMillis() - this.lastMillis;
        if (this.manager.hasRunningHandles() && (diff + BUFFER) > this.currentInterval) {
            startPull();
            checkStatusAndPull();
        }
    }

    @Override
    public void setMillis() {
        this.lastMillis = System.currentTimeMillis();
        if (!this.manager.hasRunningHandles() && !runsAsFallback()) {
            Firebug.info("PullHandlerFallback <setMillis> calling startMonitoring");
            startMonitoring();
        }
    }

    private boolean runsAsFallback() {
        return this.currentInterval == this.fallbackInterval && this.timer != null && this.timer.isRunning();
    }

    private Timer createFallbackTimer() {
        return createPullTimer(this.fallbackInterval);
    }

    private Timer createPullTimer(Integer interval) {
        cancelTimer();
        this.currentInterval = interval;
        final Timer t = new Timer() {
            @Override
            public void run() {
                checkStatusAndPull();
            }
        };
        t.scheduleRepeating(this.currentInterval);
        return t;
    }

    private Timer createPullTimer() {
        return createPullTimer(this.pullInterval);
    }

    private Timer createMonitoringTimer() {
        cancelTimer();
        this.currentInterval = this.pullMonitoringInterval;
        final Timer t = new Timer() {
            @Override
            public void run() {
                checkStatus();
            }
        };
        t.scheduleRepeating(this.currentInterval);
        return t;
    }

    private void cancelTimer() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    @Override
    public void stop() {
        Firebug.debug("PullHandler <stop> canceling timer");
        cancelTimer();
    }

    @Override
    public boolean isActive() {
        return isPulling();
    }
}