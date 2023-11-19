/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Returns a quotation of current prices and bids for a financial instrument symbol.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscKursdaten extends EasytradeCommandController {

    private String template = "msckursdaten";

    protected IntradayProvider intradayProvider;

    private HighLowProvider highLowProvider;

    protected EasytradeInstrumentProvider instrumentProvider;


    public MscKursdaten() {
        super(DefaultSymbolCommand.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final DefaultSymbolCommand cmd = (DefaultSymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        if (quote == null) {
            errors.reject("quote.unknown", "no quote found");
            return null;
        }

        final PriceRecord price = this.intradayProvider.getPriceRecords(Collections.singletonList(quote)).get(0);
        final HighLow highLow = this.highLowProvider.getHighLow52W(quote, price);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("price", price);
        model.put("highLow", highLow);

        return new ModelAndView(this.template, model);
    }
}
