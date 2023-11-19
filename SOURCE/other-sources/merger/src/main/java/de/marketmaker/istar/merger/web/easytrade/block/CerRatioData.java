/*
 * CerRatioData.java
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
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Provides ratio data for a given certificate.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 228545.qid
 */
public class CerRatioData extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    protected RatiosProvider ratiosProvider;

    public CerRatioData() {
        super(DefaultSymbolCommand.class);
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final DefaultSymbolCommand c = (DefaultSymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(c);

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.CER);

        final RatioDataRecord ratios = this.ratiosProvider.getRatioData(quote, fields);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("ratios", ratios);
        return new ModelAndView("cerratiodata", model);
    }
}