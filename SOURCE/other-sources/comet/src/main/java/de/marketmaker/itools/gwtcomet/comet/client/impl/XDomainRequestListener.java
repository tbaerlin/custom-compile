package de.marketmaker.itools.gwtcomet.comet.client.impl;

/**
 * Created on 31.01.13 16:54
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * <p/>
 * SOURCE: https://groups.google.com/forum/?fromgroups=#!topic/gwt-comet/NkfGh6-nREM
 *
 * @author Michael Lösch
 */

public interface XDomainRequestListener {

    /**
     * Raised when there is an error that prevents the completion of the
     * cross-domain request.
     */
    public void onError(XDomainRequest request);

    /**
     * Raised when the object has been completely received from the server.
     *
     * @param responseText
     */
    public void onLoad(XDomainRequest request, String responseText);

    /**
     * Raised when the browser starts receiving data from the server.
     *
     * @param responseText partial data received
     */
    public void onProgress(XDomainRequest request, String responseText);

    /**
     * Raised when there is an error that prevents the completion of
     * the
     * request.
     */
    public void onTimeout(XDomainRequest request);
}