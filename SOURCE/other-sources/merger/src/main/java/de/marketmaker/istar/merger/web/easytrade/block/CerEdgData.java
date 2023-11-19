/*
 * CerInformationen.java
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

import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Provides EDG rating and DDV risk data for a given certificate.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 417203.qid
 */
public class CerEdgData extends EasytradeCommandController {
    private CertificateDataProvider certificateDataProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private String template = "ceredgdata";

    public CerEdgData() {
        super(DefaultSymbolCommand.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final EdgData data = new EdgDataMethod(this.certificateDataProvider, quote.getInstrument()).invoke();

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("data", data);
        return new ModelAndView(this.template, model);
    }
}