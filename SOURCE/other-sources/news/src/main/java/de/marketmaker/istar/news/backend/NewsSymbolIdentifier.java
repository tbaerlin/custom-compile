/*
 * NewsSymbolIdentifier.java
 *
 * Created on 23.03.2007 21:01:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsSymbolIdentifier {
    /**
     * Returns a Map of instruments keyed by their iids for the given symbols
     * @param symbols each symbol is either an isin or a wkn
     * @return all identifiable symbols
     */
    Map<Long, Instrument> identify(Set<String> symbols);

    /**
     * Assigns Intrument objects to newsRecord for all iids associated with the record.
     * @param newsRecord to be assigned with instruments
     */
    void assignInstrumentsTo(NewsRecordImpl newsRecord);    
}
