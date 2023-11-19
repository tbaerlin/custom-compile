/*
 * ErrorUtils.java
 *
 * Created on 01.08.2006 13:18:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import org.springframework.validation.Errors;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class ErrorUtils {

    public static void rejectSymbol(String symbol, Errors e) {
        e.rejectValue("symbol", "invalid.symbol", new Object[]{symbol},
                "Invalid symbol: " + symbol);
    }

    public static void rejectProfile(Profile p, Selector selector, Errors e) {
        e.reject("invalid.profile",  "selector " + selector.getId() + " not allowed for " + p.getName());
    }
}
