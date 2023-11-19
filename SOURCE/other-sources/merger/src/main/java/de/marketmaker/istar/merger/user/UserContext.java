/*
 * UserContext.java
 *
 * Created on 03.08.2006 09:32:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserContext {
    private final User user;
    private final Company company;
    private final Profile profile;

    public UserContext(User user, Company company, Profile profile) {
        this.user = user;
        this.company = company;
        this.profile = profile;
    }

    public Company getCompany() {
        return this.company;
    }

    public Profile getProfile() {
        return this.profile;
    }

    public User getUser() {
        return this.user;
    }

    public String getProperty(String key) {
        final String value = this.user.getProperty(key);
        return value != null ? value : this.company.getProperty(key);
    }
}
