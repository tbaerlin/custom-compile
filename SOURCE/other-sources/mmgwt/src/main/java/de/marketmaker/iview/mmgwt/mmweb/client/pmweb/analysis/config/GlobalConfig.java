/*
 * GlobalConfig.java
 *
 * Created on 23.06.2014 09:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Markus Dick
 */
@NonNLS
public class GlobalConfig extends Config {
    protected GlobalConfig(boolean privacyMode, HistoryToken historyToken, String layoutGuid, int chartHeight, int chartWidth, SafeHtml headerPrefix) {
        super(privacyMode, historyToken, layoutGuid, chartHeight, chartWidth, headerPrefix);
    }

    @Override
    public String getId() {
        return this.layoutGuid;
    }

    @Override
    public String toString() {
        return "GlobalConfig{} " + super.toString();
    }
}
