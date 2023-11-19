/*
 * PageRequest.java
 *
 * Created on 07.12.2009 10:16:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;

import de.marketmaker.iview.dmxml.RequestType;

/**
 * @author oflege
 */
public class RequestStatistics implements Serializable {
    private static int nextId = 0;

    private final int id = nextId++;

    private int numBlocks;

    private boolean cancelled = false;

    private boolean failed = false;

    private double processTime;

    private double requestTime;

    private double startTimestamp;

    public RequestStatistics() {// needed for gwt-RPC serializability
    }

    public static RequestStatistics create(RequestType dmxmlRequest, double currentTimeMillis) {
        return new RequestStatistics(dmxmlRequest.getBlock().size(), currentTimeMillis);
    }

    private RequestStatistics(int numBlocks, double currentTimeMillis) {
        this.numBlocks = numBlocks;
        this.startTimestamp = currentTimeMillis;
    }

    public int getId() {
        return this.id;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public double getProcessTime() {
        return this.processTime;
    }

    public double getRequestTime() {
        return this.requestTime;
    }

    public double getStartTimestamp() {
        return this.startTimestamp;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isFailed() {
        return this.failed;
    }
    
    public void onCancel() {
        this.cancelled = true;
    }
    
    public void onCompletion(double currentTimeMillis) {
        this.processTime = currentTimeMillis - this.startTimestamp - this.requestTime;
    }

    public void onFailure(double currentTimeMillis) {
        this.failed = true;
        onResponse(currentTimeMillis);
    }

    public void onResponse(double currentTimeMillis) {
        this.requestTime = currentTimeMillis - this.startTimestamp;
    }

    @Override
    public String toString() {
        return this.startTimestamp + ": #" + this.numBlocks + " " // $NON-NLS-0$ $NON-NLS-1$
                + (this.requestTime + this.processTime)
                + "ms (" + this.requestTime + "/" + this.processTime + ")"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }
}
