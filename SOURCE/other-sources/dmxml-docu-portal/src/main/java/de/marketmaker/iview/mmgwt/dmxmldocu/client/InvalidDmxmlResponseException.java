/*
 * InvalidDmxmlResponseException.java
 *
 * Created on 30.03.12 13:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class InvalidDmxmlResponseException extends Exception implements IsSerializable {

    private String responseXml;

    public InvalidDmxmlResponseException() {
    }

    public InvalidDmxmlResponseException(String responseXml) {
        this.responseXml = responseXml;
    }

    public InvalidDmxmlResponseException(String message, String responseXml) {
        super(message);
        this.responseXml = responseXml;
    }

    public String getResponseXml() {
        return responseXml;
    }

    public void setResponseXml(String responseXml) {
        this.responseXml = responseXml;
    }
}
