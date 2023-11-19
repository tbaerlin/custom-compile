/*
 * WrappedDmxmlRequest.java
 *
 * Created on 15.03.2012 08:49:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.io.Serializable;

/**
 * Contains a dm[xml] request to be sent to dm[xml]. It is not directly sent to allow
 * augmenting the response in server code.
 * 
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class WrappedDmxmlRequest implements Serializable {

    public WrappedDmxmlRequest(String dmxmlRequest) {
        this.dmxmlRequest = dmxmlRequest;
    }

    public WrappedDmxmlRequest() {
    }

    private String dmxmlRequest;

    public String getDmxmlRequest() {
        return dmxmlRequest;
    }

    public void setDmxmlRequest(String dmxmlRequest) {
        this.dmxmlRequest = dmxmlRequest;
    }
}
