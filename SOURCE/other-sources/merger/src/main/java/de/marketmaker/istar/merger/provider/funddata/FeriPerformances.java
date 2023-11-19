/*
 * FeriPerformances.java
 *
 * Created on 23.03.2007 12:21:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Deprecated // TODO: remove once mm-xml is turned off
public class FeriPerformances implements Serializable {
    protected static final long serialVersionUID = 1L;

    private final FeriPerformanceTimeseries fund = new FeriPerformanceTimeseries();
    private final FeriPerformanceTimeseries benchmark = new FeriPerformanceTimeseries();

    public FeriPerformanceTimeseries getFund() {
        return fund;
    }

    public FeriPerformanceTimeseries getBenchmark() {
        return benchmark;
    }
}
