package de.marketmaker.istar.merger.provider;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import de.marketmaker.istar.merger.web.easytrade.block.UserCommand;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.user.NoSuchUserException;

/**
 * CachingUserProvider.java
 * Created on Sep 21, 2009 1:41:35 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CachingUserProvider {

    private UserProvider userProvider;

    // maps external user id (arbitrary String) to internal user id (Long)
    private Ehcache uidCache;

    public void setUidCache(Ehcache uidCache) {
        this.uidCache = uidCache;
    }

    public UserProvider getUserProvider() {
        return this.userProvider;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public Long getLocalUserId(UserCommand cmd) {
        if (this.uidCache != null) {
            final Element element = this.uidCache.get(cmd.getUserid());
            if (element != null) {
                return (Long) element.getValue();
            }
        }

        final UserContext userContext = retrieveUserContext(cmd);
        return userContext.getUser().getId();
    }

    private UserContext retrieveUserContext(UserCommand cmd) {
        final UserContext userContext =
                this.userProvider.getUserContext(cmd.getUserid(), cmd.getCompanyid());
        if (userContext == null) {
            throw new NoSuchUserException("Failed to create user " + cmd.getUserid(), -1);
        }

        if (this.uidCache != null) {
            this.uidCache.put(new Element(cmd.getUserid(), userContext.getUser().getId()));
        }
        return userContext;
    }

    public UserContext getUserContext(UserCommand cmd) {
        if (this.uidCache != null) {
            final Element element = this.uidCache.get(cmd.getUserid());
            if (element != null) {
                return this.userProvider.getUserContext((Long) element.getValue());
            }
        }

        return retrieveUserContext(cmd);
    }
}
