/*
 * ProfileResponse.java
 *
 * Created on 04.02.2008 11:35:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfileResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final Profile profile;

    private int timeToLive = 0;

    public static ProfileResponse invalid() {
        final ProfileResponse result = new ProfileResponse(null);
        result.setInvalid();
        return result;
    }

    public ProfileResponse(Profile profile) {
        this.profile = profile;
    }

    public ProfileResponse(String serverInfo, Profile profile) {
        super(serverInfo);
        this.profile = profile;
    }

    public Profile getProfile() {
        return this.profile;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public int getTimeToLive() {
        return this.timeToLive;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", ").append(this.profile != null ? this.profile.getName() : "null");
    }
}
