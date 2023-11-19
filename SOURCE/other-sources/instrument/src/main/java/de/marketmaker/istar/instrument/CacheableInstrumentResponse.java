/*
 * CacheableInstrumentResponse.java
 *
 * Created on 20.03.14 10:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * @author oflege
 */
public interface CacheableInstrumentResponse {
    long getInstrumentUpdateTimestamp();

    /**
     * @return the list of instruments found. The number of elements correpsonds to the number of
     *         items in the respective request. For unidentifiable items, the list will contain null values.
     */
    List<Instrument> getInstruments();

    /**
     * @return all underlyings associated with instrument ids. The key is an instrument id, which in
     *         turn is the id of one of the instrument found within this response. The value is an instrument,
     *         which might not be included as a direct search result in this response.
     */
    Map<Long, Instrument> getUnderlyings();
}
