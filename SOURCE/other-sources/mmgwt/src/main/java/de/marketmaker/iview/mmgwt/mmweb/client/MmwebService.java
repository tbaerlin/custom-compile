/*
 * MmwebService.java
 *
 * Created on 27.02.2008 09:43:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MmwebService extends RemoteService {
    public static final String SESSION_VALIDATOR_REQUEST_HEADER = "Session-Validator"; //$NON-NLS$
    MmwebResponse getData(MmwebRequest request);
}
