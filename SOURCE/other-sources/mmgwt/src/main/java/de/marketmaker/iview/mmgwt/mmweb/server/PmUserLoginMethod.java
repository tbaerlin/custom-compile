/*
 * PmUserLoginMethod.java
 *
 * Created on 05.02.13 13:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.itools.pmxml.frontend.PmxmlHash;
import de.marketmaker.iview.mmgwt.mmweb.client.PmUserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.LoginFeature;
import de.marketmaker.iview.pmxml.LoginRequest;
import de.marketmaker.iview.pmxml.LoginResponse;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static de.marketmaker.iview.mmgwt.mmweb.server.UserServiceImpl.SU_PREFIX;

/**
 * @author oflege
 */
class PmUserLoginMethod extends UserLoginMethod {
    private LoginResponse loginResponse;
    private Profile queriedVwdProfile = null;
    private boolean vwdProfileQueried = false;

    PmUserLoginMethod(PmUserServiceImpl service, UserRequest userRequest) {
        super(service, userRequest);
    }

    @Override
    UserResponse invoke() {
        final HttpServletRequest httpServletRequest = ServletRequestHolder.getHttpServletRequest();
        // enable PmxmlHandler to retrieve the locale w/o a session
        httpServletRequest.setAttribute(UserServiceImpl.SESSION_LOCALE, this.userRequest.getLocale());

        initConfigAndMode();
        this.loginResponse = login();

        switch (this.loginResponse.getState()) {
            case SLR_OK:
                // create session earlier than mmfweb because it's needed for pm communication
                createSession(httpServletRequest, getUid());
                return new PmUserResponse(super.invoke()).withState(this.loginResponse.getState());
            case SLR_OK_BUT_PASSWORD_EXPIRED:
                // create session earlier than mmfweb because it's needed for pm communication
                createSession(httpServletRequest, getUid());
                final UserResponse res = super.invoke();
                res.getUser().setPasswordChangeRequired(true);
                return new PmUserResponse(res).withState(this.loginResponse.getState());
            default:
                return new PmUserResponse().withState(this.loginResponse.getState());
        }
    }

    @Override
    protected HttpSession getSession() {
        //in mmfweb, this creates the session. in AS, session has been created already.
        return ServletRequestHolder.getHttpServletRequest().getSession(false);
    }

    @Override
    protected boolean responseOK(UserResponse response) {
        /*
        this is always true because pm login counts. if pm login was ok, vwd permissioning is less important
        and a failure of that part only leads to an AS without marketdata.
        if pm login fails, this place is never reached.
        */
        return true;
    }

    private LoginResponse login() {
        final LoginRequest request = new LoginRequest();
        request.setPwIsHashed(true);
        request.setPassword(PmxmlHash.toSHA256(getPassword(this.userRequest)));
        request.setUsername(getUserName(this.userRequest));
        request.setFeature(LoginFeature.LF_WEBGUI);
        return ((PmUserServiceImpl) this.service).login(request);
    }

    private String getUserName(UserRequest userRequest) {
        if (getMode() == UserServiceImpl.LoginMode.SU) {
            return userRequest.getLoginUpperCase().substring(SU_PREFIX.length());
        }
        return userRequest.getLoginUpperCase();
    }

    private String getPassword(UserRequest userRequest) {
        final String pw = userRequest.getPassword();
        if (getMode() == UserServiceImpl.LoginMode.SU) {
            return pw.substring(pw.indexOf("!") + 1, pw.length());
        }
        return pw;
    }

    @Override
    protected void prepareSession(HttpServletRequest request, HttpSession session) {
        // Setting this session token first is necessary to determine the correct authenticationType, if the
        // supplied vwd-ID is invalid (regarding the mandator-ID) or is some kind of a random text.
        // Setting a random text in PM's user management tool is especially possible if the PM server is down.
        WebUtils.setSessionAttribute(ServletRequestHolder.getHttpServletRequest(),
                ProfileResolver.PM_AUTHENTICATION_KEY, this.loginResponse.getSessionToken());
        super.prepareSession(request, session);
    }

    @Override
    protected Profile getProfile(String login) {
        final Profile vwdProfile = getVwdProfile();
        if(vwdProfile != null) {
            return vwdProfile;
        }
        return ((PmUserServiceImpl) this.service).getPmWebProfile(login, null);
    }

    private Profile getVwdProfile() {
        if(this.vwdProfileQueried) {
            return this.queriedVwdProfile;
        }
        this.vwdProfileQueried = true;

        final String vwdId = this.loginResponse.getVwdId();
        try {
            if (StringUtil.hasText(vwdId)) {
                this.queriedVwdProfile = super.getProfile(vwdId);
                return this.queriedVwdProfile;
            }
        }
        catch(IllegalStateException ise) {
            this.logger.warn("<getVwdProfile> failed to get profile for vwd-ID '" + vwdId + "'", ise);
        }
        this.queriedVwdProfile = null;
        return null;
    }

    @Override
    protected String getAuth() {
        if(getVwdProfile() != null) {
            return this.loginResponse.getVwdId();
        }
        return getUid();
    }

    @Override
    protected String getAuthType() {
        if(getVwdProfile() != null) {
            return UserServiceImpl.VWD_ID_AUTH_TYPE;
        }
        return UserServiceImpl.PMWEB_AUTH_TYPE;
    }

    protected String getUid() {
        return this.userRequest.getLoginUpperCase();
    }
}