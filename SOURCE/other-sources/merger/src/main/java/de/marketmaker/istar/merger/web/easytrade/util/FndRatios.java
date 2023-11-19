/*
 * FndRatios.java
 *
 * Created on 10.12.2007 14:38:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.BindException;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.block.AbstractFindersuchergebnis;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.PreferIssuerFundQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.QuoteRatios;
import de.marketmaker.istar.ratios.frontend.RatioDataRecordImpl;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * Method object that creates a model containing a fund's RatioDataRecord. That record can be
 * retrieved from the model using the key {@link #FUND_RATIOS_KEY}.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndRatios {
    private final BindException errors;

    private final RatiosProvider ratiosProvider;

    private final Quote quote;

    public static final String FUND_RATIOS_KEY = "fundRatios";

    public FndRatios(RatiosProvider ratiosProvider, Quote quote, BindException errors) {
        this.ratiosProvider = ratiosProvider;
        this.quote = quote;
        this.errors = errors;
    }

    public Map<String, Object> compute() {
        final Map<String, Object> result = new HashMap<>();

        final RatioSearchRequest rsr = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        rsr.setType(InstrumentTypeEnum.FND);

        final Map<String, String> parameters = new HashMap<>();
        rsr.setParameters(parameters);
        parameters.put("id", Long.toString(quote.getInstrument().getId()));
        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);
        if (!sr.isValid()) {
            this.errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final DefaultRatioSearchResponse drsr = (DefaultRatioSearchResponse) sr;
        if (drsr.getLength() != 1) {
            this.errors.reject("ratios.searchfailed",
                    "instrument not found: " + quote.getInstrument().getId());
            return null;
        }

        final RatioDataResult data = drsr.getElements().get(0);

        final QuoteRatios qr=getQuoteRatios(data);
        if (qr == null) {
            return null;
        }

        final RatioDataRecordImpl fundRatios = new RatioDataRecordImpl(data.getInstrumentRatios(),
                qr, AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.FND),
                RequestContextHolder.getRequestContext().getLocales());
        result.put(FUND_RATIOS_KEY, fundRatios);

        return result;
    }

    private QuoteRatios getQuoteRatios(RatioDataResult data) {
        for (final QuoteRatios qr : data.getQuotes()) {
            if (qr.getId() == this.quote.getId()) {
                return qr;
            }
        }
        return null;
    }
}
