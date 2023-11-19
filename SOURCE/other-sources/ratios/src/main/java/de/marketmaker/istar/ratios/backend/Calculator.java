/*
 * Calculator.java
 *
 * Created on 18.10.2005 11:03:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Calculator {
    /**
     * calculate ratios for all elements in toCalc, forward computed ratios to handler
     * @param toCalc input data
     * @param handler processes results
     */
    void calc(List<CalcData> toCalc, ComputedRatiosHandler handler);

    long unusedSinceMillis();
}
