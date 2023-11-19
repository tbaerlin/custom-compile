/*
 * Client.java
 *
 * Created on 10.02.2010 06:59:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushOrderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushService;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oflege
 */
@ThreadSafe
abstract class AbstractClient {
    protected final Log logger = LogFactory.getLog(getClass());

    protected final String id;

    protected final Map<String, Registration> registrations = new HashMap<>();

    protected final Object mutex = new Object();

    protected PushData toBePushed = new PushData();

    private final VwdProfile profile;

    protected long withoutSessionSince = System.currentTimeMillis();

    private PushService service;

    private static final int MS_UNTIL_DISCONNECT = 15000;

    public AbstractClient(String id, VwdProfile profile) {
        this.id = id;
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "Client[" + this.id + "]";
    }

    public String getId() {
        return this.id;
    }

    public VwdProfile getProfile() {
        return this.profile;
    }

    void add(Registration registration, BitSet allowedFields) {
        synchronized (this.mutex) {
            this.registrations.put(registration.getRegistrationKey(), registration);
        }
        registration.addClient(this, allowedFields);
    }

    void push(PushPrice price) {
        this.toBePushed.add(price);
    }

    void push(PushOrderbook orderbook) {
        this.toBePushed.add(orderbook);
    }

    boolean isWithData() {
        return !this.toBePushed.isEmpty();
    }

    void flush() {
        try {
            doFlush();
        } catch (IllegalStateException e) {
            this.logger.warn("<flush> no session for > " + MS_UNTIL_DISCONNECT + "ms for " + this.id);
            this.service.closeSession(this.id);
        }
    }

    protected boolean isSessionNullForTooLong() {
        return (System.currentTimeMillis() - this.withoutSessionSince) > MS_UNTIL_DISCONNECT;
    }

    void unregister(String symbol) {
        final Registration registration;
        synchronized (this.mutex) {
            registration = this.registrations.remove(symbol);
        }
        if (registration != null) {
            registration.removeClient(this);
        }
    }

    boolean isRegistered(String symbol) {
        synchronized (this.mutex) {
            return this.registrations.containsKey(symbol);
        }
    }

    void setPushService(PushService service) {
        this.service = service;
    }

    void stopPush() {
        this.toBePushed.setStopPush();
    }

    protected abstract void doFlush();
    abstract void dispose();
    public abstract void nullSession();
}