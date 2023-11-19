/*
 * AuthenticationKeyInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * If a zone's definition contains a context String (or List&lt;String&gt;) "authenticationKey",
 * data requests will only be answered if the request's key
 * (see {@link de.marketmaker.istar.merger.web.easytrade.MoleculeRequest#getKey()}) matches that string
 * (or is an element of that list).
 * <p>
 * The authenticationKey should only be known to the client who is expected to access the
 * zone's URLs and it should not be possible to "guess" it. This mechanism helps to ensure that
 * the zone can only be used by a particular customer, which is especially important if
 * a customer is expected to pay for each data page that is accessed.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @deprecated Used only for docman (idoc project).
 */
@SuppressWarnings("unused")
public class AuthenticationKeyInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object o) throws Exception {

        final Object authenticationKey
                = RequestContextHolder.getRequestContext().get("authenticationKey");

        if (authenticationKey == null) {
            return true;
        }

        final String userKey = HttpRequestUtil.getValue(request, ProfileResolver.KEY_KEY);
        if (isKeyAcceptable(authenticationKey, userKey)) {
            return true;
        }

        throw new IllegalStateException("invalid key: " + userKey);
    }

    private boolean isKeyAcceptable(Object authenticationKey, String userKey) {
        if (userKey == null) {
            return false;
        }

        if (authenticationKey.equals(userKey)) {
            return true;
        }

        //noinspection unchecked
        return authenticationKey instanceof List
                && ((List<String>) authenticationKey).contains(userKey);
    }
}
