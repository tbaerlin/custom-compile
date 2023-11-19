/*
 * InstrumentResponse.java
 *
 * Created on 22.12.2004 14:04:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * Result of an {@link de.marketmaker.istar.instrument.InstrumentRequest}.
 * For {@link de.marketmaker.istar.domain.instrument.Derivative}s in {@link #getInstruments()},
 * their respective underlying will be available in {@link #getUnderlyings()}.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentResponse extends AbstractIstarResponse implements
        CacheableInstrumentResponse {
    static final long serialVersionUID = 213125L;

    private long instrumentUpdateTimestamp;

    private List<Instrument> instruments;

    private Map<Long, Instrument> underlyings = Collections.emptyMap();

    public InstrumentResponse() {
    }

    public InstrumentResponse(long instrumentUpdateTimestamp) {
        this.instrumentUpdateTimestamp = instrumentUpdateTimestamp;
    }

    @Override
    public long getInstrumentUpdateTimestamp() {
        return instrumentUpdateTimestamp;
    }

    @Override
    public List<Instrument> getInstruments() {
        return this.instruments;
    }

    @Override
    public Map<Long, Instrument> getUnderlyings() {
        return this.underlyings;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setUnderlyings(Map<Long, Instrument> underlyings) {
        this.underlyings = underlyings;
    }
}
