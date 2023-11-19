/*
 * DelayProfileAdapter.java
 *
 * Created on 10.12.2008 16:21:52
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
import de.marketmaker.istar.domain.profile.ProfileAdapter;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EoDProfileAdapter implements ProfileAdapter, Serializable {
    private static final long serialVersionUID = 1L;

    private class EoDProfileAdapterProfile implements Profile, Serializable {
        private static final long serialVersionUID = 1L;

        private Profile delegate;

        private EoDProfileAdapterProfile(Profile delegate) {
            this.delegate = delegate;
        }

        public String getName() {
            return "EoD " + this.delegate.getName();
        }

        public PriceQuality getPriceQuality(Quote quote) {
            return adapt(this.delegate.getPriceQuality(quote));
        }

        public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
            return adapt(this.delegate.getPushPriceQuality(quote, entitlement));
        }

        private PriceQuality adapt(PriceQuality result) {
            return result == PriceQuality.NONE ? PriceQuality.NONE : PriceQuality.END_OF_DAY;
        }

        public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
            return adapt(this.delegate.getPriceQuality(entitlement, ks));
        }

        @Override
        public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
            return adapt(this.delegate.getPushPriceQuality(entitlement, ks));
        }

        public Collection<String> getPermission(PermissionType type) {
            // not really used anymore, just forward
            return this.delegate.getPermission(type);
        }

        public boolean isAllowed(Aspect aspect, String key) {
            return this.delegate.isAllowed(aspect, key);
        }

        public boolean isAllowed(Selector selector) {
            return this.delegate.isAllowed(selector);
        }

        public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
            // only used for news, so just forward
            return this.delegate.toEntitlements(aspect, pq);
        }

        public Profile toAspectSpecificProfile(Aspect aspect) {
            final Profile result = this.delegate.toAspectSpecificProfile(aspect);
            return (aspect != Aspect.PRICE)
                    ? result
                    : EoDProfileAdapter.this.adapt(result);
        }
    }

    public Profile adapt(Profile p) {
        return new EoDProfileAdapterProfile(p);
    }
}
