package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;
import de.marketmaker.iview.pmxml.LoginRequest;
import de.marketmaker.iview.pmxml.LoginResponse;
import de.marketmaker.iview.pmxml.ServerLoginResult;
import de.marketmaker.iview.pmxml.VoidRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created on 05.02.13 13:31
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * <p/>
 * <p/>
 * To communicate with a pm it's neccessary to get a valid pm-session-token.
 * This token is created after a pm-login is done.
 * If there is no way to perform a login, this class does a login (when a tomcat-session
 * is created) by a applicationContext-based user/pwd configuration
 *
 * @author Michael Lösch
 */

public class PmLoginInterceptor implements HttpSessionListener {

    private JaxbHandler jaxbHandler;
    protected PmxmlExchangeData pmxml;
    private String pmLicense;
    private String pmUsername;
    private String pmPwd;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    public void setPmxml(PmxmlExchangeData pmxml) {
        this.pmxml = pmxml;
    }

    @NotNull
    public void setPmLicense(String pmLicense) {
        this.pmLicense = pmLicense;
    }

    @NotNull
    public void setPmUsername(String pmUsername) {
        this.pmUsername = pmUsername;
    }

    @NotNull
    public void setPmPwd(String pmPwd) {
        this.pmPwd = pmPwd;
    }

    @NotNull
    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    public LoginResponse doLogin(HttpSession session) {
        if (!StringUtils.hasText(this.pmLicense)) {
            throw new IllegalStateException("pmLicense is not set!");
        }

        final PmExchangeData.Command loginCmd = new PmExchangeData.Command();
        loginCmd.setFunctionKey("um_login");
        loginCmd.setResponseType(LoginResponse.class);

        final LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLicense(this.pmLicense);
        loginRequest.setUsername(this.pmUsername);
        loginRequest.setPassword(this.pmPwd);

        loginCmd.setRequest(this.jaxbHandler.marshal(LoginRequest.class, loginRequest, "parameter"));
        final PmxmlExchangeDataRequest pmxmlRequest = PmExchangeData.createRequest(session, "", loginCmd);
        final PmxmlExchangeDataResponse pmxmlResponse;
        try {
            pmxmlResponse = PmExchangeData.exchangeData(pmxmlRequest, this.pmxml, this.logger, PmExchangeDataLogger.INFO);
            final LoginResponse loginResponse = this.jaxbHandler.unmarshal(
                    PmExchangeData.extractResponse(pmxmlResponse, loginCmd).getRawXml(), LoginResponse.class
            );
            if (loginResponse.getState() != ServerLoginResult.SLR_OK) {
                throw new IllegalStateException("pm login failed: " + loginResponse.getState());
            }
            session.setAttribute(ProfileResolver.PM_AUTHENTICATION_KEY, loginResponse.getSessionToken());
            this.logger.info("pm loggin as user " + this.pmUsername + "done");
            return loginResponse;
        } catch (PmxmlException e) {
            throw new IllegalStateException("pm login failed!", e);
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        //nothing to do
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        doPmLogout(httpSessionEvent.getSession());
    }

    private void doPmLogout(HttpSession httpSession) {
        final String pmAuthToken = (String) httpSession.getAttribute(ProfileResolver.PM_AUTHENTICATION_KEY);
        if (!StringUtils.hasLength(pmAuthToken)) {
            return;
        }

        final PmExchangeData.Command logoutCmd = new PmExchangeData.Command();
        logoutCmd.setFunctionKey("um_logout");

        final VoidRequest voidRequest = new VoidRequest();
        logoutCmd.setRequest(this.jaxbHandler.marshal(VoidRequest.class, voidRequest, "parameter"));

        final PmxmlExchangeDataRequest request = PmExchangeData.createRequest(httpSession, pmAuthToken, logoutCmd);
        try {
            PmExchangeData.exchangeData(request, this.pmxml, this.logger, PmExchangeDataLogger.INFO);
        } catch (PmxmlException e) {
            this.logger.warn("pm logout failed!", e);
        }
    }
}
