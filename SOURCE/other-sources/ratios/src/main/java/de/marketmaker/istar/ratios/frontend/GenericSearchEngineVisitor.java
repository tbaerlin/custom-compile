/*
 * SearchEngineVisitor.java
 *
 * Created on 03.08.2006 18:37:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.common.request.IstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface GenericSearchEngineVisitor<T, V extends IstarResponse> {
    void init(SearchParameterParser spp);

    void visit(T data);

    V getResponse();
}
