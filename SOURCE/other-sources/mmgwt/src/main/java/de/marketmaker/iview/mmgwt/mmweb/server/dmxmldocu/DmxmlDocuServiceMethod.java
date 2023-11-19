/*
 * DmxmlDocuServiceMethod.java
 *
 * Created on 06.09.12 17:09
 *
 * Copyright (this) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu;

import de.marketmaker.istar.merger.web.NoProfileException;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.AuthenticationFailedException;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.InternalDmxmlServerError;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.InvalidDmxmlRequestException;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.LoginData;
import de.marketmaker.iview.mmgwt.mmweb.server.MmwebServiceMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu.DmxmlDocuServiceImpl.LOGIN_DATA_ATTRIBUTE;

/**
 * @author oflege
 */
public class DmxmlDocuServiceMethod extends MmwebServiceMethod {

    private final Log logger = LogFactory.getLog(getClass());

    public LoginData loginData;

    public DmxmlDocuServiceMethod(DmxmlDocuServiceImpl service, RequestType dmxmlRequest)
            throws AuthenticationFailedException {
        super(service.getMmwebService());
        this.dmxmlRequest = dmxmlRequest;
        if (this.session == null || this.session.getAttribute(LOGIN_DATA_ATTRIBUTE) == null) {
            throw new AuthenticationFailedException("You need to login first");
        }
        this.loginData = (LoginData) this.session.getAttribute(LOGIN_DATA_ATTRIBUTE);
        service.checkAuthentication(this.loginData, this.servletRequest);
    }

    @Override
    protected Map<String, String[]> getMoleculeRequestParams() {
        return getAuthenticationMap(Collections.<String, String[]>emptyMap());
    }

    /**
     * <p>Requires: moleculeRequest, servletRequest, zone</p>
     * <p>Fills: internalError, rawResponse</p>
     * <p>Modifies: servletRequest</p>
     * @return raw response
     * @throws de.marketmaker.iview.mmgwt.dmxmldocu.client.AuthenticationFailedException if no profile is found
     * @throws de.marketmaker.iview.mmgwt.dmxmldocu.client.InternalDmxmlServerError if c.internalError is set after sendMoleculeRequest
     */
    void sendDmxmlRequestToMmwebService() throws AuthenticationFailedException, InvalidDmxmlRequestException {
        try {
            this.moleculeRequest = this.service.toMoleculeRequest(this.dmxmlRequest);

            final boolean hasResponse = sendMoleculeRequest(true);
            this.logger.debug("<sendDmxmlRequest> executed dmxml request.");
            if (!hasResponse) {
                if (this.internalError) {
                    throw new InternalDmxmlServerError("An internal server error occurred; see server log for details");
                }
                else {
                    throw new AuthenticationFailedException("Authorization needed: No profile found.");
                }
            }
        } catch (NoProfileException e) {
            throw new AuthenticationFailedException(e);
        }
    }
}
