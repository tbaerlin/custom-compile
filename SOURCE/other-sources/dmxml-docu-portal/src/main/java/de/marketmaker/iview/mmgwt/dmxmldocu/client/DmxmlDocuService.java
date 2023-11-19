/*
 * DmxmlDocuService.java
 *
 * Created on 15.03.2012 08:48:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface DmxmlDocuService extends RemoteService {

    /**
     * Login to dmxml documentation service. Creates a session (invalidating any existing ones)
     * which allows access to the other methods in this interface.
     * @param loginData login data
     * @throws InternalDmxmlServerError
     * @throws AuthenticationFailedException
     */
    void login(LoginData loginData) throws InternalDmxmlServerError, AuthenticationFailedException;

    /**
     * Terminates any existing session
     */
    void logout();

    /**
     * Get the repository of all available blocks.
     * auth
     * @return the repository of static documentation
     */
    BlocksDocumentation getRepository()
            throws InternalDmxmlServerError, AuthenticationFailedException;


    /**
     * Send the dm[xml] request contained in {@code request} to dm[xml] and augment the response
     * with documentation.
     * @param request request to send
     * @return response with documentation
     */
    WrappedDmxmlResponse sendDmxmlRequest(WrappedDmxmlRequest request)
            throws InvalidDmxmlRequestException, InvalidDmxmlResponseException,
            InternalDmxmlServerError, AuthenticationFailedException;

}
