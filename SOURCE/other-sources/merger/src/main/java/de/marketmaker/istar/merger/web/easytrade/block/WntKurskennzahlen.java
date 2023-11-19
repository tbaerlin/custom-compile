/*
 * WntKurskennzahlen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.WarrantRatios;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.provider.WarrantRatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WntKurskennzahlen extends EasytradeCommandController {
    private WarrantRatiosProvider warrantRatiosProvider;

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public WntKurskennzahlen() {
        super(DefaultSymbolCommand.class);
    }

    public void setWarrantRatiosProvider(WarrantRatiosProvider warrantRatiosProvider) {
        this.warrantRatiosProvider = warrantRatiosProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final DefaultSymbolCommand c = (DefaultSymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(c);

        final PriceRecord pr = this.intradayProvider.getPriceRecords(Arrays.asList(quote)).get(0);
        final WarrantRatios ratios = 
                this.warrantRatiosProvider.getWarrantRatios(SymbolQuote.create(quote), pr);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("ratios", ratios);

        return new ModelAndView("wntkurskennzahlen", model);
    }
}
