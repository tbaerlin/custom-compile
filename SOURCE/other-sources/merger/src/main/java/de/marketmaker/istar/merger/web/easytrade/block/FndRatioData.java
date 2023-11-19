/*
 * FndRatioData.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 *  Queries diverse measurement ratio data for a given fund.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 4229.qid
 */
public class FndRatioData extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;
    protected RatiosProvider ratiosProvider;

    public FndRatioData() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final SymbolCommand cmd = (SymbolCommand) o;
        final Quote q = this.instrumentProvider.getQuote(cmd);

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.FND);

        final RatioDataRecord data = this.ratiosProvider.getRatioData(q, fields);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", q);
        model.put("ratios", data);
        return new ModelAndView("fndratiodata", model);
    }
}