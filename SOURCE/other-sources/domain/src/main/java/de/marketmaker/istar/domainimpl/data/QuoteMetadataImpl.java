/*
 * QuoteMetadataImpl.java
 *
 * Created on 05.10.2008 17:44:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;

import de.marketmaker.istar.domain.data.QuoteMetadata;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteMetadataImpl implements Serializable, QuoteMetadata {
    protected static final long serialVersionUID = 1L;

    private final long qid;

    private final boolean screener;

    private final boolean convensys;

    private final boolean estimatesReuters;

    private final boolean edg;

    private final boolean gisFndReport;

    private final boolean gisCerReport;

    private final boolean stockselectionFndReport;

    private final boolean stockselectionCerReport;

    private final boolean ssatFndReport;

    private final boolean factset;

    private final boolean vwdbenlFundamentalData;

    private final boolean funddataMorningstar;

    private final boolean funddataVwdBeNl;

    private final boolean cerUnderlying;

    private final boolean wntUnderlying;

    private final boolean cerUnderlyingDzbank;

    private final boolean cerUnderlyingWgzbank;

    private final boolean wntUnderlyingDzbank;

    private final boolean cerDzbank;

    private final boolean cerWgzbank;

    private final boolean wntDzbank;

    private final boolean optUnderlying;

    private final boolean futUnderlying;

    private final boolean indexConstituent;

    public QuoteMetadataImpl(long qid, boolean screener, boolean convensys,
            boolean estimatesReuters,
            boolean edg, boolean gisFndReport, boolean gisCerReport,
            boolean stockselectionFndReport, boolean stockselectionCerReport,
            boolean ssatFndReport, boolean factset, boolean vwdbenlFundamentalData,
            boolean funddataMorningstar, boolean funddataVwdBeNl, boolean cerUnderlying,
            boolean wntUnderlying, boolean cerUnderlyingDzbank, boolean cerUnderlyingWgzbank,
            boolean wntUnderlyingDzbank,
            boolean cerDzbank, boolean cerWgzbank, boolean wntDzbank, boolean optUnderlying,
            boolean futUnderlying,
            boolean indexConstituent) {
        this.qid = qid;
        this.screener = screener;
        this.convensys = convensys;
        this.estimatesReuters = estimatesReuters;
        this.edg = edg;
        this.gisFndReport = gisFndReport;
        this.gisCerReport = gisCerReport;
        this.stockselectionFndReport = stockselectionFndReport;
        this.stockselectionCerReport = stockselectionCerReport;
        this.ssatFndReport = ssatFndReport;
        this.factset = factset;
        this.vwdbenlFundamentalData = vwdbenlFundamentalData;
        this.funddataMorningstar = funddataMorningstar;
        this.funddataVwdBeNl = funddataVwdBeNl;
        this.cerUnderlying = cerUnderlying;
        this.wntUnderlying = wntUnderlying;
        this.cerUnderlyingDzbank = cerUnderlyingDzbank;
        this.cerUnderlyingWgzbank = cerUnderlyingWgzbank;
        this.wntUnderlyingDzbank = wntUnderlyingDzbank;
        this.cerDzbank = cerDzbank;
        this.cerWgzbank = cerWgzbank;
        this.wntDzbank = wntDzbank;
        this.optUnderlying = optUnderlying;
        this.futUnderlying = futUnderlying;
        this.indexConstituent = indexConstituent;
    }

    public long getQid() {
        return this.qid;
    }

    public boolean hasConvensys() {
        return this.convensys;
    }

    public boolean hasEstimatesReuters() {
        return this.estimatesReuters;
    }

    public boolean hasSceener() {
        return this.screener;
    }

    public boolean hasEdg() {
        return this.edg;
    }

    public boolean hasGisFndReport() {
        return this.gisFndReport;
    }

    public boolean hasGisCerReport() {
        return this.gisCerReport;
    }

    public boolean hasStockselectionFndReport() {
        return this.stockselectionFndReport;
    }

    public boolean hasStockselectionCerReport() {
        return this.stockselectionCerReport;
    }

    public boolean hasSsatFndReport() {
        return ssatFndReport;
    }

    public boolean hasFactset() {
        return factset;
    }

    public boolean hasVwdbenlFundamentalData() {
        return vwdbenlFundamentalData;
    }

    public boolean hasFunddataMorningstar() {
        return funddataMorningstar;
    }

    public boolean hasFunddataVwdBeNl() {
        return funddataVwdBeNl;
    }

    public boolean hasCerUnderlying() {
        return cerUnderlying;
    }

    public boolean hasWntUnderlying() {
        return wntUnderlying;
    }

    public boolean hasCerUnderlyingDzbank() {
        return cerUnderlyingDzbank;
    }

    public boolean hasCerUnderlyingWgzbank() {
        return cerUnderlyingWgzbank;
    }

    public boolean hasWntUnderlyingDzbank() {
        return wntUnderlyingDzbank;
    }

    public boolean hasCerDzbank() {
        return cerDzbank;
    }

    public boolean hasCerWgzbank() {
        return cerWgzbank;
    }

    public boolean hasWntDzbank() {
        return wntDzbank;
    }

    public boolean hasOptUnderlying() {
        return optUnderlying;
    }

    public boolean hasFutUnderlying() {
        return futUnderlying;
    }

    public boolean hasIndexConstituent() {
        return indexConstituent;
    }

    public String toString() {
        return "QuoteMetadataImpl[qid=" + qid
                + ", screener" + (this.screener ? "+" : "-")
                + ", convensys" + (this.convensys ? "+" : "-")
                + ", estimatesReuters" + (this.estimatesReuters ? "+" : "-")
                + ", edg" + (this.edg ? "+" : "-")
                + ", gisFndReport" + (this.gisFndReport ? "+" : "-")
                + ", gisCerReport" + (this.gisCerReport ? "+" : "-")
                + ", stockselectionFndReport" + (this.stockselectionFndReport ? "+" : "-")
                + ", stockselectionCerReport" + (this.stockselectionCerReport ? "+" : "-")
                + ", ssatFndReport" + (this.ssatFndReport ? "+" : "-")
                + ", factset" + (this.factset ? "+" : "-")
                + ", vwdbenlFundamentalData" + (this.vwdbenlFundamentalData ? "+" : "-")
                + ", funddataMorningstar" + (this.funddataMorningstar ? "+" : "-")
                + ", funddataVwdBeNl" + (this.funddataVwdBeNl ? "+" : "-")
                + ", cerUnderlying" + (this.cerUnderlying ? "+" : "-")
                + ", wntUnderlying" + (this.wntUnderlying ? "+" : "-")
                + ", cerUnderlyingDzbank" + (this.cerUnderlyingDzbank ? "+" : "-")
                + ", cerUnderlyingWgzbank" + (this.cerUnderlyingWgzbank ? "+" : "-")
                + ", wntUnderlyingDzbank" + (this.wntUnderlyingDzbank ? "+" : "-")
                + ", cerDzbank" + (this.cerDzbank ? "+" : "-")
                + ", cerWgzbank" + (this.cerWgzbank ? "+" : "-")
                + ", wntDzbank" + (this.wntDzbank ? "+" : "-")
                + ", optUnderlying" + (this.optUnderlying ? "+" : "-")
                + ", futUnderlying" + (this.futUnderlying ? "+" : "-")
                + ", indexConstituent" + (this.indexConstituent ? "+" : "-")
                + "]";
    }
}
