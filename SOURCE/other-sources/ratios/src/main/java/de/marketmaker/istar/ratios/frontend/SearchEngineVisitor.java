/*
 * SearchEngineVisitor.java
 *
 * Created on 03.08.2006 18:37:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SearchEngineVisitor extends GenericSearchEngineVisitor<RatioData, RatioSearchResponse> {
    void init(SearchParameterParser spp);

    void visit(RatioData data);

    RatioSearchResponse getResponse();
}
