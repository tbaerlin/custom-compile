/*
 * WMDataResponse.java
 *
 * Created on 02.11.11 18:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.RatingData;
import de.marketmaker.istar.domain.data.TradingPhase;

/**
 * @author tkiesgen
 */
public class TradingPhaseResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final Map<Long, List<TradingPhase>> result = new HashMap<>();

    public void add(SymbolQuote quote, List<TradingPhase> phases) {
        this.result.put(quote.getId(), phases);
    }

    public List<TradingPhase> getData(SymbolQuote quote) {
        return this.result.get(quote.getId());
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("result=").append("<trading phases for ").append(this.result.size()).append(" quotes");
    }
}
