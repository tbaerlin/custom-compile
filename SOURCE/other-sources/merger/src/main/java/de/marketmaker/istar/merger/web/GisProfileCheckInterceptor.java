/*
 * ParameterMappingHandlerInterceptor.java
 *
 * Created on 29.01.2008 14:03:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.XunProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Ulrich Maurer
 */
public class GisProfileCheckInterceptor extends HandlerInterceptorAdapter {
    private int errorCode = HttpServletResponse.SC_FORBIDDEN;

    private final Set<XunProfile.Item> permissions = EnumSet.noneOf(XunProfile.Item.class);
    private final Set<Selector> selectors = EnumSet.noneOf(Selector.class);

    @Required
    public void setPermissions(String[] permissions) {
        for (final String permission : permissions) {
            this.permissions.add(XunProfile.Item.get(permission));
        }
    }
    @Required
    public void setSelectors(String[] selectors) {
        for (final String selector : selectors) {
            this.selectors.add(Selector.valueOf(selector));
        }
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "GisProfileCheckInterceptor{" +
                "permissions=" + permissions +
                ", errorCode=" + errorCode +
                ", selectors=" + selectors +
                '}';
    }

    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object o) throws Exception {
        if (isProfileAcceptable()) {
            return true;
        }
        throw new HttpException(this.errorCode);
    }

    private boolean isProfileAcceptable() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile != null && (isAcceptable(profile) || isXunAcceptable(profile));
    }

    private boolean isAcceptable(Profile profile) {
        for (final Selector selector : selectors) {
            if (profile.isAllowed(selector)) {
                return true;
            }
        }
        return false;
    }

    /* TODO: remove after KIS is obsolete */
    private boolean isXunAcceptable(Profile profile) {
        return profile instanceof XunProfile
                && ((XunProfile) profile).containsAny(this.permissions);
    }
}