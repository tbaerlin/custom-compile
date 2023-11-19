/*
 * InstrumentDao.java
 *
 * Created on 16.08.2005 14:23:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.Iterator;
import java.util.List;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * Access to instrument related data and meta data.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @since 1.0
 */
public interface InstrumentDao extends Iterable<Instrument> {

    /**
     * @return a list of available {@link de.marketmaker.istar.domain.Country}(s)
     */
    List<Country> getCountries();

    /**
     * @return a list of available {@link de.marketmaker.istar.domain.Currency}(s)
     */

    List<Currency> getCurrencies();

    /**
     * @return a list of available {@link de.marketmaker.istar.domain.Market}(s)
     */
    List<Market> getMarkets();

    /**
     * Gets the instrument identified by the given instrument id.
     *
     * @param iid an instrument id
     * @return an instrument with the given instrument id.
     */
    Instrument getInstrument(long iid);

    /**
     * @return a list of available instrument ids.
     * @deprecated please use {@link #iterator()} instead.
     */
    List<Long> getInstruments();

    /**
     * Gets an iterator of instruments updated lately.
     *
     * @return an iterator of instruments
     */
    Iterator<Instrument> getUpdates();

    /**
     * Returns the subset of those values in iids, that are <em>not</em> valid, i.e., that, when
     * used as parameter {@link #getInstrument(long)}, yield <tt>null</tt>.
     *
     * @param iids to be tested for validity
     * @return array of invalid iids
     */
    long[] validate(long[] iids);

    /**
     * @return the number of instruments contained in this DAO.
     * @since 1.2
     */
    int size();
}
