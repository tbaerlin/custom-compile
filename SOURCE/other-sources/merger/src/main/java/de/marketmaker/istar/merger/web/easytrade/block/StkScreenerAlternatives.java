/*
 * StkScreenerAlternatives.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.data.screener.ScreenerAlternative;
import de.marketmaker.istar.merger.provider.screener.ScreenerProvider;
import de.marketmaker.istar.merger.provider.screener.ScreenerResult;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Returns stock alternatives for a given financial instrument symbol.
 * The selection of the alternatives is based on <i>theScreener's</i> analysis and ratings.
 * The security, for which the alternatives should be returned, is selected on the given symbol (according to the symbol strategy) and market (without a given market strategy).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkScreenerAlternatives extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;
    private ScreenerProvider screenerProvider;

    private String template = "stkscreeneralternatives";

    public StkScreenerAlternatives() {
        super(DefaultSymbolCommand.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setScreenerProvider(ScreenerProvider screenerProvider) {
        this.screenerProvider = screenerProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        checkPermission(Selector.SCREENER);

        final Quote q = this.instrumentProvider.getQuote(cmd);

        if (q == null) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final ScreenerResult screenerResult = this.screenerProvider.getScreenerResult(q.getInstrument().getId(), "de");

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", q);
        model.put("country", getQuotes(screenerResult.getCountryAlternatives()));
        model.put("group", getQuotes(screenerResult.getGroupAlternatives()));

        return new ModelAndView(this.template, model);
    }

    private List<Quote> getQuotes(List<ScreenerAlternative> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        final List<Long> iids = new ArrayList<>(list.size());
        for (final ScreenerAlternative alternative : list) {
            iids.add(alternative.getInstrumentId());
        }
        final List<Quote> result = new ArrayList<>(list.size());
        for (final Instrument instrument : this.instrumentProvider.identifyInstruments(iids)) {
            if (instrument == null) {
                continue;
            }
            try {
                result.add(MarketStrategy.STANDARD.getQuote(instrument));
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getInstruments> failed", e);
                }
            }
        }
        return result;
    }
}
