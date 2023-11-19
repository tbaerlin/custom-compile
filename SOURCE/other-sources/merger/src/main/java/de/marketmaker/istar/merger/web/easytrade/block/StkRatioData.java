/*
 * BndRatioData.java
 *
 * Created on 23.04.2019 15:26:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides ratio data for a given stock corporation.
 *
 * @author Timo Wiegel
 */
public class StkRatioData extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    protected RatiosProvider ratiosProvider;

    public StkRatioData() {
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
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.STK);

        final RatioDataRecord ratios = this.ratiosProvider.getRatioData(quote, fields);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("ratios", ratios);
        return new ModelAndView("stkratiodata", model);
    }
}