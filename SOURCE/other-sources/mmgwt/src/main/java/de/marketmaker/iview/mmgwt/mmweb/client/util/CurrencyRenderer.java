/*
 * PriceRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Map;
import java.util.HashMap;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CurrencyRenderer implements Renderer<String> {

    public static Map<String, String> isoCurrencyMapping = new HashMap<String, String>();

    static {
        isoCurrencyMapping.put("XXZ", "%"); // $NON-NLS-0$ $NON-NLS-1$
        isoCurrencyMapping.put("XXP", I18n.I.pointsAbbr());  // $NON-NLS-0$
        isoCurrencyMapping.put("RAL", ""); // $NON-NLS-0$ $NON-NLS-1$
    }

    private final String defaultValue;

    public static CurrencyRenderer DEFAULT = new CurrencyRenderer(""); // $NON-NLS-0$

    public CurrencyRenderer(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String render(String iso) {
        if (iso == null || iso.length() == 0) {
            return this.defaultValue;
        }
        final String mapped = isoCurrencyMapping.get(iso);
        return (mapped != null) ? mapped : iso;
    }
}
