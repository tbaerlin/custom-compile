/*
 * MmwebResponseListener.java
 *
 * Created on 07.08.2008 13:07:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.http.HttpSession;

import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MmwebResponseListener {

    /**
     * invoked just before the response is sent to the client
     * @param session associated with current request, usually contains authentification data
     * @param response about to be sent
     */
    void onBeforeSend(HttpSession session, MmwebResponse response);
}
