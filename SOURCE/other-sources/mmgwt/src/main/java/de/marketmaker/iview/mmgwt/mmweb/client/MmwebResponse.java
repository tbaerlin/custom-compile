/*
 * MmwebRequest.java
 *
 * Created on 07.08.2008 10:25:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.io.Serializable;
import java.util.HashMap;

import de.marketmaker.iview.dmxml.ResponseType;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MmwebResponse implements Serializable {
    public enum State { OK, SESSION_EXPIRED, INTERNAL_ERROR }

    private HashMap<String, String> properties = new HashMap<String, String>();

    private ResponseType responseType;

    private State state = State.OK;

    private String xmlRequest;
    private String xmlResponse;

    public MmwebResponse() {
    }

    public MmwebResponse withState(State state) {
        this.state = state;
        return this;
    }

    public State getState() {
        return this.state;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public String getXmlRequest() {
        return xmlRequest;
    }

    public void setXmlRequest(String xmlRequest) {
        this.xmlRequest = xmlRequest;
    }

    public String getXmlResponse() {
        return xmlResponse;
    }

    public void setXmlResponse(String xmlResponse) {
        this.xmlResponse = xmlResponse;
    }
}
