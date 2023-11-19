/*
 * HistoricRatiosMethod.java
 *
 * Created on 27.11.2010 07:06:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricRatiosMethod {
    private final HistoricRatiosProvider provider;

    private final Map<String, Object> model = new HashMap<>();

    public HistoricRatiosMethod(HistoricRatiosProvider controller) {
        this.provider = controller;
    }

    public Map<String, Object> invoke() {
//        this.model.put("quote", quote);
        return this.model;
    }

}
