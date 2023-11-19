/*
 * FndFeriPerformances.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.funddata.FeriPerformances;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Queries performance and benchmark data provided by Feri for a given fund.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 4229.qid
 */
public class FndFeriPerformances extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private FundDataProvider fundDataProvider;

    public FndFeriPerformances() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final FundDataRequest fdr = new FundDataRequest(quote.getInstrument())
                .withFeriPerformances();

        final FundDataResponse fundResponse = this.fundDataProvider.getFundData(fdr);
        final FeriPerformances performances = fundResponse.getFeriPerformanceses().get(0);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("fund", performances.getFund().iterator());
        model.put("benchmark", performances.getBenchmark().iterator());
        return new ModelAndView("fndferiperformances", model);
    }
}