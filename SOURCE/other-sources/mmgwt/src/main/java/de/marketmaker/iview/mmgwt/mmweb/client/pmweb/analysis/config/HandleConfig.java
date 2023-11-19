package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Created on 17.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@NonNLS
public class HandleConfig extends Config {
    private final String handle;
    private final Config sourceConfig;

    protected HandleConfig(boolean privacyMode, HistoryToken historyToken, String layoutGuid, int chartHeight, int chartWidth, SafeHtml headerPrefix, String handle, Config sourceConfig) {
        super(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, headerPrefix);
        if (!StringUtil.hasText(handle)) {
            throw new IllegalArgumentException("handle must be set!");
        }
        this.handle = handle;
        this.sourceConfig = sourceConfig;
    }

    protected HandleConfig(boolean privacyMode, HistoryToken historyToken, String layoutGuid, int chartHeight, int chartWidth, SafeHtml headerPrefix, String handle) {
        this(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, headerPrefix, handle, null);
    }

    @Override
    public String getId() {
        return this.handle;
    }

    public boolean hasSourceConfig() {
        return this.sourceConfig != null;
    }

    public Config getSourceConfig() {
        return this.sourceConfig;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandleConfig)) return false;
        if (!super.equals(o)) return false;

        final HandleConfig that = (HandleConfig) o;

        if (handle != null ? !handle.equals(that.handle) : that.handle != null) return false;
        return !(sourceConfig != null ? !sourceConfig.equals(that.sourceConfig) : that.sourceConfig != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (handle != null ? handle.hashCode() : 0);
        result = 31 * result + (sourceConfig != null ? sourceConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HandleConfig{" +
                "handle='" + handle + '\'' +
                ", sourceConfig=" + sourceConfig +
                "} " + super.toString();
    }
}
