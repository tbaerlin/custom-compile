/*
 * DelayProfileAdapter.java
 *
 * Created on 10.12.2008 16:21:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileAdapter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IdentityProfileAdapter implements ProfileAdapter {
    public static final ProfileAdapter INSTANCE = new IdentityProfileAdapter();

    private IdentityProfileAdapter() {
    }

    public Profile adapt(Profile p) {
        return p;
    }
}