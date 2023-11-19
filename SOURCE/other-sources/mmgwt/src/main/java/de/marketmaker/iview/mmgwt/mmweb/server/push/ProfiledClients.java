/*
 * ProfiledRegistration.java
 *
 * Created on 14.03.2010 11:00:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.GuardedBy;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.vwd.ProfiledSnapRecord;

/**
 * Information for clients that share the same allowed fields for a given registration.
 * @author oflege
 */
class ProfiledClients<T> {
    private final Registration<T> reg;

    private final BitSet allowedFields;

    private T previous;

    @GuardedBy("this")
    private final Map<String, AbstractClient> clients = new HashMap<>();

    // clients that need a complete PushPrice as first update
    @GuardedBy("this")
    private final List<AbstractClient> newClients = new ArrayList<>();

    ProfiledClients(Registration<T> reg, BitSet allowedFields) {
        this.reg = reg;
        this.allowedFields = allowedFields;
    }

    BitSet getAllowedFields() {
        return allowedFields;
    }

    synchronized void addClient(AbstractClient c) {
        this.newClients.add(c);
    }

    synchronized int removeClient(AbstractClient c) {
        final AbstractClient client = this.clients.remove(c.getId());
        if (client == null) {
            this.newClients.remove(c);
        }
        return this.clients.size() + this.newClients.size();
    }

    void pushUpdate(SnapRecord record) {
        final T complete = this.reg.createComplete(toProfiledRecord(record));
        final T diff = this.reg.createDiff(complete, this.previous, this.allowedFields);
        this.previous = complete;
        pushUpdate(complete, diff);
    }

    private synchronized void pushUpdate(T complete, T diff) {
        this.reg.push(this.clients.values(), diff);
        if (!this.newClients.isEmpty()) {
            this.reg.push(this.newClients, complete);
            integrateNewClients();
        }
    }

    private void integrateNewClients() {
        for (AbstractClient client : this.newClients) {
            this.clients.put(client.getId(), client);
        }
        this.newClients.clear();
    }

    private ProfiledSnapRecord toProfiledRecord(SnapRecord record) {
        return new ProfiledSnapRecord("dummy", record, this.allowedFields, null, null);
    }
}
