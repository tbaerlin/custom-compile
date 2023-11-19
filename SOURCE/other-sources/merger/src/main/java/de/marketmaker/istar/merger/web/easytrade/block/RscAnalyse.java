/*
 * RscAnalyse.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.StockAnalysisProvider;
import de.marketmaker.istar.merger.stockanalysis.StockAnalysisRequest;
import de.marketmaker.istar.merger.stockanalysis.StockAnalysisResponse;

/**
 * Returns an analysis for a given id or the latest available analyses if no id is given.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscAnalyse extends EasytradeCommandController {

    public static class Command {

        // providerId is not needed here since analysisid is unique across all providers

        private String analysisid;

        /**
         * @return the analysis id
         */
        @Pattern(regex = "\\d+")
        public String getAnalysisid() {
            return analysisid;
        }

        public void setAnalysisid(String analysisid) {
            this.analysisid = analysisid;
        }

        public void setAnalysenid(String analysenid) {
            this.analysisid = analysenid;
        }

    }

    private StockAnalysisProvider stockAnalysisProvider;

    public RscAnalyse() {
        super(Command.class);
    }

    public void setStockAnalysisProvider(StockAnalysisProvider stockAnalysisProvider) {
        this.stockAnalysisProvider = stockAnalysisProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        return new ModelAndView("rscanalysis", "analysis", getAnalysis((Command) o));
    }

    private StockAnalysis getAnalysis(Command cmd) {
        if (cmd.getAnalysisid() != null) {
            return getAnalysisById(cmd.getAnalysisid());
        }
        else {
            return getLatestAnalysis();
        }
    }

    private StockAnalysis getLatestAnalysis() {
        final StockAnalysisRequest sar = new StockAnalysisRequest();
        sar.setInstrumentids(Collections.emptyList());
        sar.setAnzahl(1);
        sar.setOffset(0);
        sar.setSortBy("analysisdate");
        sar.setAscending(false);
        final StockAnalysisResponse result = this.stockAnalysisProvider.getAnalyses(sar);
        final List<StockAnalysis> analyses = result.getAnalyses();
        if (analyses.isEmpty()) {
            throw new NoDataException("no analysis available");
        }
        return analyses.get(0);
    }

    private StockAnalysis getAnalysisById(String id) {
        final List<StockAnalysis> analyses = this.stockAnalysisProvider.getAnalyses(Collections.singletonList(id));
        if (analyses.get(0) == null) {
            throw new NoDataException("unknown analysis id: " + id);
        }
        return analyses.get(0);
    }

}


