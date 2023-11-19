package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * Created on 08.09.2010 14:34:05
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public enum CerDataSection {
    CHART(I18n.I.chart(), true),
    STATIC(I18n.I.staticData(), true),
    PRICE(I18n.I.prices(), false),
    RATIOS(I18n.I.ratios(), true),
    COMPARE_CHART(I18n.I.compareChart(), false),
    UNDERLYING(I18n.I.underlying(), false);

    private final String name;
    private final boolean expanded;

    CerDataSection(String name, boolean expanded) {
        this.name = name;
        this.expanded = expanded;
    }

    public String getName() {
        return name;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
