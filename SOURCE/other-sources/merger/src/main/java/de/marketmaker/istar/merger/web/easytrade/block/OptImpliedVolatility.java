/*
 * OptImplicitVolatility.java
 *
 * Created on 14.12.11 10:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.ImpliedVolatilities;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.provider.risk.ImpliedVolatilitiesImpl;
import de.marketmaker.istar.merger.provider.risk.ImpliedVolatilityProvider;
import de.marketmaker.istar.merger.provider.risk.ImpliedVolatilityRequest;
import de.marketmaker.istar.merger.provider.risk.ImpliedVolatilityResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oflege
 */
public class OptImpliedVolatility extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    private ImpliedVolatilityProvider volatilityProvider;

    private UnderlyingShadowProvider underlyingShadowProvider;

    public static class Command extends DefaultSymbolCommand {
        private LocalDate from;

        public LocalDate getFrom() {
            return from;
        }

        public void setFrom(LocalDate from) {
            this.from = from;
        }
    }

    public OptImpliedVolatility() {
        super(Command.class);
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void setVolatilityProvider(ImpliedVolatilityProvider volatilityProvider) {
        this.volatilityProvider = volatilityProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Instrument instrument = this.instrumentProvider.getInstrument(cmd);
        final ImpliedVolatilities volatilities = getVolatilities(cmd, instrument.getId());


        final Map<String, Object> model = new HashMap<>();
        model.put("quote", getQuote(instrument, cmd.getMarket(), cmd.getMarketStrategy()));
        model.put("volatilities", volatilities);
        return new ModelAndView("optimpliedvola", model);
    }

    private Quote getQuote(Instrument instrument, String market, String marketStrategy) {
        try {
            return this.instrumentProvider.getQuote(instrument, market, marketStrategy);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getQuote> no quote for " + instrument.getId() + ".iid");
            }
        }
        return instrument.getQuotes().get(0);
    }

    private ImpliedVolatilities getVolatilities(Command cmd, long iid) {
        ImpliedVolatilities result = doGetVolatilities(cmd, iid);
        if (result != null) {
            return result;
        }
        final List<Long> iids = this.underlyingShadowProvider.getInstrumentids(iid);
        if (iids != null) {
            for (Long shadowIid : iids) {
                result = doGetVolatilities(cmd, shadowIid);
                if (result != null) {
                    return result;
                }
            }
        }
        return ImpliedVolatilitiesImpl.createEmpty(iid);
    }

    private ImpliedVolatilities doGetVolatilities(Command cmd, final long iid) {
        final ImpliedVolatilityRequest request = new ImpliedVolatilityRequest(iid, cmd.getFrom());
        final ImpliedVolatilityResponse response
                = this.volatilityProvider.getImpliedVolatilities(request);
        return response.getResult();
    }

}
