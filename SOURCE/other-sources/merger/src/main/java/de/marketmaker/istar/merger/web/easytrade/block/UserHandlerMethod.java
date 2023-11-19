/*
 * UserHandlerMethod.java
 *
 * Created on 01.12.2015 11:57
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.RequestWrapper;

/**
 * @author mdick
 * @see UserHandler#onBind(HttpServletRequest, Object)
 */
public class UserHandlerMethod {
    private HttpServletRequest httpServletRequest;

    private UserCommand o;

    public UserHandlerMethod(HttpServletRequest httpServletRequest, UserCommand o) {
        this.httpServletRequest = httpServletRequest;
        this.o = o;
    }

    public void invoke() {
        final UserCommand command = o;
        if (command.getUserid() == null) {
            final HttpSession session = httpServletRequest.getSession(false);
            if (session != null) {
                final String userId = (String) session.getAttribute(ProfileResolver.AUTHENTICATION_KEY);
                command.setUserid(userId);
            }
            else {
                // Necessary when accessing flex tools by credential instead of HTTP session;
                // credential and company id are molecule request parameters (not atom request parameters).
                resolveUserIdFromCredential(httpServletRequest, command);
            }
        }
    }

    private boolean trySetUserIdAndCompanyId(HttpServletRequest request, UserCommand command) {
        final String credential = HttpRequestUtil.getValue(request, ProfileResolver.CREDENTIAL_KEY);
        if (credential != null) {
            command.setUserid(ProfileUtil.decodeAuthentication(credential));
            final String companyid = HttpRequestUtil.getValue(request, "companyid");
            if (companyid != null) {
                try {
                    command.setCompanyid(Long.parseLong(companyid));
                } catch (NumberFormatException nfe) {
                    //do nothing
                }
            }
            return true;
        }
        return false;
    }

    private void resolveUserIdFromCredential(final HttpServletRequest httpServletRequest,
            UserCommand command) {
        trySetUserIdAndCompanyId(httpServletRequest, command);

        HttpServletRequest request = httpServletRequest;
        while (request instanceof RequestWrapper) {
            request = ((RequestWrapper) request).getDelegate();
            if (trySetUserIdAndCompanyId(request, command)) {
                return;
            }
        }
    }
}
