package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;
import net.jcip.annotations.ThreadSafe;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

/**
 * Created on 16.07.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@ThreadSafe
public class WebsocketClient extends AbstractClient {

    private Session session;

    public WebsocketClient(String id, VwdProfile profile) {
        super(id, profile);
    }

    public void setSession(Session session) {
        synchronized (this.mutex) {
            this.session = session;
            this.withoutSessionSince = (session != null) ? 0 : System.currentTimeMillis();
            this.logger.info("<setSession> for " + this.id + " with " + (session != null ? "session" : "null"));
            // todo: remove this from registrations if session == null?
            // todo: add this to registrations if session != null?
        }
    }

    @Override
    protected void doFlush() throws IllegalStateException {
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
                this.session.getBasicRemote().sendObject(this.toBePushed);
            }
            catch (EncodeException | IOException e) {
                this.logger.error("<flush> encoding failed for " + this.id, e);
                setSession(null);
                return;
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

    @Override
    void dispose() {
        synchronized (this.mutex) {
            for (Registration registration : this.registrations.values()) {
                registration.removeClient(this);
            }
            this.registrations.clear();
            if (this.session != null) {
                try {
                    this.session.close();
                }
                catch (IOException e) {
                    this.logger.error("could not close session!", e);
                }
            }
        }
    }
}
