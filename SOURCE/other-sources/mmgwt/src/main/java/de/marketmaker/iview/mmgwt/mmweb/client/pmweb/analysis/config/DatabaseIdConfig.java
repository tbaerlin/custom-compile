package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Created on 17.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DatabaseIdConfig extends Config {
    private final String databaseId;

    protected DatabaseIdConfig(boolean privacyMode, HistoryToken historyToken, String layoutGuid, int chartHeight, int chartWidth, SafeHtml headerPrefix, String databaseId) {
        super(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, headerPrefix);
        if (!StringUtil.hasText(databaseId)) {
            throw new IllegalArgumentException("databaseId must be set!"); // $NON-NLS$
        }
        this.databaseId = databaseId;
    }

    @Override
    public String getId() {
        return this.databaseId;
    }

    @Override
    public String toString() {
        return "DatabaseIdConfig{databaseId='" + databaseId + '\'' + "} " + super.toString();  // $NON-NLS$
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseIdConfig)) return false;
        if (!super.equals(o)) return false;

        final DatabaseIdConfig that = (DatabaseIdConfig) o;

        return !(databaseId != null ? !databaseId.equals(that.databaseId) : that.databaseId != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (databaseId != null ? databaseId.hashCode() : 0);
        return result;
    }
}
