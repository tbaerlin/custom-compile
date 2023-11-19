/*
 * VwdProfileAdapter.java
 *
 * Created on 03.11.2008 16:05:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileAdapter;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * A simple ProfileAdapter that adapts a given profile with respect to
 * {@link de.marketmaker.istar.domain.profile.Profile.Aspect#PRICE}
 * permissions.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdProfileAdapter implements ProfileAdapter, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final List<SelectorItem> items;

    private final byte[] counterServiceResponse;

    public VwdProfileAdapter(List<SelectorItem> items, byte[] counterServiceResponse) {
        this.items = items;
        this.counterServiceResponse = counterServiceResponse;
    }

    static class SelectorItem implements Comparable<SelectorItem>, Serializable {
        private static final long serialVersionUID = 1L;

        private final int id;

        private final PriceQuality pq;

        private final boolean push;

        SelectorItem(int id, PriceQuality pq, boolean push) {
            this.id = id;
            this.pq = pq;
            this.push = push;
        }

        public int compareTo(SelectorItem o) {
            return this.id - o.id;
        }

        public int getId() {
            return id;
        }

        public PriceQuality getPq() {
            return pq;
        }

        public boolean isPush() {
            return push;
        }

        public String toString() {
            return this.id + " " + pq + " " + (this.push ? "push" : "pull");
        }

        public boolean isRestrictionFor(PriceQuality pq) {
            return pq != null && pq.ordinal() < this.pq.ordinal();
        }

        public boolean isRestrictionFor(int id, PriceQuality pq) {
            return this.id == id && isRestrictionFor(pq);
        }
    }

    private class VwdProfileAdapterProfile implements Profile, Serializable {
        private static final long serialVersionUID = 1L;

        private Profile delegate;

        public VwdProfileAdapterProfile(Profile p) {
            this.delegate = p;
        }

        public String getName() {
            return "Adapted " + this.delegate.getName();
        }

        public Collection<String> getPermission(PermissionType type) {
            // not really used anymore, just forward
            return this.delegate.getPermission(type);
        }

        public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
            return applyRestriction(entitlement, this.delegate.getPriceQuality(entitlement, ks));
        }

        @Override
        public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
            return applyRestriction(entitlement, this.delegate.getPushPriceQuality(entitlement, ks));
        }

        private PriceQuality applyRestriction(String entitlement, PriceQuality result) {
            final int id = toEntitlementId(entitlement);
            for (SelectorItem item : items) {
                if (item.isRestrictionFor(id, result)) {
                    return item.getPq();
                }
            }
            return result;
        }

        private PriceQuality getPriceQuality(Quote quote, int requiredEnt, boolean pushRequired) {
            final PriceQuality result = this.delegate.getPriceQuality(quote);
            final Entitlement entitlement = quote.getEntitlement();
            final String[] entitlements = entitlement.getEntitlements(KeysystemEnum.VWDFEED);
            for (String s : entitlements) {
                final int id = toEntitlementId(s);
                if (requiredEnt != 0 && requiredEnt != id) {
                    continue;
                }
                for (SelectorItem item : items) {
                    if (item.isRestrictionFor(id, result) && (!pushRequired || item.isPush())) {
                        return item.getPq();
                    }
                }
            }
            return result;
        }

        public PriceQuality getPriceQuality(Quote quote) {
            return getPriceQuality(quote, 0, false);
        }

        public PriceQuality getPushPriceQuality(Quote quote, String e) {
            return getPriceQuality(quote, (e != null) ? toEntitlementId(e) : 0, true);
        }

        public boolean isAllowed(Aspect aspect, String key) {
            final boolean allowed = this.delegate.isAllowed(aspect, key);
            if (!allowed || aspect != Aspect.PRICE) {
                return allowed;
            }
            final int id = toEntitlementId(key);
            for (final SelectorItem item : items) {
                if (item.getId() == id && item.getPq() == PriceQuality.NONE) {
                    return false;
                }
            }
            return true;
        }

        public boolean isAllowed(Selector selector) {
            for (final Aspect aspect : selector.getAspects()) {
                if (isAllowed(aspect, Integer.toString(selector.getId()))) {
                    return true;
                }
            }
            return false;
        }

        public Profile toAspectSpecificProfile(Aspect aspect) {
            final Profile result = this.delegate.toAspectSpecificProfile(aspect);
            return (aspect != Aspect.PRICE)
                    ? result
                    : VwdProfileAdapter.this.adapt(result);
        }

        public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
            final BitSet result = this.delegate.toEntitlements(aspect, pq);
            if (aspect != Aspect.PRICE) {
                return result;
            }
            for (SelectorItem item : items) {
                if (result.get(item.getId()) && item.isRestrictionFor(pq)) {
                    result.clear(item.getId());
                }
            }
            return null;
        }

        public String toString() {
            return "VwdProfileAdapterProfile[" + items + ", " + this.delegate + "]";
        }
    }

    private static int toEntitlementId(String entitlement) {
        return EntitlementsVwd.toValue(entitlement);
    }

    public String toString() {
        return "VwdProfileAdapter" + this.items;
    }

    public String getCounterServiceResponse() {
        if (this.counterServiceResponse == null) {
            return null;
        }
        return new String(this.counterServiceResponse, UTF_8);
    }

    public Profile adapt(Profile p) {
        for (SelectorItem item : items) {
            if (item.isRestrictionFor(p.getPriceQuality("" + item.id, KeysystemEnum.VWDFEED))) {
                // TODO: check push/pull as soon as that is available from Profile interface
                return new VwdProfileAdapterProfile(p);
            }
        }
        return p;
    }
}
