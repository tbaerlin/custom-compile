/*
 * PmAboProfile.java
 *
 * Created on 17.07.12 11:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * Combines a pm abo, a fixed news permission, and a delegate profile.
 * @author oflege
 */
public class PmAboProfile implements Profile {
    private static final long serialVersionUID = 284140545643269964L;

    private static final int DELAY_OFFSET = 520;

    private static Collection<String> NEWS_FOR_ALL = Collections.singleton("1P");

    private static final BitSet DEFAULT_NEWS_BITS = new BitSet();

    private static Collection<String> DEFAULT_NEWS = new HashSet<>(Arrays.asList("1P", "1T", "19E"));

    private static BitSet NEWS_BITS = new BitSet();

    private static BitSet DELAYED_NEWS_BITS = new BitSet();

    private static final Set<String> WISO_BASIC_ABO = Collections.singleton("P");

    static {
        for (String ent : DEFAULT_NEWS) {
            addEntitlement(EntitlementsVwd.toValue(ent));
        }
        DEFAULT_NEWS_BITS.set(EntitlementsVwd.toValue("1P"));
    }

    static void addEntitlement(int i) {
        NEWS_BITS.set(i);
        DELAYED_NEWS_BITS.set(i + DELAY_OFFSET);
    }

    /**
     * Maps wiso abo names to "normal" abo names
     */
    private static final Map<String, Collection<String>> WISO_ABO_MAPPINGS
            = new HashMap<>();

    static {
        WISO_ABO_MAPPINGS.put("WB-2", Collections.singleton("2"));  // Plus
        WISO_ABO_MAPPINGS.put("WB-D", Collections.singleton("D"));  // Deutschland
        WISO_ABO_MAPPINGS.put("WB-FP", Collections.singleton("8")); // FondsPlus
        WISO_ABO_MAPPINGS.put("WB-ID", Collections.singleton("1")); // Intraday
        WISO_ABO_MAPPINGS.put("WB-IDM", Collections.singleton("M")); // Intraday Online Service Mini
        WISO_ABO_MAPPINGS.put("WB-M+K", Collections.singleton("9")); // Markt- und Konjunkturdaten
        WISO_ABO_MAPPINGS.put("WB-N", Collections.singleton("N")); // News
//        ABO_NAME_MAPPINGS.put("WB-PW", ""); ???
        WISO_ABO_MAPPINGS.put("WB-W", Arrays.asList("D", "E", "U")); // Welt
    }


    private static final String NEWS_ABO = "N";

    private final Set<String> abos;

    private final String kennung;

    private final Profile delegate;

    private final boolean withNews;

    /**
     * Creates a profile for an "anonymous" wiso boerse user.
     */
    public PmAboProfile(String login) {
        this(login, WISO_BASIC_ABO, ProfileFactory.valueOf(false));
    }

    public PmAboProfile(String kennung, Collection<String> abos, Profile delegate) {
        this.kennung = kennung;
        this.abos = new HashSet<>();
        for (String abo : abos) {
            if (WISO_ABO_MAPPINGS.containsKey(abo)) {
                this.abos.addAll(WISO_ABO_MAPPINGS.get(abo));
            }
            else {
                this.abos.add(abo);
            }
        }
        this.withNews = this.abos.contains(NEWS_ABO);
        this.delegate = delegate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + this.kennung
                + ", " + this.abos + ", delegate=" + this.delegate + "]";
    }

    public boolean isWisoBasicAbo() {
        return this.abos.equals(WISO_BASIC_ABO);
    }

    @Override
    public String getName() {
        return this.kennung;
    }

    public Set<String> getAbos() {
        return Collections.unmodifiableSet(this.abos);
    }

    @Override
    public PriceQuality getPriceQuality(Quote quote) {
        final PriceQuality aboQuality = getAboQuality(quote);
        final PriceQuality priceQuality = this.delegate.getPriceQuality(quote);
        return PriceQuality.max(aboQuality, priceQuality);
    }

    @Override
    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return this.delegate.getPushPriceQuality(quote, entitlement);
    }

    @Override
    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        final PriceQuality aboQuality = (ks == KeysystemEnum.MM)
            ? getAboQuality(entitlement) : PriceQuality.NONE;
        final PriceQuality priceQuality = this.delegate.getPriceQuality(entitlement, ks);
        return PriceQuality.max(aboQuality, priceQuality);
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.delegate.getPushPriceQuality(entitlement, ks);
    }

    @Override
    public Collection<String> getPermission(PermissionType type) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAllowed(Aspect aspect, String key) {
        if (aspect == Aspect.NEWS) {
            final int ent = EntitlementsVwd.toValue(key);
            if (!this.withNews) {
                return DEFAULT_NEWS_BITS.get(ent);
            }
            return NEWS_BITS.get(ent) || DELAYED_NEWS_BITS.get(ent);
        }
        return this.delegate.isAllowed(aspect, key);
    }

    @Override
    public boolean isAllowed(Selector selector) {
        for (String pmAbo : selector.getPmAbos()) {
            if (this.abos.contains(pmAbo)) {
                return true;
            }
        }
        for (Aspect aspect : selector.getAspects()) {
            if (isAllowed(aspect, "" + selector.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        if (aspect == Aspect.NEWS) {
            if (!this.withNews) {
                return DEFAULT_NEWS_BITS;
            }
            return (pq == null || pq == PriceQuality.REALTIME) ? NEWS_BITS : DELAYED_NEWS_BITS;
        }
        return this.delegate.toEntitlements(aspect, pq);
    }

    @Override
    public Profile toAspectSpecificProfile(Aspect aspect) {
        if (aspect == Aspect.NEWS) {
            if (!this.withNews) {
                return new DefaultProfile(new SimplePermissionProvider(getName() + "/DEFAULTNEWS",
                        PermissionType.NEWS_DELAY, NEWS_FOR_ALL));
            }
            return new DefaultProfile(new SimplePermissionProvider(getName() + "/NEWS",
                    PermissionType.NEWS_DELAY, DEFAULT_NEWS));
        }
        return this.delegate.toAspectSpecificProfile(aspect);
    }

    public PriceQuality getAboQuality(Quote quote) {
        final String[] entitlements = quote.getEntitlement().getEntitlements(KeysystemEnum.MM);
        return entitlements != null ? getAboQuality(entitlements) : PriceQuality.NONE;
    }

    private PriceQuality getAboQuality(String... entitlements) {
        for (String entitlement : entitlements) {
            if (this.abos.contains(entitlement)) {
                return PriceQuality.END_OF_DAY;
            }
        }
        return PriceQuality.NONE;
    }
}
