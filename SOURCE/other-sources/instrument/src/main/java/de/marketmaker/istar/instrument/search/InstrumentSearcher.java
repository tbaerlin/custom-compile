/*
 * InstrumentSearcher.java
 *
 * Created on 03.01.2005 16:38:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentSearcher {

    SearchMetaResponse getMetaData(SearchMetaRequest request);

    SearchResponse search(SearchRequest sr);

    SearchResponse simpleSearch(SearchRequest sr);

    /**
     * Returns the subset of those values in iids, that are <em>not</em> valid.
     * @param iids to be tested for validity
     * @return array of invalid iids
     */
    long[] validate(long[] iids);

    /**
     * Returns the instrumentids of all underlyings that are associated with any
     * derivatives in the search index.
     * @return underlying iids
     */
    Set<Long> getUnderlyingIds();
}
