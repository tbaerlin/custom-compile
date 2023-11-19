/*
 * OrderEntryService.java
 *
 * Created on 16.01.14 14:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.RemoteService;
import de.marketmaker.iview.pmxml.LoginBrokerRequest;
import de.marketmaker.iview.pmxml.LoginBrokerResponse;

/**
 * @author Markus Dick
 */
public interface OrderEntryService extends RemoteService {
    LoginBrokerResponse login(LoginBrokerRequest request) throws OrderEntryServiceException;
}
