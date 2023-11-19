/*
 * BenchmarkHistoryRequest.java
 *
 * Created on 23.04.12 11:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.bonddata;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author tkiesgen
 */
public class BenchmarkHistoryRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final Quote quote;

    public BenchmarkHistoryRequest(Quote quote) {
        this.quote = quote;
    }

    public Quote getQuote() {
        return quote;
    }

    @Override
    public String toString() {
        return "BenchmarkHistoryRequest{" +
                "quote=" + quote +
                '}';
    }
}