/*
 * ProfileAdapter.java
 *
 * Created on 03.11.2008 15:14:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ProfileAdapter {
    /**
     * Returns an adapted version of the given profile
     * @param p adaptee
     * @return adapted profile
     */
    Profile adapt(Profile p);
}
