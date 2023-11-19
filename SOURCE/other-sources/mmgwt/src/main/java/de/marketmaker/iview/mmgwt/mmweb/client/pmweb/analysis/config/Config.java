package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.pmxml.DatabaseObject;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Created on 14.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@NonNLS
public abstract class Config {
    private final boolean privacyMode;
    private final SafeHtml headerPrefix;
    private final HistoryToken historyToken;
    protected final String layoutGuid;
    private final int chartHeight;
    private final int chartWidth;

    public static GlobalConfig createGlobal(HistoryToken historyToken, String layoutGuid, SafeHtml headerPrefix, boolean privacyMode) {
        return new GlobalConfig(privacyMode, historyToken, layoutGuid, -1, -1, headerPrefix);
    }

    public static HandleConfig createWithHandle(HistoryToken historyToken, String handle, String layoutGuid, SafeHtml headerPrefix) {
        return new HandleConfig(PrivacyMode.isActive(), historyToken, layoutGuid, -1, -1, headerPrefix, handle);
    }

    public static HandleConfig createWithHandle(HistoryToken historyToken, String handle, String layoutGuid, SafeHtml headerPrefix, Config sourceConfig, boolean privacyMode) {
        return new HandleConfig(privacyMode, historyToken, layoutGuid, -1, -1, headerPrefix, handle, sourceConfig);
    }

    public static DatabaseIdConfig createWithDatabaseId(HistoryToken historyToken, String databaseId, String layoutGuid, int chartHeight, int chartWidth, boolean privacyMode) {
        return new DatabaseIdConfig(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, null, databaseId);
    }

    public static DatabaseIdConfig createWithDatabaseId(HistoryToken historyToken, String databaseId, String layoutGuid, SafeHtml header, boolean privacyMode) {
        return new DatabaseIdConfig(privacyMode, historyToken, layoutGuid, -1, -1, header, databaseId);
    }

    public static DatabaseObjectConfig createWithDatabaseObject(HistoryToken historyToken, DatabaseObject dbo, String layoutGuid, SafeHtml header, boolean privacyMode) {
        return new DatabaseObjectConfig(privacyMode, historyToken, layoutGuid, header, dbo);
    }

    public static SecurityIdConfig createWithSecurityId(HistoryToken historyToken, String securityId, String layoutGuid, int chartHeight, int chartWidth, boolean privacyMode) {
        return new SecurityIdConfig(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, null, securityId);
    }

    protected Config(boolean privacyMode, HistoryToken historyToken, String layoutGuid, int chartHeight, int chartWidth, SafeHtml headerPrefix) {
        this.privacyMode = privacyMode;
        this.headerPrefix = headerPrefix;
        this.historyToken = historyToken;
        this.layoutGuid = layoutGuid;
        this.chartHeight = chartHeight;
        this.chartWidth = chartWidth;
    }

    public abstract String getId();

    public SafeHtml getHeaderPrefix() {
        return headerPrefix;
    }

    public HistoryToken getHistoryToken() {
        return historyToken;
    }

    public String getLayoutGuid() {
        return layoutGuid;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public boolean isPrivacyMode() {
        return privacyMode;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;

        final Config config = (Config) o;

        if (privacyMode != config.privacyMode) return false;
        if (chartHeight != config.chartHeight) return false;
        if (chartWidth != config.chartWidth) return false;
        if (headerPrefix != null ? !headerPrefix.equals(config.headerPrefix) : config.headerPrefix != null)
            return false;
        if (historyToken != null ? !historyToken.equals(config.historyToken) : config.historyToken != null)
            return false;
        return !(layoutGuid != null ? !layoutGuid.equals(config.layoutGuid) : config.layoutGuid != null);

    }

    @Override
    public int hashCode() {
        int result = (privacyMode ? 1 : 0);
        result = 31 * result + (headerPrefix != null ? headerPrefix.hashCode() : 0);
        result = 31 * result + (historyToken != null ? historyToken.hashCode() : 0);
        result = 31 * result + (layoutGuid != null ? layoutGuid.hashCode() : 0);
        result = 31 * result + chartHeight;
        result = 31 * result + chartWidth;
        return result;
    }

    @Override
    public String toString() {
        return "Config{" +
                "privacyMode=" + privacyMode +
                ", headerPrefix=" + headerPrefix +
                ", historyToken=" + historyToken +
                ", layoutGuid='" + layoutGuid + '\'' +
                ", chartHeight=" + chartHeight +
                ", chartWidth=" + chartWidth +
                '}';
    }
}