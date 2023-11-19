/*
 * TestProfile.java
 *
 * Created on 26.06.2009 14:35:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

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
 * Profile to be used in test cases, subclasses are expected to override methods as needed.
 * Cannot be in test path, as it could not be accessed by tests in different modules.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TestProfile  implements Profile {
    private final String name;

    public TestProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "ProfileAdapter[" + this.name + "]";
    }

    public PriceQuality getPriceQuality(Quote quote) {
        return PriceQuality.NONE;
    }

    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return PriceQuality.NONE;
    }

    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        return PriceQuality.NONE;
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return PriceQuality.NONE;
    }

    public Collection<String> getPermission(PermissionType type) {
        return Collections.emptyList();
    }

    public boolean isAllowed(Aspect aspect, String key) {
        return false;
    }

    public boolean isAllowed(Selector selector) {
        return false;
    }

    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        return new BitSet();
    }

    public Profile toAspectSpecificProfile(Aspect aspect) {
        return this;
    }
}
