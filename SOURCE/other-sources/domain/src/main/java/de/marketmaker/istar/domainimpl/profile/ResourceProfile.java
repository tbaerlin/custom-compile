/*
 * ResourceProfile.java
 *
 * Created on 24.08.2005 14:48:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ResourceProfile implements Profile, Serializable {
    private static final long serialVersionUID = 650462L;

    private final Profile delegate;
    private final String name;

    public ResourceProfile(Profile delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public PriceQuality getPriceQuality(Quote quote) {
        return this.delegate.getPriceQuality(quote);
    }

    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return PriceQuality.NONE;
    }

    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.delegate.getPriceQuality(entitlement, ks);
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return PriceQuality.NONE;
    }

    public Collection<String> getPermission(PermissionType type) {
        return this.delegate.getPermission(type);
    }

    public boolean isAllowed(Aspect aspect, String key) {
        return delegate.isAllowed(aspect, key);
    }

    public boolean isAllowed(Selector selector) {
        return delegate.isAllowed(selector);
    }

    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        return this.delegate.toEntitlements(aspect, pq);
    }

    public Profile toAspectSpecificProfile(Aspect aspect) {
        return this.delegate.toAspectSpecificProfile(aspect);
    }

    public String toString() {
        return "ResourceProfile[name=" + this.name + ", delegate=" + this.delegate + "]";
    }
}
