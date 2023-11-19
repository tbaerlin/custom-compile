/*
 * StockAnalysisDao.java
 *
 * Created on 10.08.2006 09:31:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StockAnalysisDao {
    Map<String, Long> getAnalysts();

    int deleteAnalysis(long id);

    int storeAnalysis(Long id, String analyst, Instrument instrument,
                      Rating rating, DateTime date,
                      String category, String subcategory,
                      String headline, String text, Double aimMin, Double aimMax, String currency);

    List<StockAnalysis> getAnalyses(List<Instrument> instruments, int maxResults);

    List<StockAnalysis> getAnalyses(List<String> analysisids);

    StockAnalysisSummary getSummaryData(Instrument instrument);

    Map<String, Map<?, String>> getMetaData();

    StockAnalysisResponse getAnalyses(StockAnalysisRequest request);

    StockAnalysisAims getAims(long instrumentid);

    void deleteOldEntries();
}
