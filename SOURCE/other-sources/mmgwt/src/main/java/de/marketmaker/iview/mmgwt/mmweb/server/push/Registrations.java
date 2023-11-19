/*
 * Registrations.java
 *
 * Created on 10.02.2010 07:04:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.push.PushRegistry;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Keeps information about registered vendorkeys and collects the registrations with updates
 * in a list that can be queried to create push updates periodically.
 * @author oflege
 */
@ThreadSafe
public class Registrations {
    private static final int FID_BEST_BID_1 = VwdFieldDescription.ADF_Best_Bid_1.id();

    private class RegistrationPair {
        private final PriceRegistration priceRegistration;

        private final OrderbookRegistration orderbookRegistration;

        private RegistrationPair(PriceRegistration priceRegistration,
                OrderbookRegistration orderbookRegistration) {
            this.priceRegistration = priceRegistration;
            this.orderbookRegistration = orderbookRegistration;
        }

        private void ackUpdateFor(OrderedUpdate update) {
            if (this.priceRegistration != null && this.priceRegistration.isFreshUpdate(update)) {
                Registrations.this.ackUpdateFor(this.priceRegistration);
            }
            if (this.orderbookRegistration != null && this.orderbookRegistration.isFreshUpdate(update)) {
                Registrations.this.ackUpdateFor(this.orderbookRegistration);
            }
        }

        private void ackUpdateFor(ParsedRecord pr) {
            if (this.priceRegistration != null && this.priceRegistration.isFreshUpdate(pr)) {
                Registrations.this.ackUpdateFor(this.priceRegistration);
            }
            if (this.orderbookRegistration != null && this.orderbookRegistration.isFreshUpdate(pr)) {
                Registrations.this.ackUpdateFor(this.orderbookRegistration);
            }
        }

        public boolean isIdle(long now) {
            if (this.priceRegistration != null && !this.priceRegistration.isIdle(now)) {
                return false;
            }
            if (this.orderbookRegistration != null && !this.orderbookRegistration.isIdle(now)) {
                return false;
            }
            return true;
        }

        public RegistrationPair withOrderbook(Quote q) {
            return new RegistrationPair(this.priceRegistration, new OrderbookRegistration(q));
        }

        public RegistrationPair withPrice(Quote q) {
            return new RegistrationPair(new PriceRegistration(q), this.orderbookRegistration);
        }
    }


    private final Map<ByteString, RegistrationPair> map = new HashMap<>();

    private PushRegistry pushRegistry;

    private final Object withUpdatesMutex = new Object();

    @GuardedBy("withUpdatesMutex")
    private List<Registration> withUpdates = new ArrayList<>();

    public void setPushRegistry(PushRegistry pushRegistry) {
        this.pushRegistry = pushRegistry;
    }

    void ackUpdateFor(ByteString key, OrderedUpdate update) {
        final RegistrationPair registration = getRegistration(key);
        if (registration != null) {
            registration.ackUpdateFor(update);
        }
    }

    void ackUpdateFor(ByteString key, ParsedRecord pr) {
        final RegistrationPair registration = getRegistration(key);
        if (registration != null) {
            registration.ackUpdateFor(pr);
        }
    }

    private RegistrationPair getRegistration(ByteString key) {
        synchronized (this.map) {
            return this.map.get(key);
        }
    }

    private Registration getRegistration(Quote q, boolean orderbook) {
        final ByteString key = new ByteString(q.getSymbolVwdcode());
        synchronized (this.map) {
            RegistrationPair existing = this.map.get(key);
            if (existing != null) {
                if (orderbook) {
                    if (existing.orderbookRegistration == null) {
                        existing = existing.withOrderbook(q);
                        this.map.put(key, existing);
                    }
                    return existing.orderbookRegistration;
                }
                else {
                    if (existing.priceRegistration == null) {
                        existing = existing.withPrice(q);
                        this.map.put(key, existing);
                    }
                    return existing.priceRegistration;
                }
            }

            final RegistrationPair pair = new RegistrationPair(
                    orderbook ? null : new PriceRegistration(q),
                    orderbook ? new OrderbookRegistration(q) : null
            );

            this.map.put(key, pair);
            this.pushRegistry.register(q.getSymbolVwdfeed());
            return orderbook ? pair.orderbookRegistration : pair.priceRegistration;
        }
    }


    int size() {
        synchronized (this.map) {
            return this.map.size();
        }
    }

    void ackUpdateFor(Registration registration) {
        synchronized (this.withUpdatesMutex) {
            this.withUpdates.add(registration);
        }
    }

    List<Registration> getWithUpdates() {
        synchronized (this.withUpdatesMutex) {
            if (this.withUpdates.isEmpty()) {
                return Collections.emptyList();
            }
            final List<Registration> result = this.withUpdates;
            this.withUpdates = new ArrayList<>(Math.max(10, result.size()));
            return result;
        }
    }

    void addClientFor(Quote q, AbstractClient client, BitSet allowedFields) {
        final boolean orderbook = allowedFields.get(FID_BEST_BID_1);
        client.add(getRegistration(q, orderbook), allowedFields);
    }

    void completeRegistrations() {
        this.pushRegistry.completeRegistrations();
    }

    int evictIdleRegistrations() {
        final long now = System.currentTimeMillis();
        int result = 0;
        synchronized (this.map) {
            final Iterator<Map.Entry<ByteString, RegistrationPair>> it = this.map.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<ByteString, RegistrationPair> entry = it.next();
                if (entry.getValue().isIdle(now)) {
                    this.pushRegistry.unregister(entry.getKey().toString());
                    it.remove();
                    result++;
                }
            }
        }
        return result;
    }
}
