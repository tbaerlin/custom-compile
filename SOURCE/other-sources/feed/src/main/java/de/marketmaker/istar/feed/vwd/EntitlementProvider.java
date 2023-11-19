/*
 * EntitlementProvider.java
 *
 * Created on 23.03.2006 13:05:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.BitSet;
import java.util.Set;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.Vendorkey;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface EntitlementProvider extends de.marketmaker.istar.domainimpl.EntitlementProvider { 
    /**
     * Returns an array of all possible entitlements for this vkey. If {@link
     * #getEntitlement} will be called for any field, the result will either be
     * a member of the array returned by this method or 0.
     * @param vkey
     * @return entitlements
     */
    int[] getEntitlements(String vkey);

    /**
     * Returns the entitlement for the given vendorkey and fieldid
     * @param vkey
     * @param fieldid
     * @return entitlement or 0 if no entitlement is defined.
     */
    int getEntitlement(String vkey, int fieldid);

    /**
     * Returns a set with all vwd market names for which there exists at least one quote
     * that is visible under the profile p
     * @param p profile
     * @return list of vwd market names
     */
    Set<String> getMarketNames(Profile p);

    /**
     * Returns an array of all possible entitlements for this vkey. If {@link
     * #getEntitlement} will be called for any field, the result will either be
     * a member of the array returned by this method or 0.
     * @param vkey
     * @return entitlements
     * @deprecated use {@link #getEntitlements(de.marketmaker.istar.feed.FeedData)}
     */
    int[] getEntitlements(Vendorkey vkey);

    /**
     * Returns the entitlement for the given vendorkey and fieldid
     * @param vkey
     * @param fieldid
     * @return entitlement or 0 if no entitlement is defined.
     * @deprecated use {@link #getEntitlement(de.marketmaker.istar.feed.FeedData, int)}
     */
    int getEntitlement(Vendorkey vkey, int fieldid);

    /**
     * Returns an array of all possible entitlements for this vkey. If {@link
     * #getEntitlement} will be called for any field, the result will either be
     * a member of the array returned by this method or 0.
     * @param data
     * @return entitlements
     */
    int[] getEntitlements(FeedData data);

    /**
     * Returns the entitlement for the given FeedData object and fieldid
     * @param data
     * @param fieldid
     * @return entitlement or 0 if no entitlement is defined.
     */
    int getEntitlement(FeedData data, int fieldid);

    /**
     * Returns a BitSet containing bits set for each entitled vwd field for the given key
     * @param vkey queried key
     * @return set of entitled fields
     */
    BitSet getFields(String vkey);

    /**
     * Returns a BitSet containing bits set for each entitled vwd field for the given vkey
     * @param vkey queried key
     * @return set of entitled fields
     */
    BitSet getFields(Vendorkey vkey);

    /**
     * Returns a BitSet containing bits set for each entitled vwd field for the given data
     * @param data queried data
     * @return set of entitled fields
     */
    BitSet getFields(FeedData data);

    /**
     * Returns a BitSet containing bits set for each vwd field for the given key and entitlement
     *
     * @param vkey queried key
     * @param entitlements limits the returned fields to those with any of these entitlement
     * @return set of entitled fields
     */
    BitSet getFields(String vkey, int... entitlements);

    /**
     * Returns a BitSet containing bits set for each vwd field for the given key and entitlement
     *
     * @param vkey queried key
     * @param entitlements limits the returned fields to those with any of these entitlement
     * @return set of entitled fields
     */
    BitSet getFields(Vendorkey vkey, int... entitlements);

    /**
     * Returns a BitSet containing bits set for each vwd field for the given key and entitlement
     *
     * @param data queried data
     * @param entitlements limits the returned fields to those with any of these entitlement
     * @return set of entitled fields
     */
    BitSet getFields(FeedData data, int... entitlements);

    /**
     * Returns a BitSet containing the field ids of fields that a user with profile p
     * is allowed to access for quote q with any of the price qualities in <tt>required</tt>
     * @param q entitled entity
     * @param profile contains users entitlements
     * @param required price qualities to be considered
     * @return BitSet with entitled fields
     */
    BitSet getAllowedFields(Quote q, Profile profile, Set<PriceQuality> required);

    /**
     * Returns a BitSet containing the field ids of fields that a user with profile p
     * is allowed to access for quote q;
     * @param q entitled entity
     * @param profile contains users entitlements
     * @return BitSet with entitled fields
     */
    BitSet getAllowedFields(Quote q, Profile profile);


    /**
     * Returns a BitSet containing the field ids of fields that a user with profile p
     * is allowed to request push access for;
     * @param q entitled entity
     * @param profile contains users entitlements
     * @return BitSet with entitled fields
     */
    BitSet getAllowedPushFields(Quote q, Profile profile);

    /**
     * Returns a BitSet containing the field ids of fields to delete if an EoD filter needs to be
     *  applied or null if no EoD filter is necessary at all.
     *
     *
     * @param q entitled entity
     * @param profile contains users entitlements
     * @return null or a BitSet with fields to delete for EoD quality
     */
    EndOfDayFilter getEoDFilter(Quote q, Profile profile);

    /**
     * Returns BitSet of fields to delete if Quote is entitled for a particular user.
     * A logical <b>AND</b> shall be performed on the result BitSet returned by {@link #getEoDFilter(Quote, Profile)}.
     *
     * @param q entitled entity
     * @param profile contains users profile
     */
    BitSet getAllowedEodFieldsByProfile(Quote q, Profile profile);
}
