/*
 * Profile.java
 *
 * Created on 10.07.2006 17:06:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.profile;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Profile extends Serializable {
    static final long serialVersionUID = 545217028120161451L;

    /**
     * vwd Permissionierungssystem Mapping:
     * <ol>
     *     <li>Typ: Datenselektor, Kategorie: Nachrichten == NEWS</li>
     *     <li>Typ: Datenselektor, Kategorie: * ~= PRICE</li>
     *     <li>Typ: Funktionsselektor, Kategorie: Seite == PAGE</li>
     *     <li>Typ: Funktionsselektor, Kategorie: Produkt == PRODUCT</li>
     *     <li>Typ: Funktionsselektor, Kategorie: Funktion == FUNCTION</li>
     * </ol>
     */
    enum Aspect {
        PRICE, NEWS, PAGE, PRODUCT, FUNCTION, STATIC
    }

    /**
     * Returns an identifier for the profile
     * @return profile's name
     */
    String getName();

    /**
     * @param quote quote for which data is requested
     * @return best available price quality for the given quote
     */
    PriceQuality getPriceQuality(Quote quote);

    /**
     *
     * @param quote quote for which data is requested
     * @param entitlement if not null, do only consider this entitlement instead of all for the quote
     * @return best available price quality for pushed data of the given quote
     */
    PriceQuality getPushPriceQuality(Quote quote, String entitlement);

    /**
     * Returns price quality for some entitlement
     * @param entitlement to be checked
     * @param ks entitlement's keysystem
     * @return price quality
     */
    PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks);

    /**
     * Returns price quality for some entitlement, requiring push mode
     * @param entitlement to be checked
     * @param ks entitlement's keysystem
     * @return price quality
     */
    PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks);

    /**
     * @param type permission type
     * @return collection of permissions for the given type
     * @deprecated use {@link #isAllowed(de.marketmaker.istar.domain.profile.Profile.Aspect, String)}
     *             with Aspect.FUNCTION and an appropriate key
     */
    Collection<String> getPermission(PermissionType type);

    /**
     * Returns whether a specific selector is part of this profile; useful for Aspects that do
     * not contain qualities for a selector but just store allowed selectors
     * @param aspect check selector for this aspect; must not be null
     * @param key selector
     * @return whether the selector is present for the aspect
     */
    boolean isAllowed(Aspect aspect, String key);

    /**
     * Returns whether a specific selector is part of this profile.
     * @param selector enum that contains an aspect and an id
     * @return whether the selector is present
     */
    boolean isAllowed(Selector selector);

    /**
     * Returns a BitSet with all entitlements for the given aspect
     * @param aspect which entitlements to retrieve
     * @param pq required PriceQuality, null for unspecified
     * @return entitlements
     */
    BitSet toEntitlements(Aspect aspect, PriceQuality pq);

    /**
     * Returns a profile based on this one that is only guaranteed to return valid information
     * for the given Aspect. Useful to reduce the size of objects that have to be serialized
     * (a NewsRequest may carry a profile that just contains data for Aspect.NEWS).
     * @param aspect the sole aspect that is required
     * @return possibly an adapted Profile, may also return this.
     */
    Profile toAspectSpecificProfile(Aspect aspect);
}
