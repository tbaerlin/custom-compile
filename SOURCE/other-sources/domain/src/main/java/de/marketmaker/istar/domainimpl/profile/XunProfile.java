/*
 * XunProfile.java
 *
 * Created on 31.01.2008 11:16:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 * Last version xun2selectors:

 GENOB_B=7A,6Y,27C,27E,27F,27J,27S,28C,28G,28R,33I,33T,33Y,33Z,34Y,35Z,36D,36E,14I,15U,37W,8N,16G
 GENOB=1X,6F,7A,6Y,27C,27E,27F,27J,27S,28C,28G,28R,33I,33T,33Y,33Z,34Y,35Z,36D,36E,14I,15U,37W,8N,16G
 GENOB_L=1X,6F,7A,6Y,27C,27E,27F,27J,27S,28C,28G,28R,33I,33T,33Y,33Z,34Y,35Z,36D,36E,14I,15U,37W,8N,16G
 DBFF=8G
 DBFFSN=8G
 DBIN=15Z
 DBINSN=15Z
 EUREX=16D,16E,17W
 EUREXS=16D,16E,17W
 EUX_MT=13Y,16D,16E,17W
 EUX_MS=13Y,16D,16E,17W
 EUWAX=8R
 EUWAXSN=8R
 KMD=7C
 KMDSN=7C
 KMD_MT=7C,13T
 KMD_MS=7C,13T
 KMR=8C
 KMRSN=8C
 NEWEX=13I
 NEWEXSN=13I
 STOZ=7S
 STOZSN=7S
 X_ES=7F
 X_ES_SN=7F
 X_US=7E
 X_US_SN=7E
 X_US_MT=7J,7E
 X_US_MS=7J,7E
 SNAP=7A,7C,7E,7F,7S,8C,8R,13I,13Z,14Y,15Z,16D,16E,17W
 SNAPZ=7A,7C,7E,7F,7S,8C,8R,13I,13Z,14Y,15Z,16D,16E,17W
 SNAPR=7A,7C,7E,7F,7S,8C,8R,13I,13Z,14Y,15Z,16D,16E,17W
 SNAPD=7A,7C,7E,7F,7S,8C,8R,13I,13Z,14Y,15Z,16D,16E,17W

 */
@Deprecated
public final class XunProfile implements Serializable, Profile {
    static final long serialVersionUID = 1L;

    public enum Item {
        GTR, GIN, GINWEB, GIF, GIFWEB, GENOB, GENOB_B, GENOB_L, DBFF, DBFFSN, DBIN, DBINSN,
        EUREX, EUREXS, EUX_MT, EUX_MS, EUWAX, EUWAXSN, KMD, KMDSN, KMD_MT, KMD_MS, KMR, KMRSN,
        NEWEX, NEWEXSN, STOZ, STOZSN, X_ES, X_ES_SN, X_US, X_US_SN, X_US_MT, X_US_MS, SNAP, SNAPZ,
        SNAPR, SNAPD;

        public static Item get(String s) {
            return Item.valueOf(s.replace('-', '_'));
        }
    }

    public final static XunProfile EMPTY
            = new XunProfile("NULL", EnumSet.noneOf(Item.class), ProfileFactory.valueOf(false));

    private final String name;

    private final Set<Item> items;

    private final Profile delegate;

    public XunProfile(String name, EnumSet<Item> items, Profile delegate) {
        this.name = name;
        this.items = items;
        this.delegate = delegate;
    }

    public String getName() {
        return "Xun:" + this.name;
    }

    public String toString() {
        return "XunProfile[" + this.name + ", " + this.items + "]";
    }

    public boolean containsAny(Set<Item> otherItems) {
        return !Collections.disjoint(this.items,  otherItems);
    }

    public boolean isAllowed(String s) {
        try {
            return this.items.contains(Item.get(s));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Collection<String> getPermission(PermissionType type) {
        // TODO: HACK for DZ BANK
        if (type == PermissionType.FUNDDATA) {
            if (this.delegate.isAllowed(Selector.FUNDDATA_MORNINGSTAR_DE)) {
                return Collections.emptyList();
            }
            return Collections.singletonList("FERI");
        }
        return this.delegate.getPermission(type);
    }

    public boolean isAllowed(Aspect aspect, String key) {
        return this.delegate.isAllowed(aspect, key);
    }

    public boolean isAllowed(Selector selector) {
        return this.delegate.isAllowed(selector);
    }

    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        return this.delegate.toEntitlements(aspect, pq);
    }

    public Profile toAspectSpecificProfile(Aspect aspect) {
        return this.delegate.toAspectSpecificProfile(aspect);
    }

    public PriceQuality getPriceQuality(Quote quote) {
        return this.delegate.getPriceQuality(quote);
    }

    public PriceQuality getPushPriceQuality(Quote quote, String entitlement) {
        return this.delegate.getPushPriceQuality(quote, entitlement);
    }

    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.delegate.getPriceQuality(entitlement, ks);
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        return this.delegate.getPushPriceQuality(entitlement, ks);
    }

}
