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
public class SecurityIdConfig extends Config {
    private final String securityId;

    protected SecurityIdConfig(boolean privacyMode, HistoryToken historyToken, String layoutGuid, int chartHeight, int chartWidth, SafeHtml headerPrefix, String securityId) {
        super(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, headerPrefix);
        if (!StringUtil.hasText(securityId)) {
            throw new IllegalArgumentException("securityId must be set!");
        }
        this.securityId = securityId;
    }

    @Override
    public String getId() {
        return this.securityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityIdConfig)) return false;
        if (!super.equals(o)) return false;

        final SecurityIdConfig that = (SecurityIdConfig) o;

        return !(securityId != null ? !securityId.equals(that.securityId) : that.securityId != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (securityId != null ? securityId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SecurityIdConfig{" +
                "securityId='" + securityId + '\'' +
                "} " + super.toString();
    }
}
