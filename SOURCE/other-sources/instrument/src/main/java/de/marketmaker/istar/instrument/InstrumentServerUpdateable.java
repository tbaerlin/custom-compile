/*
 * InstrumentServerUpdateable.java
 *
 * Created on 13.09.2005 16:14:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.instrument.export.InstrumentDao;
import de.marketmaker.istar.instrument.search.InstrumentSearcher;
import de.marketmaker.istar.instrument.search.SuggestionSearcher;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentServerUpdateable {
    /**
     * Called whenever a new/updated instrument index is available
     * @param update true iff this method was called due to an update; false for new index
     * @param instrumentDao
     * @param instrumentSearcher
     * @param suggestionSearcher
     */
    void setInstrumentBackends(boolean update, InstrumentDao instrumentDao,
            InstrumentSearcher instrumentSearcher,
            SuggestionSearcher suggestionSearcher);
}
