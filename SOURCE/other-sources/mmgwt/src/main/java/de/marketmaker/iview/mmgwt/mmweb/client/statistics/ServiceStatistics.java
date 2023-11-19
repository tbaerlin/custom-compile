/*
 * ServiceStatistics.java
 *
 * Created on 02.03.2009 17:07:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import com.google.gwt.core.client.Scheduler;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StatisticsWindow;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ServiceStatistics implements RequestCompletedHandler {
    private int[] avgProcessingTimes;

    private int[] avgRequestTimes;

    private int[] avgTotalTimes;

    private double lastProcessingTime;

    private double lastRequestTime;

    private double lastTotalTime;

    private int numBlocks;

    private int numFailed;

    private int numRequests;

    private static ServiceStatistics INSTANCE = null;

    public static ServiceStatistics getOrCreate() {
        if (INSTANCE == null) {
            INSTANCE = new ServiceStatistics();
            EventBusRegistry.get().addHandler(RequestCompletedEvent.getType(), INSTANCE);
        }
        return INSTANCE;
    }

    ServiceStatistics() {
        reset();
    }

    public int[] getAvgProcessingTimes() {
        return avgProcessingTimes;
    }

    public int[] getAvgRequestTimes() {
        return avgRequestTimes;
    }

    public int[] getAvgTotalTimes() {
        return avgTotalTimes;
    }

    public int getLastProcessingTime() {
        return (int) lastProcessingTime;
    }

    public int getLastRequestTime() {
        return (int) lastRequestTime;
    }

    public int getLastTotalTime() {
        return (int) lastTotalTime;
    }

    public int getMeanProcessingTime() {
        return getMeanDuration(this.avgProcessingTimes);
    }

    public int getMeanRequestTime() {
        return getMeanDuration(this.avgRequestTimes);
    }

    public int getMeanTotalTime() {
        return getMeanDuration(this.avgTotalTimes);
    }

    public void reset() {
        this.numRequests = 0;
        this.numBlocks = 0;
        this.numFailed = 0;
        this.avgRequestTimes = new int[20];
        this.avgProcessingTimes = new int[20];
        this.avgTotalTimes = new int[20];
        this.lastProcessingTime = 0;
        this.lastRequestTime = 0;
        this.lastTotalTime = 0;
    }

    public void ackFailure() {
        this.numFailed++;
    }

    public void onRequestCompleted(RequestCompletedEvent event) {
        if (event.getStatistics() != null) {
            ackServiceCall(event);
        }
    }

    private void ackServiceCall(final RequestCompletedEvent event) {
        final RequestStatistics statistics = event.getStatistics();
        this.numRequests++;
        this.numBlocks += statistics.getNumBlocks();
        this.lastRequestTime = statistics.getRequestTime();
        this.lastProcessingTime = statistics.getProcessTime();
        this.lastTotalTime = this.lastRequestTime + this.lastProcessingTime;
        addDuration(this.avgRequestTimes, this.lastRequestTime);
        if (!statistics.isCancelled()) {
            addDuration(this.avgProcessingTimes, this.lastProcessingTime);
            addDuration(this.avgTotalTimes, this.lastTotalTime);
        }
        StatisticsWindow.updateStats();

        final String line = "Stats: total=" + this.lastTotalTime // $NON-NLS-0$
                + ", request=" + this.lastRequestTime + ", process=" + this.lastProcessingTime; // $NON-NLS$

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                DebugUtil.logToServer("UserAgent: " + BrowserSpecific.getUserAgent() + " / " + line);
                Firebug.log(line);
                Firebug.log("Last request was \n" + event.getResponse().getXmlRequest());
            }
        });
    }

    private void addDuration(final int[] array, double v) {
        double f = Math.max(0, v);
        final int i = Math.min((int) (f / 100), array.length - 1);
        array[i]++;
    }

    private int getMeanDuration(final int[] times) {
        int i = 0;
        int j = times.length - 1;
        int iCount = times[i];
        int jCount = times[j];
        while (i < j) {
            if (jCount > iCount) {
                i++;
                iCount += times[i];
            }
            else {
                j--;
                jCount += times[j];
            }
        }
        return i * 100 + 50;
    }
}