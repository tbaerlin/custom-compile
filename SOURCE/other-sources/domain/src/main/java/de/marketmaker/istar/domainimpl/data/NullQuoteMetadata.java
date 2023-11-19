/*
 * NullQuoteMetadata.java
 *
 * Created on 05.10.2008 17:47:01
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
public class NullQuoteMetadata implements Serializable, QuoteMetadata {
    protected static final long serialVersionUID = 1L;

    public static final QuoteMetadata INSTANCE = new NullQuoteMetadata();

    private NullQuoteMetadata() {
    }

    public long getQid() {
        return 0;
    }

    public boolean hasOrderbook() {
        return false;
    }

    public boolean hasConvensys() {
        return false;
    }

    public boolean hasEstimatesReuters() {
        return false;
    }

    public boolean hasSceener() {
        return false;
    }

    public boolean hasEdg() {
        return false;
    }

    public boolean hasGisFndReport() {
        return false;
    }

    public boolean hasGisCerReport() {
        return false;
    }

    public boolean hasStockselectionFndReport() {
        return false;
    }

    public boolean hasStockselectionCerReport() {
        return false;
    }

    public boolean hasSsatFndReport() {
        return false;
    }

    public boolean hasFactset() {
        return false;
    }

    public boolean hasVwdbenlFundamentalData() {
        return false;
    }

    public boolean hasFunddataMorningstar() {
        return false;
    }

    public boolean hasFunddataVwdBeNl() {
        return false;
    }

    public boolean hasCerUnderlying() {
        return false;
    }

    public boolean hasWntUnderlying() {
        return false;
    }

    public boolean hasCerUnderlyingDzbank() {
        return false;
    }

    public boolean hasWntUnderlyingDzbank() {
        return false;
    }

    public boolean hasCerDzbank() {
        return false;
    }

    public boolean hasWntDzbank() {
        return false;
    }

    public boolean hasCerUnderlyingWgzbank() {
        return false;
    }

    public boolean hasCerWgzbank() {
        return false;
    }

    public boolean hasOptUnderlying() {
        return false;
    }

    public boolean hasFutUnderlying() {
        return false;
    }

    public boolean hasIndexConstituent() {
        return false;
    }
}
