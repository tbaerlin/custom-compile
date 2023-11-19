/*
 * CerInformationen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.Underlying;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataProvider;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Queries <b>key future data</b> identified by a given financial <b>instrument symbol</b>. The
 * subject security is selected based on the given symbol w/o <b>symbol strategy</b> and market w/o
 * <b>market strategy</b>.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FutStaticData extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private UnderlyingShadowProvider underlyingShadowProvider;

    private CommonDataProvider commonDataProvider;

    public FutStaticData() {
        super(DefaultSymbolCommand.class);
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final Instrument underlying = getUnderlying(quote);
        final Quote underlyingQuote = underlying != null ? this.instrumentProvider.getQuote(underlying, null, null) : null;

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("underlyingQuote", underlyingQuote);

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {

            final List<CommonDataResponse> commonDataResponses =
                this.commonDataProvider.fetchCommonData(
                    null, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

            if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
                CommonDataResponse commonDataResponse = commonDataResponses.get(0);
                model.put("common", commonDataResponse.getFields());
            }
        }

        return new ModelAndView("futstaticdata", model);
    }

    public Instrument getUnderlying(Quote quote) {
        final Instrument instrument = quote.getInstrument();
        if (!(instrument instanceof Derivative)) {
            return null;
        }

        final long underlyingId = ((Derivative) instrument).getUnderlyingId();
        if (underlyingId <= 0) {
            return null;
        }

        final Instrument underlying = this.instrumentProvider.identifyInstrument(EasytradeInstrumentProvider.iidSymbol(underlyingId), null);

        if (!(underlying instanceof Underlying)) {
            return underlying;
        }

        final Long shadowIid = this.underlyingShadowProvider.getShadowInstrumentId(underlying.getId());
        if (shadowIid == null) {
            return null;
        }
        return this.instrumentProvider.identifyInstrument(EasytradeInstrumentProvider.iidSymbol(shadowIid), null);
    }
}