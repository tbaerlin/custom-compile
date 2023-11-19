/*
 * DefaultProfile.java
 *
 * Created on Dec 27, 2002 10:13:38 AM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionProvider;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

import static de.marketmaker.istar.domain.profile.PermissionType.*;

/**
 * Default profile implementation.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultProfile implements Profile, Serializable {
    private static final long serialVersionUID = 650469899186654444L;

    private static final PermissionType[] NON_ASPECT_PERMISSION_TYPES = new PermissionType[]{
            PermissionType.FACTSET,
            PermissionType.FUNDDATA,
            PermissionType.QIDS_REALTIME,
            PermissionType.QIDS_DELAY
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProfile.class);

    private static class AspectItem implements Comparable<AspectItem> {
        private final int selector;

        private final short attributedSelector;

        private AspectItem(int selector, final PriceQuality priceQuality) {
            this.selector = selector;
            this.attributedSelector = (short) (selector | (priceQuality.ordinal() << AspectData.QUALITY_SHIFT));
        }

        @Override
        public int compareTo(AspectItem o) {
            return this.selector - o.selector;
        }

        @Override
        public String toString() {
            return this.selector + AspectData.QUALITY_NAMES[AspectData.decodeQuality(this.attributedSelector)];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return this.selector == ((AspectItem) o).selector;
        }

        @Override
        public int hashCode() {
            return this.selector;
        }
    }

    public static class AspectData implements Serializable {
        private static final int ID_MASK = 0x3FFF;

        private static final int QUALITY_MASK = 0xC000;

        private static final int QUALITY_SHIFT = 14;

        private static final PriceQuality QUALITIES[] = new PriceQuality[]{
                PriceQuality.REALTIME, PriceQuality.DELAYED, PriceQuality.END_OF_DAY
        };

        private static final String QUALITY_NAMES[] = new String[]{"RT", "NT", "EOD"};

        private final short[] attributedSelectors;

        private AspectData(Collection<AspectItem> items) {
            this.attributedSelectors = new short[items.size()];
            int i = 0;
            for (AspectItem item : items) {
                this.attributedSelectors[i++] = item.attributedSelector;
            }
        }

        private PriceQuality getPriceQuality(int n) {
            for (short as : attributedSelectors) {
                final int selector = as & 0xFFFF;
                if ((selector & ID_MASK) == n) {
                    return QUALITIES[selector >> QUALITY_SHIFT];
                }
            }
            return PriceQuality.NONE;
        }

        private boolean hasSelector(int n) {
            for (int selector : attributedSelectors) {
                if ((selector & ID_MASK) == n) {
                    return true;
                }
            }
            return false;
        }

        private BitSet toBitSet(PriceQuality pq) {
            final BitSet result = new BitSet();
            if (pq == PriceQuality.NONE) {
                return result;
            }
            if (pq == null) {
                for (int selector : attributedSelectors) {
                    result.set(selector & ID_MASK);
                }
                return result;
            }
            final int encodedQuality = pq.ordinal() << QUALITY_SHIFT;
            for (int selector : this.attributedSelectors) {
                if ((selector & QUALITY_MASK) == encodedQuality) {
                    result.set(selector & ID_MASK);
                }
            }
            return result;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(Math.max(16, attributedSelectors.length * 4));
            sb.append("[");
            for (int i = 0; i < this.attributedSelectors.length; ) {
                int j = i + 1;
                while (j < this.attributedSelectors.length && isSuccessor(i, j)) {
                    j++;
                }
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(EntitlementsVwd.toEntitlement(selector(i)));
                if (j > (i + 1)) {
                    sb.append("-").append(EntitlementsVwd.toEntitlement(selector(j - 1)));
                }
                sb.append('/').append(QUALITY_NAMES[quality(i)]);
                i = j;
            }
            return sb.append("]").toString();
        }

        private int selector(int i) {
            return this.attributedSelectors[i] & ID_MASK;
        }

        private int quality(int i) {
            return decodeQuality(this.attributedSelectors[i]);
        }

        private static int decodeQuality(short s) {
            return (s & QUALITY_MASK) >> QUALITY_SHIFT;
        }

        private boolean isSuccessor(int i, int j) {
            final int si = this.attributedSelectors[i];
            final int sj = this.attributedSelectors[j];
            return (sj - si) == (j - i) && ((si & QUALITY_MASK) == (sj & QUALITY_MASK));
        }

        public static AspectData create(Collection<String> rt, Collection<String> nt,
                Collection<String> eod) {
            if (rt.isEmpty() && nt.isEmpty() && eod.isEmpty()) {
                return null;
            }
            final Set<AspectItem> items = new TreeSet<>();
            collect(rt, items, PriceQuality.REALTIME);
            collect(nt, items, PriceQuality.DELAYED);
            collect(eod, items, PriceQuality.END_OF_DAY);
            return new AspectData(items);
        }

        private static void collect(Collection<String> ents, Set<AspectItem> items,
                final PriceQuality quality) {
            for (String ent : ents) {
                final String[] fromTo = ent.split("-");
                for (int i = EntitlementsVwd.toValue(fromTo[0]),
                        n = EntitlementsVwd.toValue(fromTo[fromTo.length - 1]); i <= n; i++) {
                    items.add(new AspectItem(i, quality));
                }
            }
        }
    }

    private static final AspectData NULL_ASPECT_DATA = new AspectData(Collections.<AspectItem>emptyList());

    private String name;

    private final BitSet entitlements;

    private final AspectData[] data = new AspectData[Aspect.values().length];

    private Map<PermissionType, Collection<String>> permissions
            = new EnumMap<>(PermissionType.class);

    public DefaultProfile(String name, AspectData priceAspect, AspectData newsAspect) {
        this(name, priceAspect, newsAspect, null);
    }

    private DefaultProfile(String name, AspectData priceAspect, AspectData newsAspect,
            Map<PermissionType, Collection<String>> permissions) {
        this.name = name;
        this.data[Aspect.PRICE.ordinal()] = priceAspect;
        this.data[Aspect.NEWS.ordinal()] = newsAspect;
        if (permissions != null) {
            this.permissions.putAll(permissions);
        }

        // TODO: uncomment and remove after all backends have been updated
//        this.entitlements = null;
        this.entitlements = createMigrationBitSet();
    }

    private BitSet createMigrationBitSet() {
        final BitSet result = new BitSet();
        for (AspectData ad: new AspectData[] {getData(Aspect.PRICE), getData(Aspect.NEWS)}) {
            set(result, ad.toBitSet(PriceQuality.REALTIME), 0);
            set(result, ad.toBitSet(PriceQuality.DELAYED), 520);
            set(result, ad.toBitSet(PriceQuality.END_OF_DAY), 1040);
        }
        return result;
    }

    private static void set(BitSet result, BitSet bs, final int offset) {
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            result.set(i + offset);
        }
    }

    public DefaultProfile(PermissionProvider pp) {
        this.name = pp.getName();
        this.data[Aspect.PRICE.ordinal()] = AspectData.create(pp.getPermissions(PRICES_REALTIME),
                pp.getPermissions(PRICES_DELAY), pp.getPermissions(PRICES_EOD));
        this.data[Aspect.NEWS.ordinal()] = AspectData.create(pp.getPermissions(NEWS_REALTIME),
                pp.getPermissions(NEWS_DELAY), Collections.emptyList());
        this.data[Aspect.PRODUCT.ordinal()] = AspectData.create(pp.getPermissions(PRODUCTS),
                Collections.emptySet(), Collections.emptySet());
        this.data[Aspect.STATIC.ordinal()] = AspectData.create(pp.getPermissions(STATIC),
                Collections.emptySet(), Collections.emptySet());
        for (PermissionType permissionType : NON_ASPECT_PERMISSION_TYPES) {
            this.permissions.put(permissionType, pp.getPermissions(permissionType));
        }

        // TODO: uncomment and remove after all backends have been updated
//        this.entitlements = null;
        this.entitlements = createMigrationBitSet();
    }

    /**
     * @deprecated
     */
    public DefaultProfile(String name, BitSet entitlements) {
        this.name = name;
        this.entitlements = new BitSet(entitlements.size());
        this.entitlements.or(entitlements);
    }

    protected Object readResolve() {
        if (!isDeprecated()) {
            return this;
        }

        // hack, look up local resource profile
        if (this.name.endsWith(".resource")) {
            final String rName = this.name.substring(0, this.name.length() - ".resource".length());
            final PermissionProvider pp = ResourcePermissionProvider.getInstance(rName);
            if (pp != null) {
                return new DefaultProfile(pp);
            }
        }
        else if (this.name.startsWith("resource:")) {
            final PermissionProvider pp = ResourcePermissionProvider.getInstance(this.name.substring("resource:".length()));
            if (pp != null) {
                return new DefaultProfile(new PermissionProvider() {
                    @Override
                    public String getName() {
                        return DefaultProfile.this.name;
                    }

                    @Override
                    public Collection<String> getPermissions(PermissionType type) {
                        return pp.getPermissions(type);
                    }
                });
            }
        }

        if (this.entitlements.nextSetBit(1561) >= 0) {
            final BitSet tooHigh = new BitSet();
            tooHigh.or(this.entitlements);
            tooHigh.clear(1, 1560);
            LOGGER.error("<readResolve> profile " + this.name + " has selector(s) > 1560: " + tooHigh);
        }
        final AspectData ad = AspectData.create(toEntitlements(this.entitlements, 1, 521),
                toEntitlements(this.entitlements, 521, 1041),
                toEntitlements(this.entitlements, 1041, 1561));
        return new DefaultProfile(this.name, ad, ad, this.permissions);
    }

    private boolean isDeprecated() {
        // will be null if a remote source does not know about this field
        //noinspection ConstantConditions
        return this.data == null;
    }

    private static Collection<String> toEntitlements(BitSet bits, int from, int to) {
        final HashSet<String> result = new HashSet<>();
        for (int i = bits.nextSetBit(from); i >= 0 && i < to; i = bits.nextSetBit(i + 1)) {
            result.add(EntitlementsVwd.toEntitlement(i - from + 1));
        }
        return result;
    }

    public String toString() {
        if (isDeprecated()) {
            return "DefaultProfile@deprecated[" + this.name + ", " + EntitlementsVwd.asString(this.entitlements) + "]";
        }
        return "DefaultProfile[" + this.name
                + ", prices=" + getData(Aspect.PRICE)
                + ", news=" + getData(Aspect.NEWS)
                + ", products=" + getData(Aspect.PRODUCT)
                + ", permissions=" + this.permissions
                + "]";
    }

    public String getName() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultProfile)) {
            return false;
        }

        final DefaultProfile defaultProfile = (DefaultProfile) o;
        return name.equals(defaultProfile.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public PriceQuality getPriceQuality(Quote quote) {
        final PriceQuality resultByQid = getPriceQualityByQid(quote);
        if (resultByQid != null) {
            return resultByQid;
        }

        final Entitlement entitlement = quote.getEntitlement();
        final String[] entitlements = entitlement.getEntitlements(KeysystemEnum.VWDFEED);
        PriceQuality result = null;

        // calc lowest PQ not equal to NONE
        for (String s : entitlements) {
            final int i = toEntitlementId(s);
            final PriceQuality quality = getData(Aspect.PRICE).getPriceQuality(i);

            if (quality != PriceQuality.NONE) {
                if (result == null) {
                    result = quality;
                }
                else {
                    result = PriceQuality.min(result, quality);
                }
            }
        }
        return (result == null) ? PriceQuality.NONE : result;
    }

    private AspectData getData(final Aspect aspect) {
        final AspectData result = this.data[aspect.ordinal()];
        return (result != null) ? result : NULL_ASPECT_DATA;
    }

    private int toEntitlementId(String entitlement) {
        return EntitlementsVwd.toValue(entitlement);
    }

    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return PriceQuality.NONE;
    }

    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        if (ks != KeysystemEnum.VWDFEED) {
            return PriceQuality.NONE;
        }
        final int n = toEntitlementId(entitlement);
        return getData(Aspect.PRICE).getPriceQuality(n);
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return PriceQuality.NONE;
    }

    private PriceQuality getPriceQualityByQid(Quote q) {
        if (hasQidPermission(PermissionType.QIDS_REALTIME, q)) {
            return PriceQuality.REALTIME;
        }
        if (hasQidPermission(PermissionType.QIDS_DELAY, q)) {
            return PriceQuality.DELAYED;
        }
        return null;
    }

    private boolean hasQidPermission(PermissionType type, Quote q) {
        final Collection<String> qids = this.permissions.get(type);
        return (qids != null) && qids.contains(Long.toString(q.getId()));
    }

    public Collection<String> getPermission(PermissionType type) {
        final Collection<String> values = this.permissions.get(type);
        return (values != null) ? values : Collections.<String>emptyList();
    }

    public boolean isAllowed(Aspect aspect, String key) {
        if (aspect == Aspect.FUNCTION) {
            if (getPermission(PermissionType.FACTSET).contains(key) ||
                    getPermission(PermissionType.FUNDDATA).contains(key)) {
                return true;
            }
        }
        // function selectors are stored with price selectors:
        final Aspect entitledAspect = (aspect == Aspect.FUNCTION) ? Aspect.PRICE : aspect;
        return getData(entitledAspect).hasSelector(toEntitlementId(key));
    }

    public boolean isAllowed(Selector selector) {
        for (final Aspect aspect : selector.getAspects()) {
            if (isAllowed(aspect, EntitlementsVwd.toNumericEntitlement(selector.getId()))) {
                return true;
            }
        }
        return false;
    }

    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        return getData(aspect).toBitSet(pq);
    }

    public Profile toAspectSpecificProfile(Aspect aspect) {
        return this;
    }
}
