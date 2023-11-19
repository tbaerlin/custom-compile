/*
 * NewsRequestBase.java
 *
 * Created on 29.09.2009 14:15:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsRequestBase {
    Profile getProfile();

    void setProfile(Profile profile);

    DateTime getFrom();

    DateTime getTo();

    boolean isWithAds();

    boolean isWithText();

    boolean isWithGallery();

}
