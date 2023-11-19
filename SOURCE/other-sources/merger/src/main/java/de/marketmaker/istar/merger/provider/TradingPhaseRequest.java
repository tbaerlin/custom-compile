/*
 * RatingDataRequest.java
 *
 * Created on 02.11.11 18:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.Interval;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.data.TradingPhase;

/**
 * @author tkiesgen
 */
public class TradingPhaseRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final List<SymbolQuote> quotes;

    private final TradingPhase.SignalSystem[] systems;

    private final TradingPhase.SignalSystem.Strategy[] strategies;

    private final Interval interval;

    private final Boolean shortPhases;

    public TradingPhaseRequest(SymbolQuote quote, TradingPhase.SignalSystem system,
            TradingPhase.SignalSystem.Strategy strategy, Interval interval,
            Boolean shortPhases) {
        this(Collections.singletonList(quote), new TradingPhase.SignalSystem[]{system},
                new TradingPhase.SignalSystem.Strategy[]{strategy}, interval, shortPhases);
    }

    public TradingPhaseRequest(List<SymbolQuote> quotes,
            TradingPhase.SignalSystem[] systems,
            TradingPhase.SignalSystem.Strategy[] strategies, Interval interval,
            Boolean shortPhases) {
        this.quotes = quotes;
        this.systems = systems;
        this.strategies = strategies;
        this.interval = interval;
        this.shortPhases = shortPhases;
    }

    public List<SymbolQuote> getQuotes() {
        return quotes;
    }

    public TradingPhase.SignalSystem[] getSystems() {
        return systems;
    }

    public TradingPhase.SignalSystem.Strategy[] getStrategies() {
        return strategies;
    }

    public Interval getInterval() {
        return interval;
    }

    public Boolean getShortPhases() {
        return shortPhases;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", quotes=").append(this.quotes);
        sb.append(", system=").append(Arrays.toString(this.systems));
        sb.append(", strategy=").append(Arrays.toString(this.strategies));
        sb.append(", interval=").append(this.interval);
        sb.append(", shortPhases=").append(this.shortPhases);
    }
}
