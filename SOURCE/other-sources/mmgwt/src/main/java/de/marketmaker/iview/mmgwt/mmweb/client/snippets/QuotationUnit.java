/*
 * QuotationUnit.java
 *
 * Created on 8/3/15 10:17 AM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author kmilyut
 */
public enum QuotationUnit {
    UNKNOWN(I18n.I.others()),
    UNIT(I18n.I.quotationUnit()),
    PERCENT(I18n.I.quotationPercent()),
    PERMILLE(I18n.I.quotationPermille()),
    POINT(I18n.I.quotationPoint());

    private final String label;

    QuotationUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static QuotationUnit fromString(String s) {
        QuotationUnit result;
        try {
            result = valueOf(s);
        } catch (IllegalArgumentException e) {
            result = UNKNOWN;
        }

        return result;
    }
}
