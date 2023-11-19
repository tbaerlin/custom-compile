/*
 * QuotesForInstrument.java
 *
 * Created on 24.10.13 11:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.marketmaker.istar.domain.instrument.MmInstrumentclass;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;

/**
* Collects information about the quotes that belong to a single instrument
*/
class QuotesForInstrument {
    static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    static final QuotesForInstrument EOF = new QuotesForInstrument(MdpExporterDp2.MM_IID);

    final long iid;

    // this is an instrument-level attribute, but is defined in the mdp on quote-level
    // for performance reasons, we retrieve it with QuotesForIstar and merge it later
    // into the instruments
    private MmInstrumentclass instrumentclass;

    final List<QuoteDp2> quotes = new ArrayList<>();

    QuotesForInstrument(long iid) {
        this.iid = iid;
        INSTANCE_COUNT.incrementAndGet();
    }

    MmInstrumentclass getInstrumentclass() {
        return instrumentclass;
    }

    void setInstrumentclass(MmInstrumentclass instrumentclass) {
        this.instrumentclass = instrumentclass;
    }

    void add(QuoteDp2 quote) {
        this.quotes.add(quote);
    }

    boolean isEmpty() {
        return this.quotes.isEmpty();
    }
}
