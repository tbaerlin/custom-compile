/*
 * ProfileFactory.java
 *
 * Created on Dec 27, 2002 10:12:22 AM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.profile.PermissionProvider;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * Factory for Profile objects.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfileFactory {
    private static Logger logger = LoggerFactory.getLogger(ProfileFactory.class);

    private static final Map<PermissionProvider, Profile> PROFILE_CACHE
            = Collections.synchronizedMap(new WeakHashMap<>());

    // ensure no instance can be created
    private ProfileFactory() {
    }

    public static Profile createInstance(PermissionProvider pp) {
        final Profile profile = PROFILE_CACHE.get(pp);
        if (profile != null) {
            return profile;
        }


        final DefaultProfile result = new DefaultProfile(pp);

        if (pp instanceof ResourcePermissionProvider) {
            final ResourcePermissionProvider rpp = (ResourcePermissionProvider) pp;
            return new ResourceProfile(result, rpp.getResourceKey());
        }

        PROFILE_CACHE.put(pp, result);
        logger.info("<createInstance> created profile " + result.getName()
                + ", cache size = " + PROFILE_CACHE.size());

        return result;
    }

    /**
     * Returns a profile in which either allows or denies everything.
     * @param allAllowed true if everything should be accessible, false if nothing
     * should be.
     * @return all or nothing profile
     */
    public static Profile valueOf(boolean allAllowed) {
        return allAllowed ? AllOrNothingProfile.ALL : AllOrNothingProfile.NOTHING;
    }

    public static void main(String[] args) {
        Profile profile = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance("iview"));
        System.out.println(profile);

    }
}
