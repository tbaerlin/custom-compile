/*
 * SearchResponse.java
 *
 * Created on 22.12.2004 14:05:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.SuggestIndexConstants;

/**
 * TODO:
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SuggestRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 123545L;

    private static String getNameField(Profile profile, InstrumentNameStrategy strategy) {
        if (strategy == InstrumentNameStrategies.WM_WP_NAME_KURZ) {
            return IndexConstants.FIELDNAME_WM_WP_NAME_KURZ;
        }
        if (FeatureFlags.Flag.NEW_WP_NAMES.isEnabled()) {
            if (profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)) {
                return SuggestIndexConstants.FIELDNAME_NAME_COST;
            }
            else {
                return SuggestIndexConstants.FIELDNAME_NAME_FREE;
            }
        }
        return IndexConstants.FIELDNAME_NAME;
    }

    private int limit = 10;

    private String query;

    private String strategy;

    private Profile profile;

    private String nameField;

    private InstrumentTypeEnum type;

    public SuggestRequest(Profile profile, String nameField) {
        this.profile = profile;
        this.nameField = nameField;
    }

    public SuggestRequest(Profile profile, InstrumentNameStrategy strategy) {
        this(profile, getNameField(profile, strategy));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("SuggestRequest['")
                .append(this.query).append("', limit=").append(this.limit).append(", strategy=")
                .append(this.strategy).append(", profile=").append(getProfileName());
        if (this.type != null) {
            sb.append(", type=").append(this.type);
        }
        return sb.append("]").toString();
    }

    private String getProfileName() {
        return this.profile != null ? this.profile.getName() : null;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public void setType(InstrumentTypeEnum type) {
        this.type = type;
    }

    public int getLimit() {
        return this.limit;
    }

    public String getQuery() {
        return this.query;
    }

    public Profile getProfile() {
        return this.profile;
    }

    public String getStrategy() {
        return this.strategy;
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    public String getNameField() {
        return nameField;
    }

    private Object readResolve() {
        if (this.nameField == null) {
            this.nameField = IndexConstants.FIELDNAME_NAME;
        }
        return this;
    }
}