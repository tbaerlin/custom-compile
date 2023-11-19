/*
 * FndPerformance.java
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

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.FndBenchmarkRatios;
import de.marketmaker.istar.merger.web.easytrade.util.FndRatios;
import de.marketmaker.istar.merger.web.easytrade.util.FndSectorPerformance;

/**
 * Provides performance ratio data of a given fund.
 * <p>
 * The returned ratio data contains performances of following instruments:
 * <ul>
 * <li>the given fund</li>
 * <li>the benchmark of the given fund</li>
 * <li>several other funds that are in the same sector of the given fund, i.e. the best and
 * worst performances within the same sector.</li>
 * </ul>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 4229.qid
 */
public class FndPerformance extends EasytradeCommandController {
    private HistoricRatiosProvider historicRatiosProvider;

    private RatiosProvider ratiosProvider;

    private FundDataProvider fundDataProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public FndPerformance() {
        super(DefaultSymbolCommand.class);
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Map<String, Object> model = new HashMap<>();

        final SymbolCommand cmd = (SymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        model.put("quote", quote);

        final Map<String, Object> fr = new FndRatios(this.ratiosProvider, quote, errors).compute();
        if (fr == null) {
            return null;
        }

        model.putAll(fr);
        final RatioDataRecord fundRatios = (RatioDataRecord) fr.get(FndRatios.FUND_RATIOS_KEY);

        final Map<String, Object> sperf
                = new FndSectorPerformance(this.ratiosProvider, fundRatios).compute();
        model.putAll(sperf);

        final Map<String, Object> bperf
                = new FndBenchmarkRatios(this.instrumentProvider, this.fundDataProvider,
                this.historicRatiosProvider, quote).compute();
        model.putAll(bperf);

        return new ModelAndView("fndperformance", model);
    }
}
