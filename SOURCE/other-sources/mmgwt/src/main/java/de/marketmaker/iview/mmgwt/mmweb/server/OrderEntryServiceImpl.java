/*
 * OrderEntryServiceImpl.java
 *
 * Created on 16.01.14 14:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.itools.amqprpc.impl.RemoteAccessTimeoutException;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryService;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryServiceException;
import de.marketmaker.iview.pmxml.LoginBrokerRequest;
import de.marketmaker.iview.pmxml.LoginBrokerResponse;
import de.marketmaker.iview.pmxml.block.PmExchangeData;

/**
 * @author Markus Dick
 */
@SuppressWarnings("GwtServiceNotRegistered")
public class OrderEntryServiceImpl extends AbstractOrderEntryService implements OrderEntryService {

    @Override
    public LoginBrokerResponse login(LoginBrokerRequest request) throws OrderEntryServiceException {
        if (request == null) {
            throw new IllegalArgumentException("<login> request is null!");
        }
        if (request.getSessionHandle() == null) {
            throw new IllegalArgumentException("<login> sessionHandle of request is null!");
        }
        if (request.getPassword() == null) {
            throw new IllegalArgumentException("<login> password of request is null!");
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<login> " + request.getSessionHandle());
        }

        try {
            request.setPasswordCrypted(isEncryptPassword());
            if (isEncryptPassword()) {
                request.setPassword(doCrypt(request.getSessionHandle(), request.getPassword()));
            }
        } catch (Exception e) {
            this.logger.error("<login> call failed for session=" + request.getSessionHandle(), e);
            throw new OrderEntryServiceException("encrypt_password_failed", e.getMessage());
        }

        try {
            PmExchangeData.bindToServer(ServletRequestHolder.getHttpServletRequest().getSession(false));
            return getPmxmlHandler().exchangeData(request, "LoginBroker", LoginBrokerResponse.class);
        } catch (Exception e) {
            this.logger.error("<login> call failed for session=" + request.getSessionHandle(), e);
            if (e.getCause() instanceof RemoteAccessTimeoutException) {
                throw new OrderEntryServiceException("remote_timeout", e.getCause().getMessage());
            }
            throw new OrderEntryServiceException("unspecified.error", e.getMessage(), e);
        }
    }
}
