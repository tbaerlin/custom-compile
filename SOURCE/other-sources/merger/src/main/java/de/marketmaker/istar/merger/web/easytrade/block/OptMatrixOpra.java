/*
 * OptMatrixOpra.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.instrument.OptionDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OptMatrixOpra extends OpraFinder {
    private static final MarketDp2 NULL_MARKET = new MarketDp2(0, "");

    private static final CurrencyDp2 NULL_CURRENCY = new CurrencyDp2(0, null);

    public OptMatrixOpra() {
        super(SymbolListCommand.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolListCommand cmd = (SymbolListCommand) o;

        final String underlyingSymbol = getUnderlyingSymbol(cmd);
        final OpraRatioSearchResponse sr = getOpraItems(cmd, underlyingSymbol);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final ListResult listResult =
                ListResult.create(cmd, Collections.singletonList("symbol"), "symbol", 0);
        final Map<String, Object> model = createResultModel(sr, listResult);
        model.put("underlyingQuote", createUnderlyingQuote(cmd));
        return new ModelAndView("optmatrixopra", model);
    }

    private OpraRatioSearchResponse getOpraItems(SymbolListCommand cmd, String underlyingWkn) {
        if (underlyingWkn == null) {
            return new OpraRatioSearchResponse();
        }
        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.OPT, (String[]) null, null);
        // see: CORE-13406 reverted, this causes trouble in the frontend
        rsr.addParameter("underlyingWkn", "'" + underlyingWkn + "'");
        rsr.addParameter("sort1", "wkn");
        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.isDisablePaging() ? 35000 : cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.FALSE.toString());

        return this.ratiosProvider.getOpraItems(rsr);
    }

    private QuoteDp2 createUnderlyingQuote(SymbolListCommand cmd) {
        final OptionDp2 underlying = new OptionDp2(0);
        underlying.setName(cmd.getSymbol());

        final QuoteDp2 result = new QuoteDp2(0);
        result.setMarket(NULL_MARKET);
        result.setCurrency(NULL_CURRENCY);
        result.setInstrument(underlying);
        return result;
    }
}