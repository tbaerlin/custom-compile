package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.itools.gwtcomet.comet.server.CometSession;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import net.jcip.annotations.ThreadSafe;

/**
 * Created on 16.07.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@ThreadSafe
public class CometClient extends AbstractClient {

    private CometSession session;

    public CometClient(String id, VwdProfile profile) {
        super(id, profile);
    }

    public void setSession(CometSession session) {
        synchronized (this.mutex) {
            this.session = session;
            this.withoutSessionSince = (session != null) ? 0 : System.currentTimeMillis();
            this.logger.info("<setSession> for " + this.id + " with " + (session != null ? "session" : "null"));
            // todo: remove this from registrations if session == null?
            // todo: add this to registrations if session != null?
        }
    }

    protected void doFlush() {
        synchronized (this.mutex) {
            if (this.session == null) {
                if (isSessionNullForTooLong()) {
                    this.toBePushed.clear();
                    throw new IllegalStateException();
                }
                return;
            }
            if (this.toBePushed.isEmpty()) {
                return;
            }
            try {
                this.session.enqueue(this.toBePushed);
            } catch (IllegalStateException e) {
                this.logger.warn("<flush> failed for " + this.id);
                setSession(null);
                return; // keep queued prices
            }
            this.toBePushed = new PushData();
        }
    }

    @Override
    public void nullSession() {
        synchronized (this.mutex) {
            this.session = null;
        }
    }

    void dispose() {
        synchronized (this.mutex) {
            for (Registration registration : this.registrations.values()) {
                registration.removeClient(this);
            }
            this.registrations.clear();
            if (this.session != null) {
                this.session.invalidate();
            }
        }
    }
}