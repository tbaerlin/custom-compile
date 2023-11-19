/*
 * AnalysesMetaRequest.java
 *
 * Created on 21.03.12 08:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
public class AnalysesMetaRequest extends AbstractAnalysesRequest {
    protected static final long serialVersionUID = 1L;

    private final boolean ignoreAnalysesWithoutRating;

    public AnalysesMetaRequest(Selector selector, boolean ignoreAnalysesWithoutRating) {
        super(selector);
        this.ignoreAnalysesWithoutRating = ignoreAnalysesWithoutRating;
    }

    public boolean isIgnoreAnalysesWithoutRating() {
        return ignoreAnalysesWithoutRating;
    }
}
