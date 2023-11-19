/*
 * StkKennzahlenBenchmark.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.QuarterlyYield;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscQuarterlyYields extends EasytradeCommandController {

    private HistoricRatiosProvider historicRatiosProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private String templateName = "mscquarterlyyields";

    public MscQuarterlyYields() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final DefaultSymbolCommand cmd = (DefaultSymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final List<QuarterlyYield> quarterlyYields
                = this.historicRatiosProvider.getQuarterlyYields(SymbolQuote.create(quote));
        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("yields", quarterlyYields);
        return new ModelAndView(this.templateName, model);
    }
}