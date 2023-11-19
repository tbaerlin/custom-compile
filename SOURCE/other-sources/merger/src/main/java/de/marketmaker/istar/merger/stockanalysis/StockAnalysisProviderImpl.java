/*
 * StockAnalysisProviderImpl.java
 *
 * Created on 10.08.2006 13:32:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.merger.provider.StockAnalysisProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisProviderImpl implements StockAnalysisProvider {
    private StockAnalysisDao stockAnalysisDao;

    public void setStockAnalysisDao(StockAnalysisDao stockAnalysisDao) {
        this.stockAnalysisDao = stockAnalysisDao;
    }

    public List<StockAnalysis> getAnalyses(List<Instrument> instruments, int maxResults) {
        return this.stockAnalysisDao.getAnalyses(instruments, maxResults);
    }

    public List<StockAnalysis> getAnalyses(List<String> analysisids) {
        return this.stockAnalysisDao.getAnalyses(analysisids);
    }

    public StockAnalysisSummary getSummaryData(Instrument instrument) {
        return this.stockAnalysisDao.getSummaryData(instrument);
    }

    public StockAnalysisAims getAims(long instrumentid) {
        return this.stockAnalysisDao.getAims(instrumentid);
    }

    public Map<String, Map<?, String>> getMetaData() {
        return this.stockAnalysisDao.getMetaData();
    }

    public StockAnalysisResponse getAnalyses(StockAnalysisRequest request) {
        return this.stockAnalysisDao.getAnalyses(request);
    }
}
