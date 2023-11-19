/*
 * HistoricRatiosProvider.java
 *
 * Created on 17.07.2006 17:33:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.*;
import de.marketmaker.istar.domainimpl.data.IntervalUnit;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pm5.historicRatios")
public interface HistoricRatiosProvider {

    List<IntervalPerformance> getIntervalPerformances(SymbolQuote quote, IntervalUnit unit, int intervals);
    List<QuarterlyYield> getQuarterlyYields(SymbolQuote quote);

    List<BasicHistoricRatios> getPortfolioRatios(PortfolioRatiosRequest request);

    List<BasicHistoricRatios> getBasicHistoricRatios(SymbolQuote quote, SymbolQuote benchmarkQuote, List<Interval> intervals);
    List<BasicHistoricRatios> getBasicHistoricRatios(SymbolQuote quote, SymbolQuote benchmarkQuote, List<Interval> intervals, Double aggregationDays);

    List<ExtendedHistoricRatios> getExtendedHistoricRatios(SymbolQuote quote, SymbolQuote quoteBenchmark, List<Interval> intervals);

    List<CorporateAction> getCorporateActions(SymbolQuote quote, Interval interval, boolean withFactorizedDividends);

    PortfolioVaRLightResponse getPortfolioVaRLight(PortfolioVaRLightRequest request);

    List<List<Price>> getHistoricPrices(List<SymbolQuote> quotes, List<LocalDate> dates);
}
