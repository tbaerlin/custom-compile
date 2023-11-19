/*
 * MmwebRequest.java
 *
 * Created on 07.08.2008 10:25:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.io.Serializable;
import java.util.ArrayList;

import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PlaceStatistics;

/**
 * Wrapper object for a dmxml request and additional information (statistics, etc.).
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MmwebRequest implements Serializable {

    private ArrayList<PlaceStatistics> usageStatistics;

    private RequestType dmxmlRequest;

    private boolean withXmlRequest = false;
    private boolean withXmlResponse = false;

    public MmwebRequest() { // needed for gwt-RPC serializability
    }

    public MmwebRequest(RequestType dmxmlRequest) {
        this.dmxmlRequest = dmxmlRequest;
    }

    public RequestType getDmxmlRequest() {
        return this.dmxmlRequest;
    }

    public ArrayList<PlaceStatistics> getUsageStatistics() {
        return this.usageStatistics;
    }

    public void setUsageStatistics(ArrayList<PlaceStatistics> usageStatistics) {
        this.usageStatistics = usageStatistics;
    }

    public boolean isWithXmlRequest() {
        return withXmlRequest;
    }

    public void setWithXmlRequest(boolean withXmlRequest) {
        this.withXmlRequest = withXmlRequest;
    }

    public boolean isWithXmlResponse() {
        return withXmlResponse;
    }

    public void setWithXmlResponse(boolean withXmlResponse) {
        this.withXmlResponse = withXmlResponse;
    }
}
