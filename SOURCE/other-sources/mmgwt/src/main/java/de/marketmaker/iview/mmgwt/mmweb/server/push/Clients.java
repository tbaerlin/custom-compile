/*
 * Clients.java
 *
 * Created on 10.02.2010 07:06:12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.marketmaker.istar.domainimpl.profile.VwdProfile;

/**
 * @author oflege
 */
@ThreadSafe
class Clients {
    private final Log logger = LogFactory.getLog(getClass());

    @GuardedBy("clientsMutex")
    private final Map<String, AbstractClient> clients = new HashMap<>();

    private final Object clientsMutex = new Object();

    AbstractClient getClient(String s) {
        synchronized (this.clientsMutex) {
            return this.clients.get(s);
        }
    }

    void stopPush(String id) {
        final AbstractClient client = getClient(id);
        if (client != null) {
            client.stopPush();
            this.logger.info("<stopPush> for " + id);
        }
        else {
            this.logger.info("<stopPush> no such client " + id);
        }
    }

    void stopPush() {
        synchronized (this.clientsMutex) {
            for (AbstractClient client : this.clients.values()) {
                client.stopPush();
            }
            if (!this.clients.isEmpty()) {
                this.logger.info("<stopPush> for " + this.clients.size() + " clients");
            }
        }
    }

    boolean removeClient(String id) {
        final AbstractClient result;
        final int numClients;
        synchronized (this.clientsMutex) {
            result = this.clients.remove(id);
            numClients = this.clients.size();
        }
        if (result == null) {
            this.logger.info("<removeClient> no such client " + id);
            return false;
        }
        result.dispose();
        this.logger.info("<removeClient> " + result + ", #clients=" + numClients);
        return true;
    }

    AbstractClient createClient(String id, VwdProfile profile, boolean websocket) {
        final AbstractClient result = websocket
                ? new WebsocketClient(id, profile)
                : new CometClient(id, profile);
        final int numClients;
        synchronized (this.clientsMutex) {
            this.clients.put(result.getId(), result);
            numClients = this.clients.size();
        }
        this.logger.info("<createClient> " + result + ", #clients=" + numClients);
        return result;
    }

    List<AbstractClient> getClientsWithData() {
        final List<AbstractClient> result;
        synchronized (this.clientsMutex) {
            result = new ArrayList<>(this.clients.size());
            for (AbstractClient client : this.clients.values()) {
                if (client.isWithData()) {
                    result.add(client);
                }
            }
        }
        return result;
    }

    Collection<AbstractClient> getClients() {
        synchronized (this.clientsMutex) {
            return Collections.unmodifiableCollection(this.clients.values());
        }
    }
}