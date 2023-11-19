/*
 * WrappedDmxmlResponse.java
 *
 * Created on 15.03.2012 08:50:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.io.Serializable;

import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Element;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Node;

/**
 * Contains the response of a dm[xml] request sent through
 * {@link DmxmlDocuService}
 * together with documentation information.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class WrappedDmxmlResponse implements Serializable {

    private String dmxmlResponse;

    private Element xmlTreeRoot;

    private String requestValidationMessage;
    private String responseValidationMessage;
    private String parserMessage;
    private InvalidDmxmlResponseException responseException;


    public String getDmxmlResponse() {
        return dmxmlResponse;
    }

    public void setDmxmlResponse(String dmxmlResponse) {
        this.dmxmlResponse = dmxmlResponse;
    }

    public Element getXmlTreeRoot() {
        return xmlTreeRoot;
    }

    public void setXmlTreeRoot(Element xmlTreeRoot) {
        this.xmlTreeRoot = xmlTreeRoot;
    }

    public String getRequestValidationMessage() {
        return requestValidationMessage;
    }

    public void setRequestValidationMessage(String requestValidationMessage) {
        this.requestValidationMessage = requestValidationMessage;
    }

    public String getResponseValidationMessage() {
        return responseValidationMessage;
    }

    public void setResponseValidationMessage(String responseValidationMessage) {
        this.responseValidationMessage = responseValidationMessage;
    }

    public String getParserMessage() {
        return parserMessage;
    }

    public void setParserMessage(String parserMessage) {
        this.parserMessage = parserMessage;
    }

    public InvalidDmxmlResponseException getResponseException() {
        return responseException;
    }

    public void setResponseException(InvalidDmxmlResponseException responseException) {
        this.responseException = responseException;
    }
}
