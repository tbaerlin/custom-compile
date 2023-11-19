/*
 * InvalidDmxmlRequestException.java
 *
 * Created on 21.03.12 13:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class InvalidDmxmlRequestException extends Exception implements IsSerializable {
    public InvalidDmxmlRequestException() {
    }

    private String requestXml;

    public InvalidDmxmlRequestException(String requestXml) {
        this.requestXml = requestXml;
    }

    public InvalidDmxmlRequestException(String message, String requestXml) {
        super(message);
        this.requestXml = requestXml;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
    }
}
