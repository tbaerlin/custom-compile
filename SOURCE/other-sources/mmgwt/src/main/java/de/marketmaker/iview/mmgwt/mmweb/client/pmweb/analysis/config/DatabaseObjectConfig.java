package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DatabaseObject;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Created on 17.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@NonNLS
public class DatabaseObjectConfig extends Config {
    private final DatabaseObject dbo;


    public DatabaseObjectConfig(boolean privacyMode, HistoryToken historyToken, String layoutGuid, SafeHtml headerPrefix, DatabaseObject dbo) {
        super(privacyMode, historyToken, layoutGuid, -1, -1, headerPrefix);
        if (dbo != null && StringUtil.hasText(dbo.getId())) {
            this.dbo = dbo;
            return;
        }
        throw new IllegalArgumentException("databaseObject must be set and must have an Id!");
    }

    @Override
    public String getId() {
        return this.dbo.getId();
    }

    @Override
    public String toString() {
        return "DatabaseObjectConfig{" +
                "dbo.id=" + dbo.getId() +
                "dbo.classIdx=" + dbo.getClassIdx() +
                '}';
    }

    public DatabaseObject getDbo() {
        return this.dbo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final DatabaseObjectConfig that = (DatabaseObjectConfig) o;
        return dbo.getId().equals(that.dbo.getId()) &&
                dbo.getClassIdx().equals(that.dbo.getClassIdx());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + dbo.getId().hashCode();
        result = 31 * result + dbo.getClassIdx().hashCode();
        return result;
    }
}
