/*
 * TeaserConfigData.java
 *
 * Created on 10/1/14 8:00 AM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.google.gwt.json.client.JSONObject;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
@SuppressWarnings("SimplifiableIfStatement")
public class TeaserConfigData {

    private final String version;

    private final String linkUrl;

    private final boolean linkEnabled;

    private final boolean teaserEnabled;

    private final String linkTarget;

    public TeaserConfigData(JSONObject jsonObject) {
        this.version = jsonObject.get("version").isString().stringValue();
        linkUrl = jsonObject.get("linkUrl").isString().stringValue();
        linkEnabled = jsonObject.get("linkEnabled").isBoolean().booleanValue();
        teaserEnabled = jsonObject.get("teaserEnabled").isBoolean().booleanValue();
        linkTarget = jsonObject.get("linkTarget").isString().stringValue();
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public boolean isLinkEnabled() {
        return linkEnabled;
    }

    public boolean isTeaserEnabled() {
        return teaserEnabled;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "TeaserConfigData{" +
                "version='" + version + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", linkEnabled=" + linkEnabled +
                ", teaserEnabled=" + teaserEnabled +
                ", linkTarget='" + linkTarget + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeaserConfigData)) return false;

        TeaserConfigData that = (TeaserConfigData) o;

        if (linkEnabled != that.linkEnabled) return false;
        if (teaserEnabled != that.teaserEnabled) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (linkUrl != null ? !linkUrl.equals(that.linkUrl) : that.linkUrl != null) return false;
        return !(linkTarget != null ? !linkTarget.equals(that.linkTarget) : that.linkTarget != null);

    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (linkUrl != null ? linkUrl.hashCode() : 0);
        result = 31 * result + (linkEnabled ? 1 : 0);
        result = 31 * result + (teaserEnabled ? 1 : 0);
        result = 31 * result + (linkTarget != null ? linkTarget.hashCode() : 0);
        return result;
    }
}
