/*
 * AllOrNothingProfile.java
 *
 * Created on Dec 27, 2002 10:27:14 AM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * Profile that either allows or denies access to everything, implemented as a
 * dualton (or whatever a singleton with two instances is called).
 * Use {@link ProfileFactory#valueOf} to obtain instance
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
final class AllOrNothingProfile implements Profile, Serializable {
    private static final long serialVersionUID = -3086434778860291728L;

    static final Profile ALL = new AllOrNothingProfile(true);

    static final Profile NOTHING = new AllOrNothingProfile(false);

    private static final int MAX_ENTITLEMENT = 5000;

    /**
     * whether everything is allowed (true) or nothing is allowed (false)
     */
    private final boolean isAll;

    // private contructor, use {@link ProfileFactory#valueOf} to obtain instance
    private AllOrNothingProfile(boolean isAll) {
        this.isAll = isAll;
    }

    public PriceQuality getPriceQuality(Quote quote) {
        return this.isAll ? PriceQuality.REALTIME : PriceQuality.NONE;
    }

    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return this.isAll ? PriceQuality.REALTIME : PriceQuality.NONE;
    }

    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.isAll ? PriceQuality.REALTIME : PriceQuality.NONE;
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.isAll ? PriceQuality.REALTIME : PriceQuality.NONE;
    }

    public Collection<String> getPermission(PermissionType type) {
        return Collections.emptyList();
    }

    public boolean isAllowed(Aspect aspect, String key) {
        return this.isAll;
    }

    public boolean isAllowed(Selector selector) {
        return this.isAll;
    }

    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        if (!this.isAll) {
            return new BitSet(0);
        }
        final BitSet result = new BitSet(MAX_ENTITLEMENT);
        result.set(1, MAX_ENTITLEMENT);
        return result;
    }

    public Profile toAspectSpecificProfile(Aspect aspect) {
        return this;
    }

    protected Object readResolve() {
        return isAll ? ALL : NOTHING;
    }    

    public String toString() {
        return "AllOrNothingProfile[" + getName() + "]";
    }

    public String getName() {
        return this.isAll ? "all" : "nothing";
    }
}
