package de.marketmaker.istar.merger.provider.pages;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.common.request.IstarResponse;

import java.util.List;

/**
 * MergerPageSearchResponse.java
 * Created on 15.07.2010 15:21:31
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

/**
 * Instances of this class are returned as result by {@link de.marketmaker.istar.merger.provider.pages.PageSearchProvider#searchPages(MergerPageSearchRequest)}.
 * In the main, this class wrapps a {@link java.util.List} of {@link de.marketmaker.istar.merger.provider.pages.PageSummary}
 * objects.
 * @author Sebastian Wild
 */
public class MergerPageSearchResponse extends AbstractIstarResponse {
    
    private static final long serialVersionUID = -7336056147815805469L;

    /**
     * Creates a new instance wrapping {@code foundPages}.
     * @param foundPages the list of pages to wrap
     */
    public MergerPageSearchResponse(List<PageSummary> foundPages) {
        if (foundPages == null)
            throw new IllegalArgumentException("foundPages may not be null");
        this.foundPages = foundPages;
    }

    /**
     * getter for foundPages
     * @return the wrapped list of {@link de.marketmaker.istar.merger.provider.pages.PageSummary}
     * objects.
     */
    public List<PageSummary> getFoundPages() {
        return foundPages;
    }


    private final List<PageSummary> foundPages;

}
