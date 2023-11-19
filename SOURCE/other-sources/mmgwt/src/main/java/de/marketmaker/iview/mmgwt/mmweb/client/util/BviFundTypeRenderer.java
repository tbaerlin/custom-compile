/*
 * SellHoldBuyRenderer.java
 *
 * Created on 20.08.2008 17:08:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BviFundTypeRenderer implements Renderer<String> {
    private static final Map<String, String> MAPPING = new HashMap<String, String>();

    static {
        MAPPING.put("A", I18n.I.stockFund());  // $NON-NLS-0$
        MAPPING.put("D", I18n.I.fundFund());  // $NON-NLS-0$
        MAPPING.put("G", I18n.I.moneyMarketFund());  // $NON-NLS-0$
        MAPPING.put("I", I18n.I.realEstateFund());  // $NON-NLS-0$
        MAPPING.put("M", I18n.I.mixedFund());  // $NON-NLS-0$
        MAPPING.put("R", I18n.I.pensionFund());  // $NON-NLS-0$
        MAPPING.put("S", I18n.I.other());  // $NON-NLS-0$
        MAPPING.put("W", I18n.I.valueGuaranteedFund());  // $NON-NLS-0$
    }

    public String render(String category) {
        final String s = MAPPING.get(category);
        return s == null ? category : s;
    }
}
