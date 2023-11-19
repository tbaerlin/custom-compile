/*
 * StkAnalystenschaetzung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.HistoricEstimates;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.estimates.EstimatesProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Returns historic estimates of the economic development of a given stock corporation for the last few weeks and/or months depending on the data item.
 * <p>
 *     In the response, the XML entity name of a data item is suffixed with its value's age.
 *     The age is given as a single-digit number that is appended with <code>w</code> for weeks or <code>m</code> for months.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkHistoricEstimates extends EasytradeCommandController {
    private EstimatesProvider estimatesProvider;
    private EasytradeInstrumentProvider instrumentProvider;

    public StkHistoricEstimates() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setEstimatesProvider(EstimatesProvider estimatesProvider) {
        this.estimatesProvider = estimatesProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("instrument", quote.getInstrument());
        model.put("referencequote", quote);
        final HistoricEstimates historicEstimates = getEstimates(quote);
        model.put("historicEstimates", historicEstimates);

        return new ModelAndView("stkhistoricestimates", model);
    }

    private HistoricEstimates getEstimates(Quote quote) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return this.estimatesProvider.getHistoricEstimates(profile, quote.getInstrument().getId());
    }
}