/*
 * IsoCurrencySource.java
 *
 * Created on 06.05.2010 13:34:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author oflege
*/
class IsoCurrencySource {
    private Logger logger = LoggerFactory.getLogger(getClass());


    public static final int TYPE_FACTOR = 1;

    public static final int TYPE_KEY = 2;

    private final String source;

    private final Map<String, IsoCurrencyResult> results
            = new ConcurrentHashMap<>();

    IsoCurrencySource(String source) {
        this.source = source;
    }

    void addResult(IsoCurrencyResult icr) {
        if (this.results.containsKey(icr.getIsoKey())) {
            this.logger.debug("<addResult> target currency " + icr.getIsoKey()
                    + " for source " + this.source + " exists and will be overwritten");
        }
        this.results.put(icr.getIsoKey(), icr);
    }

    boolean hasResult(String currency) {
        return this.results.containsKey(currency);
    }

    IsoCurrencyResult getResult(String currency) {
        return this.results.get(currency);
    }

    public String toString() {
        return "IsoCurrencySource[isosource=" + this.source
                + ", results=" + this.results
                + "]";
    }
}
