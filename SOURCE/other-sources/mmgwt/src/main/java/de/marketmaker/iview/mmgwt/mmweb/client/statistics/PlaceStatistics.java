/*
 * PlaceStatistics.java
 *
 * Created on 07.12.2009 10:08:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;

import com.google.gwt.core.client.Duration;

/**
 * Captures when a certain page was visited and statistics about the requests issued when the
 * page was active.
 * @author oflege
 */
public class PlaceStatistics implements Serializable {
    private double timestamp;

    private String historyToken;

    private int numRequests = 0;

    private int numBlocks = 0;

    private int requestTime = 0;

    private int processTime = 0;

    private int maxRequestTime = 0;

    private int maxProcessTime = 0;

    public PlaceStatistics() { // needed for gwt-RPC serializability
    }

    public PlaceStatistics(String historyToken) {
        this.historyToken = historyToken;
        this.timestamp = Duration.currentTimeMillis();
    }

    public double getTimestamp() {
        return timestamp;
    }

    public String getHistoryToken() {
        return this.historyToken;
    }

    public int getNumRequests() {
        return numRequests;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public int getRequestTime() {
        return requestTime;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getMaxRequestTime() {
        return maxRequestTime;
    }

    public int getMaxProcessTime() {
        return maxProcessTime;
    }

    @Override
    public String toString() {
        return this.timestamp + ": " + this.historyToken // $NON-NLS-0$
                + " #" + this.numRequests + "/" + this.numBlocks // $NON-NLS-0$ $NON-NLS-1$
                + " rt=" + this.requestTime + "(" + this.maxRequestTime + ")" // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                + " pt=" + this.processTime + "(" + this.maxProcessTime + ")"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    /**
     * Acknowledges a request that has been issued and its result been processed.
     * Do not refactor to use {@link com.google.gwt.core.client.Duration}, as this object needs
     * to live on the server side as well where Duration is not available.
     * @param numBlocks number of blocks in request
     * @param rt millis elapsed until response was received
     * @param pt millis elapsed until response was processed
     */
    void ackRequest(int numBlocks, int rt, int pt) {
        this.numRequests++;
        this.numBlocks += numBlocks;
        this.requestTime += rt;
        if (rt > this.maxRequestTime) {
            this.maxRequestTime = rt;
        }
        this.processTime += pt;
        if (pt > this.maxProcessTime) {
            this.maxProcessTime = pt;
        }
    }

}
