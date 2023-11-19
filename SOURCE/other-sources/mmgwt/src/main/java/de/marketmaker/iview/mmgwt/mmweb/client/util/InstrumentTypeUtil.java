/*
 * InstrumentTypeUtil.java
 *
 * Created on 19.06.2008 16:33:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.HashMap;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentTypeUtil {

    private final static HashMap<String, String> NAMES = new HashMap<String, String>();

    public static final String ALL_KEY = "ALL"; // $NON-NLS-0$

    public static final String MSC_KEY = "MSC"; // $NON-NLS-0$

    static {
        NAMES.put("STK", I18n.I.stock());  // $NON-NLS-0$
        NAMES.put("BND", I18n.I.bonds());  // $NON-NLS-0$
        NAMES.put("CER", I18n.I.certificates());  // $NON-NLS-0$
        NAMES.put("CUR", I18n.I.currencies());  // $NON-NLS-0$
        NAMES.put("FND", I18n.I.funds());  // $NON-NLS-0$
        NAMES.put("FUT", I18n.I.futures());  // $NON-NLS-0$
        NAMES.put("GNS", I18n.I.bonusShares());  // $NON-NLS-0$
        NAMES.put("IND", I18n.I.indices());  // $NON-NLS-0$
        NAMES.put("MER", I18n.I.goodsAndCommodities());  // $NON-NLS-0$
        NAMES.put("OPT", I18n.I.options());  // $NON-NLS-0$
        NAMES.put("UND", I18n.I.underlyings());  // $NON-NLS-0$
        NAMES.put("WNT", I18n.I.warrants());  // $NON-NLS-0$
        NAMES.put("ZNS", I18n.I.interestRate());  // $NON-NLS-0$
        NAMES.put("MK", I18n.I.economyCycleDate());  // $NON-NLS-0$

        // and two pseudo types:
        NAMES.put(ALL_KEY, I18n.I.all()); 
        NAMES.put(MSC_KEY, I18n.I.other());                                  
    }

    public static String getName(String type) {
        final String s = NAMES.get(type);
        return (s != null) ? s : type;
    }
}
