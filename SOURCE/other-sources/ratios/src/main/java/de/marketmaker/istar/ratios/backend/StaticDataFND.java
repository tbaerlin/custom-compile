/*
 * StaticData.java
 *
 * Created on 16.09.2005 14:10:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticDataFND extends AbstractStaticData {
    public final static StaticDataFND NULL = new StaticDataFND(null, null);

    private final Long vwdBenchmarkQid;

    private final Long vwdbenlBenchmarkQid;

    public StaticDataFND(Long vwdBenchmarkQid, Long vwdbenlBenchmarkQid) {
        this.vwdBenchmarkQid = vwdBenchmarkQid;
        this.vwdbenlBenchmarkQid = vwdbenlBenchmarkQid;
    }

    public Long getVwdBenchmarkQid() {
        return vwdBenchmarkQid;
    }

    public Long getVwdbenlBenchmarkQid() {
        return vwdbenlBenchmarkQid;
    }

    public String toString() {
        return "StaticDataFND[vwdBenchmarkQid=" + vwdBenchmarkQid
                + ", vwdbenlBenchmarkQid=" + vwdbenlBenchmarkQid
                + "]";
    }
}