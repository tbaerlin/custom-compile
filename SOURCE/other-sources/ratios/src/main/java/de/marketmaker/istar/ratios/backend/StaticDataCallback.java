/*
 * StaticDataCallback.java
 *
 * Created on 16.09.2005 13:43:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StaticDataCallback {
    void process(InstrumentTypeEnum type, long instrumentid, Map<Integer, Object> fields);
}
