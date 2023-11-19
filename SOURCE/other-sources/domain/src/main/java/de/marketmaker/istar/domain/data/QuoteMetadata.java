/*
 * QuoteMetadata.java
 *
 * Created on 05.10.2008 17:43:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface QuoteMetadata {
    long getQid();

    boolean hasConvensys();

    boolean hasEstimatesReuters();

    boolean hasSceener();

    boolean hasEdg();

    boolean hasGisFndReport();

    boolean hasGisCerReport();

    boolean hasStockselectionFndReport();

    boolean hasStockselectionCerReport();

    boolean hasSsatFndReport();

    boolean hasFactset();

    boolean hasVwdbenlFundamentalData();

    boolean hasFunddataMorningstar();

    boolean hasFunddataVwdBeNl();

    boolean hasCerUnderlying();

    boolean hasWntUnderlying();

    boolean hasCerUnderlyingDzbank();

    boolean hasCerUnderlyingWgzbank();

    boolean hasWntUnderlyingDzbank();

    boolean hasCerDzbank();

    boolean hasCerWgzbank();

    boolean hasWntDzbank();

    boolean hasIndexConstituent();

    boolean hasOptUnderlying();

    boolean hasFutUnderlying();

}
