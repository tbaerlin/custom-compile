/*
 * OptUtil.java
 *
 * Created on 16.03.2009 15:36:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.OPTFinderElement;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OptUtil {
    public static String getNearMoneyStyle(OPTFinderElement e) {
        if (e.getUnderlyingPrice() == null || e.getStrike() == null) {
            return "opt-unknown"; // $NON-NLS-0$
        }
        return isNearMoney(e) ? "opt-nearmoney" : null; // $NON-NLS-0$
    }

    public static boolean isNearMoney(OPTFinderElement e) {
        final double price = Double.parseDouble(e.getUnderlyingPrice());
        final double strike = Double.parseDouble(e.getStrike());
        return strike >= (0.9 * price) && strike <= (1.1 * price);
    }

}
