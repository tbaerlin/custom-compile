/*
 * StockAnalysisProvider.java
 *
 * Created on 10.08.2006 13:32:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.merger.stockanalysis.StockAnalysisRequest;
import de.marketmaker.istar.merger.stockanalysis.StockAnalysisResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StockAnalysisProvider {

    List<StockAnalysis> getAnalyses(List<Instrument> instruments, int maxResults);

    List<StockAnalysis> getAnalyses(List<String> analysisids);

    StockAnalysisSummary getSummaryData(Instrument instrument);

    StockAnalysisAims getAims(long instrumentid);

    Map<String, Map<?, String>> getMetaData();

    StockAnalysisResponse getAnalyses(StockAnalysisRequest request);

}
