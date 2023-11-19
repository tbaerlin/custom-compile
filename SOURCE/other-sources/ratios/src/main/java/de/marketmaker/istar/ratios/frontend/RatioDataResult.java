/*
 * RatioDataResult.java
 *
 * Created on 26.10.2005 15:29:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioDataResult implements Serializable {
    static final long serialVersionUID = 1L;

    private final long selectedQuoteid;
    private final QuoteRatios[] quotes;

    public RatioDataResult(long quoteid, QuoteRatios[] quotes) {
        this.selectedQuoteid = quoteid;
        this.quotes = quotes;
    }

    public String toString() {
        return "RatioDataResult["
                + getInstrumentid() + ".iid"
                + ", " + this.selectedQuoteid + ".qid"
                + ": " + this.quotes.length
                + "]";
    }

    public long getTimestamp() {
        return getInstrumentRatios().getTimestamp();
    }

    public long getInstrumentid() {
        return getInstrumentRatios().getId();
    }

    public long getQuoteid() {
        return this.selectedQuoteid;
    }

    public InstrumentRatios getInstrumentRatios() {
        return this.quotes[0].getInstrumentRatios();
    }

    public QuoteRatios getQuoteData() {
        for (QuoteRatios quote : this.quotes) {
            if (quote.getId() == this.selectedQuoteid) {
                return quote;
            }
        }
        return null;
    }

    public QuoteRatios[] getQuotes() {
        return this.quotes;
    }
}
