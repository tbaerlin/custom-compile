/*
 * WMDataRequest.java
 *
 * Created on 02.11.11 18:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author oflege
 */
public class WMDataRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 7059807933787617900L;

    private Set<Long> iids;

    private Set<String> isins;

    private final Profile profile;

    /**
     * Only to be used for instruments not known in istar systems.
     */
    public WMDataRequest(Profile profile, String... isins) {
        this.isins = new HashSet<>(Arrays.asList(isins));
        this.profile = profile;
    }

    public WMDataRequest(Profile profile, Long... iids) {
        this(profile, Arrays.asList(iids));
    }

    public WMDataRequest(Profile profile, Collection<Long> iids) {
        this.iids = new HashSet<>(iids);
        this.profile = profile;
    }

    public Set<Long> getIids() {
        return this.iids == null ? Collections.<Long>emptySet() : this.iids;
    }

    public Set<String> getIsins() {
        return this.isins == null ? Collections.<String>emptySet() : this.isins;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", iids=").append(this.iids);
        sb.append(", isins=").append(this.isins);
        sb.append(", profile=").append(this.profile);
    }
}
