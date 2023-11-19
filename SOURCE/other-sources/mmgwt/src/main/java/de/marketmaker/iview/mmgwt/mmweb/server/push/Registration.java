/*
 * Registration.java
 *
 * Created on 08.03.2010 14:45:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.joda.time.DateTimeConstants;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;

/**
 * Keeps information about a single registered vendorkey.
 * @author oflege
 */
@ThreadSafe
abstract class Registration<T> {
    // clients that only need differences as updates

    /**
     * clients for this registration; each element contains references to clients
     * that are allowed to access the same fields. For most
     * symbols and clients, this list is expected to be of size 1
     */
    @GuardedBy("clientsMutex")
    private final List<ProfiledClients<T>> profiledClients
            = new ArrayList<>(4);

    private final Object clientsMutex = new Object();

    // reference indicates whether any clients are registered, mark indicates snap update
    private final AtomicMarkableReference<Boolean> updated
            = new AtomicMarkableReference<>(Boolean.FALSE, false);

    /**
     * Maximum number of clients that used this registration. Helps to determine when to
     * evict the registration after all clients unregistered.
     */
    @GuardedBy("clientsMutex")
    private int maxNumClients = 0;

    /**
     * since when no clients are registered; orphaned registrations are expected to
     * be evicted after some time
     */
    @GuardedBy("clientsMutex")
    private long orphanSince = System.currentTimeMillis();

    protected final ByteString vwdcode;

    protected final String symbolVwdcode;

    public Registration(Quote quote) {
        this.symbolVwdcode = quote.getSymbolVwdcode();
        this.vwdcode = new ByteString(this.symbolVwdcode);
    }

    @Override
    public String toString() {
        return this.symbolVwdcode;
    }

    public String getRegistrationKey() {
        return symbolVwdcode;
    }

    ByteString getVwdcode() {
        return this.vwdcode;
    }

    protected final void pushUpdate(SnapRecord record) {
        if (!hasClients()) {
            return;
        }
        synchronized (this.clientsMutex) {
            for (ProfiledClients<T> clients : this.profiledClients) {
                clients.pushUpdate(record);
            }
        }
    }

    protected abstract T createComplete(SnapRecord sr);

    protected abstract T createDiff(T complete, T previous, BitSet allowedFields);

    protected abstract boolean isFreshUpdate(OrderedUpdate update);

    protected abstract boolean isFreshUpdate(ParsedRecord pr);

    protected abstract void push(Collection<AbstractClient> clients, T value);

    private ProfiledClients<T> createClients(BitSet allowedFields) {
        return new ProfiledClients<>(this, allowedFields);
    }

    /**
     * @return true iff current update status is false and we have clients; the update mark will
     *         be set to true so that the next call will return false.
     */
    boolean isFreshUpdate() {
        return this.updated.compareAndSet(Boolean.TRUE, Boolean.TRUE, false, true);
    }

    void clearUpdated() {
        // if reference is FALSE, the updated flag is false anyway
        this.updated.compareAndSet(Boolean.TRUE, Boolean.TRUE, true, false);
    }

    void removeClient(AbstractClient c) {
        synchronized (this.clientsMutex) {
            for (int i = 0; i < profiledClients.size(); i++) {
                ProfiledClients clients = profiledClients.get(i);
                if (clients.removeClient(c) == 0) {
                    this.profiledClients.remove(i);
                    break;
                }
            }
            if (this.profiledClients.isEmpty()) {
                this.orphanSince = System.currentTimeMillis();
                this.updated.set(Boolean.FALSE, false);
            }
        }
    }

    void addClient(AbstractClient c, BitSet allowedFields) {
        synchronized (this.clientsMutex) {
            for (ProfiledClients clients : profiledClients) {
                if (clients.getAllowedFields().equals(allowedFields)) {
                    clients.addClient(c);
                    return;
                }
            }
            final ProfiledClients<T> reg = createClients(allowedFields);
            this.profiledClients.add(reg);
            reg.addClient(c);
            if (this.orphanSince != 0L) {
                this.updated.set(Boolean.TRUE, false);
                this.orphanSince = 0L;
            }
        }
    }

    boolean hasClients() {
        return this.updated.getReference();
    }

    boolean isIdle(long now) {
        synchronized (this.clientsMutex) {
            if (this.orphanSince == 0L) {
                return false;
            }
            final long msIdle = now - this.orphanSince;
            if (this.maxNumClients < 2) {
                return msIdle > DateTimeConstants.MILLIS_PER_HOUR;
            }
            if (this.maxNumClients < 5) {
                return msIdle > DateTimeConstants.MILLIS_PER_DAY;
            }
            return msIdle > DateTimeConstants.MILLIS_PER_WEEK;
        }
    }
}
