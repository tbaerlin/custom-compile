/*
 * StkAnalystenschaetzung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataRequest;
import de.marketmaker.istar.merger.provider.stockdata.StockDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides information about a company like the address and the company profile.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkCompanyProfile extends EasytradeCommandController {
    private StockDataProvider stockDataProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public StkCompanyProfile() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final StockDataRequest sdr = new StockDataRequest(Arrays.asList(quote.getInstrument().getId()),
                RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales()).withCompanyProfile();
        final StockDataResponse stockDataResponse = this.stockDataProvider.getStockData(sdr);
        final CompanyProfile companyProfile = stockDataResponse.getCompanyProfiles().get(0);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("profile", companyProfile);

        return new ModelAndView("stkcompanyprofile", model);
    }
}