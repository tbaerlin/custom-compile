/*
 * LatestNewsRequest.java
 *
 * Created on 15.03.2007 12:17:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LatestNewsRequest extends AbstractIstarRequest implements NewsRequestBase {
    protected static final Long serialVersionUID = 1L;

    private List<String> iids;

    private Profile profile;

    private boolean withText = true;

    private boolean withRawText;

    private boolean withGallery = false;

    public LatestNewsRequest() {
    }

    public List<String> getIids() {
        return this.iids;
    }

    public void setIids(List<String> iids) {
        this.iids = iids;
    }

    @Override
    public boolean isWithGallery() {
        return withGallery;
    }

    public void setWithGallery(boolean withGallery) {
        this.withGallery = withGallery;
    }

    @Override
    public Profile getProfile() {
        return this.profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Override
    public boolean isWithText() {
        return this.withText;
    }

    public void setWithText(boolean withText) {
        this.withText = withText;
    }

    @Override
    public boolean isWithAds() {
        return false;
    }

    @Override
    public DateTime getTo() {
        return null;
    }

    public EntitlementsVwd getEntitlements() {
        return null;
    }

    @Override
    public DateTime getFrom() {
        return null;
    }

    public boolean isWithRawText() {
        return withRawText;
    }

    public void setWithRawText(boolean withRawText) {
        this.withRawText = withRawText;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", profile=").append(this.profile.getName())
                .append(", iids=").append(this.iids)
                .append(withText ? ", withText" : "")
                .append(withRawText ? ", withRawText" : "");
    }

}