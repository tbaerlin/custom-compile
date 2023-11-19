/*
 * MscRatingData.java
 *
 * Created on 03.11.2011 17:06:22
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

import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RestrictedWMDataImpl;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.WMDataProvider;
import de.marketmaker.istar.merger.provider.WMDataRequest;
import de.marketmaker.istar.merger.provider.WMDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Returns static data for an instrument/quote as provided by <a href="http://www.wmdaten.de">WM</a>.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WmStaticData extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private WMDataProvider wmDataProvider;

    public WmStaticData() {
        super(DefaultSymbolCommand.class);
    }

    public void setWmDataProvider(WMDataProvider wmDataProvider) {
        this.wmDataProvider = wmDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Map<String, Object> model = new HashMap<>();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        WMData wmData = null;
        final Quote quote = getQuote(cmd);
        if (quote != null) {
            final long iid = quote.getInstrument().getId();
            final WMDataResponse wmDataResponse =
                this.wmDataProvider.getData(new WMDataRequest(profile, iid));
            wmData = wmDataResponse.getData(iid);
        }
        else if (IsinUtil.isIsin(cmd.getSymbol())) {
            final String isin = cmd.getSymbol();
            final WMDataResponse wmDataResponse =
                this.wmDataProvider.getData(new WMDataRequest(profile, isin));
            wmData = wmDataResponse.getData(isin);
        }
        else if (cmd.getSymbol().length() == 6) {
            final String wkn = cmd.getSymbol();
            final WMDataResponse wmDataResponse =
                this.wmDataProvider.getData(new WMDataRequest(profile, wkn));
            wmData = wmDataResponse.getData(wkn);
        }

        if (wmData != null) {
            model.put("wmData", new RestrictedWMDataImpl(wmData));
        }
        model.put("quote", quote);
        return new ModelAndView("wmstaticdata", model);
    }

    private Quote getQuote(SymbolCommand cmd) {
        try {
            return this.instrumentProvider.getQuote(cmd);

        } catch (UnknownSymbolException use) {
            // try defaulting to use instrument only for symbols with "no entitled quote"
            try {
                final Instrument instrument = this.instrumentProvider.getInstrument(cmd);
                if (instrument != null) {
                    return NullQuote.create(instrument);
                }
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getQuote> unknown symbol for instrument retrieval", e);
                }
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getQuote> unknown symbol", use);
            }
        }
        return null;
    }
}
