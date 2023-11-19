/*
 * ProfileProviderCache.java
 *
 * Created on 04.02.2008 13:19:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfileProviderCache implements ProfileProvider {
    public static final Profile INVALID = ProfileFactory.valueOf(false);

    private Ehcache cache;

    private ProfileProvider provider;

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public void setProvider(ProfileProvider provider) {
        this.provider = provider;
    }

    public ProfileResponse getProfile(ProfileRequest request) {
        if (!request.isUseCache()) {
            final ProfileResponse response = doGetProfile(request);
            if (response.isValid() && "vwd-ent:ByLogin".equals(request.getAuthenticationType())) {
                // common case for mmf[web]: login by Login, subsequent requests with vwdId,
                // => add profile to the cache by its vwdId to speed things up
                addVwdProfileToCache(response);
            }
            return response;
        }

        final String key = request.toCacheKey();

        final Element element = this.cache.get(key);
        if (element != null) {
            final Profile profile = (Profile) element.getValue();
            if (profile == null) {
                return ProfileResponse.invalid();
            }
            return new ProfileResponse("cache", profile);
        }

        final ProfileResponse result = doGetProfile(request);

        addToCache(key, result);

        return result;
    }

    private void addVwdProfileToCache(ProfileResponse response) {
        final VwdProfile p = (VwdProfile) response.getProfile();
        addToCache(ProfileRequest.byVwdId(p.getVwdId(), p.getAppId()).toCacheKey(), response);
    }

    private void addToCache(String key, ProfileResponse result) {
        final Profile profile = result.getProfile();
        final Element e = new Element(key, profile);
        if (profile == null) {
            e.setTimeToLive(60);
        }
        if (result.getTimeToLive() > 0) {
            e.setTimeToLive(result.getTimeToLive());
        }
        this.cache.put(e);
    }

    private ProfileResponse doGetProfile(ProfileRequest request) {
        return this.provider.getProfile(request);
    }
}