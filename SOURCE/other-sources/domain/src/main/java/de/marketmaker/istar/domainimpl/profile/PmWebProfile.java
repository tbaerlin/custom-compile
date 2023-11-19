/*
 * PmWebProfile.java
 *
 * Created on 17.08.12 11:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Combines a pm login and license-key with a delegate profile.
 *
 * @author Michael Lösch
 * @author Markus Dick
 */
public class PmWebProfile implements Profile {
    private static final long serialVersionUID = 1L;

    private final VwdProfile delegate;

    private final String pmLogin;

    private final String license;

    private final Map<String, Boolean> features;

    public PmWebProfile(String pmLogin, String license, VwdProfile delegate, Map<String, Boolean> features) {
        this.delegate = delegate;
        this.license = license;
        this.pmLogin = pmLogin;
        this.features = features;
    }

    public VwdProfile getDelegate() {
        return delegate;
    }

    public String getPmLogin() {
        return pmLogin;
    }

    public String getLicense() {
        return license;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + this.pmLogin
                + ", " + this.license + ", delegate=" + this.delegate + "]";
    }


    @Override
    public String getName() {
        return this.delegate != null
                ? this.delegate.getName()
                : this.pmLogin;
    }

    @Override
    public PriceQuality getPriceQuality(Quote quote) {
        return this.delegate != null
                ? this.delegate.getPriceQuality(quote)
                : PriceQuality.NONE;
    }

    @Override
    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return this.delegate != null
                ? this.delegate.getPushPriceQuality(quote, entitlement)
                : PriceQuality.NONE;
    }

    @Override
    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.delegate != null
                ? this.delegate.getPriceQuality(entitlement, ks)
                : PriceQuality.NONE;
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.delegate != null
                ? this.delegate.getPushPriceQuality(entitlement, ks)
                : PriceQuality.NONE;
    }

    @Override
    public Collection<String> getPermission(PermissionType type) {
        return this.delegate != null
                ? this.delegate.getPermission(type)
                : Collections.<String>emptyList();
    }

    @Override
    public boolean isAllowed(Aspect aspect, String key) {
        return this.delegate != null && this.delegate.isAllowed(aspect, key);
    }

    @Override
    public boolean isAllowed(Selector selector) {
        return this.delegate != null && this.delegate.isAllowed(selector);
    }

    @Override
    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        return this.delegate != null
                ? this.delegate.toEntitlements(aspect, pq)
                : new BitSet();
    }

    @Override
    public Profile toAspectSpecificProfile(Aspect aspect) {
        return this.delegate != null
                ? this.delegate.toAspectSpecificProfile(aspect)
                : this;
    }

    public Map<String, Boolean> getFeatures() {
        return this.features;
    }
}
