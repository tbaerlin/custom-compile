/*
 * UserMessageMonitor.java
 *
 * Created on 07.08.2008 13:00:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.http.HttpSession;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;

/**
 * Tries to figure out if there are any pending alert notifications for a given user and, if so,
 * adds a property to the response that can be used to inform the user about those notifications
 * in the frontend.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class UserAlertMonitor implements MmwebResponseListener {
    /**
     * Assumption: this is a distributed cache and some component will put the ids of those users
     * into it that have pending notifications.
     */
    private Ehcache cache;

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public void onBeforeSend(HttpSession session, MmwebResponse response) {
        final String uid = getVwdUserId(session);
        if (uid == null || this.cache == null) {
            return;
        }
        final Element e = this.cache.get(uid);
        if (e != null) {
            // the value will be an integer: number of pending alert notifications
            response.addProperty(AppConfig.PROP_KEY_PENDING_ALERTS, e.getValue().toString());
        }
    }

    /**
     * Retrieves vwdUserId from session
     * @see de.marketmaker.iview.mmgwt.mmweb.server.UserLoginMethod#prepareSession(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpSession)
     * @param session the http session
     * @return uid or null
     */
    private String getVwdUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        try {
            return (String) session.getAttribute(ProfileResolver.AUTHENTICATION_KEY);
        } catch (IllegalStateException e) {
            // session invalidated; unfortunately no way to test that 
            return null;
        }
    }

    @ManagedOperation(description = "Adds a limit count for the given vwd ID to the limits cache. Intended for testing purposes.")
    public void addLimitCount(String vwdId, int limitCount) {
        if(limitCount <= 0) {
            removeLimitCount(vwdId);
        }
        this.cache.put(new Element(vwdId, limitCount));
    }

    @ManagedOperation(description = "Removes the limit count for the given vwd ID. Intended for testing purposes.")
    public void removeLimitCount(String vwdId) {
        this.cache.remove(vwdId);
    }
}