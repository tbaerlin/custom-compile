/*
 * InstrumentSorter.java
 *
 * Created on 20.08.2009 11:15:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.instrument.search.SearchSorter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentSorter extends SearchSorter {
    /**
     * Do whatever is necessary to initialize this object; most likely only useful if the sorter
     * is used for an update and the initialization needs to examine the index to be updated
     *
     * @param indexBaseDir where to find the index
     * @param update true iff index will be updated
     * @throws IOException on error
     */
    void prepare(File indexBaseDir, boolean update) throws IOException;

    /**
     * Returns the order for this instrument; the returned values must be compatible the the type
     * of the SortField returned by this object's return value for
     * {@link de.marketmaker.istar.instrument.search.SearchSorter#getSortField()}.
     *
     * @param instrument to be sorted
     * @return instrument's order
     */
    String getOrder(Instrument instrument);

    /**
     * Called by instrument indexer after all instruments have been indexed, i.e. written by an
     * index writer.
     */
    void afterInstrumentIndexed();
}
