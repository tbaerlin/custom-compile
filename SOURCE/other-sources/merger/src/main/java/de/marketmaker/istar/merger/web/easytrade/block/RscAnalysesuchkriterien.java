/*
 * RscAnalysesuchkriterien.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.provider.StockAnalysisProvider;
import de.marketmaker.istar.merger.web.easytrade.block.analyses.RscFinderMetadata;

/**
 * Returns data describing categories and criteria for instrument analyses.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscAnalysesuchkriterien implements AtomController {
    private StockAnalysisProvider stockAnalysisProvider;

    public void setStockAnalysisProvider(StockAnalysisProvider stockAnalysisProvider) {
        this.stockAnalysisProvider = stockAnalysisProvider;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        final Map<String, Map<?, String>> metadata = this.stockAnalysisProvider.getMetaData();
        final Map<String, Object> model = new HashMap<>();
        model.putAll(metadata);
        return new ModelAndView(RscFinderMetadata.TEMPLATE, model);
    }
}
