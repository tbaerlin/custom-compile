/*
 * InstrumentRepository.java
 *
 * Created on 18.10.2005 11:02:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;

import java.util.Collection;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentRepository {
    Collection<Long> getDerivativeQuoteids(long instrumentid);

    Long getInstrumentid(long quoteid);

    Instrument getInstrument(long instrumentid);

    Quote getBenchmarkQuote(Instrument instrument);

    Long getNonStandardUnderlyingQid(long iid);

    Quote getQuote(long quoteid);
}
