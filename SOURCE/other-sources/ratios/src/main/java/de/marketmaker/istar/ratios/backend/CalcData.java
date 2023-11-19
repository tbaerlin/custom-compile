/*
 * CalcData.java
 *
 * Created on 19.10.2005 08:14:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CalcData {
    private final Quote quote;
    private final Quote referenceQuote;
    private final SnapRecord snap;
    private final SnapRecord referenceSnap;
    private final int currencyFactor;
    private final boolean trace;

    public CalcData(Quote quote, SnapRecord snap, Quote referenceQuote, SnapRecord referenceSnap,
            boolean trace) {
        this.quote = quote;
        this.referenceQuote=referenceQuote;
        this.snap = snap;
        this.referenceSnap = referenceSnap;
        this.currencyFactor = quote.getCurrency().isCent() ? 100 : 1;
        this.trace = trace;
    }

    public long getQid() {
        return quote.getId();
    }

    public long getIid() {
        return quote.getInstrument().getId();
    }

    public Quote getQuote() {
        return quote;
    }

    public Quote getReferenceQuote() {
        return referenceQuote;
    }

    public SnapRecord getSnap() {
        return snap;
    }

    public SnapRecord getReferenceSnap() {
        return referenceSnap;
    }

    public InstrumentTypeEnum getInstrumentType() {
        return this.quote.getInstrument().getInstrumentType();
    }

    public int getCurrencyFactor() {
        return currencyFactor;
    }

    public boolean isTrace() {
        return trace;
    }

    @Override
    public String toString() {
        return "CalcData[" + getQid() + ".qid, snap=" + this.snap
                + ", referenceSnap=" + this.referenceSnap
                + ", currencyFactor=" + this.currencyFactor
                + "]";
    }
}
