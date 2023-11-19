/*
 * StkVwdItCompanyRawdata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.ConvensysRawdata;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.VwdItCompanyFundamentalsProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Provides company data by Il Sole 24 Ore S.p.A. (aka AMF - Analisi Mercati Finanziari)
 * as XML raw data and links to PDF files for market manager.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkVwdItCompanyRawdata extends StkIlSole24OreCompanyBase {
    public StkVwdItCompanyRawdata() {
        super(DefaultSymbolCommand.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final VwdItCompanyFundamentalsProvider provider = this.companyFundamentalsProvider;
        final SymbolCommand cmd = (SymbolCommand) o;
        final Quote quote = getQuote(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);

        final String isin = quote == null ? null : quote.getInstrument().getSymbolIsin();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        if (isin != null && profile.isAllowed(Selector.IL_SOLE_COMPANY_DATA_XML)) {
            final ConvensysRawdata rawdata = provider.getPortraitData(isin);

            if (rawdata != null) {
                model.put("rawdata", rawdata);
                if (profile.isAllowed(Selector.IL_SOLE_COMPANY_DATA_PDF) && isAvailable(rawdata, "pdf_exists")) {
                    model.put("pdfRequest", getRequest(request, quote.getInstrument().getSymbolIsin(), null));
                    model.put("orgPdfRequest", getRequest(request, quote.getInstrument().getSymbolIsin(), PARAM_ORG_REQUEST));
                }
            }
        }

        return new ModelAndView("stkvwditcompanyrawdata", model);
    }

    private boolean isAvailable(ConvensysRawdata rawdata, String key) {
        final Object exists = rawdata.getAdditionalInformation().get(key);
        return exists instanceof Boolean && ((Boolean) exists);
    }
}