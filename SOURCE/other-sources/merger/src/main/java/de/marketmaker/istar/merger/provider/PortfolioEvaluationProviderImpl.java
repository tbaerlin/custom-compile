/*
 * PortfolioEvaluationProviderImpl.java
 *
 * Created on 08.08.2006 14:34:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.user.EvaluatedPortfolio;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioEvaluationProviderImpl implements PortfolioEvaluationProvider {
    private static final String INDEX_POINTS = "XXP";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private InstrumentProvider instrumentProvider;

    private IntradayProvider intradayProvider;

    private IsoCurrencyConversionProvider currencyConversionProvider;

    public void setCurrencyConversionProvider(
            IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public EvaluatedPortfolio evaluate(Portfolio p, boolean withEmptyPositions) {
        final List<PortfolioPosition> positions = p.getPositions(withEmptyPositions);
        final Map<Long, Quote> quoteById = getQuotes(positions);
        final Map<Long, PriceRecord> priceById = getPrices(positions, quoteById);
        final Map<String, BigDecimal> exchangerateByCode = getExchangerates(p, quoteById);
        return new EvaluatedPortfolio(p, quoteById, priceById, exchangerateByCode);
    }

    private Map<String, BigDecimal> getExchangerates(Portfolio p, Map<Long, Quote> quoteById) {
        final Map<String, BigDecimal> result = new HashMap<>();

        final HashSet<String> foreignCurrencies = new HashSet<>();
        for (Quote quote : quoteById.values()) {
            if (quote.getCurrency().getSymbolIso() != null) {
                foreignCurrencies.add(quote.getCurrency().getSymbolIso());
            }
        }

        foreignCurrencies.remove(p.getCurrencyCode()); // not foreign

        if (foreignCurrencies.remove(INDEX_POINTS)) {
            result.put(INDEX_POINTS, BigDecimal.ONE);
        }

        if (foreignCurrencies.isEmpty()) {
            return result;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getExchangerates> for " + foreignCurrencies + " in portfolio " + p.getId());
        }

        for (String isoCode : foreignCurrencies) {
            try {
                final BigDecimal exchangerate =
                        this.currencyConversionProvider.getConversion(isoCode, p.getCurrencyCode()).getRate().getValue();
                result.put(isoCode, exchangerate);
            } catch (NoDataException e) {
                // missing conversion, has been logged by provider, ignore here                
            }
        }

        return result;
    }

    private Map<Long, PriceRecord> getPrices(List<PortfolioPosition> positions, 
            Map<Long, Quote> quotesById) {
        final Map<Long, PriceRecord> result = new HashMap<>();

        final List<Quote> quotes = new ArrayList<>(positions.size());
        for (PortfolioPosition pp : positions) {
            quotes.add(quotesById.get(pp.getQid()));
        }

        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        for (int i = 0; i < positions.size(); i++) {
            final PortfolioPosition pp = positions.get(i);
            final PriceRecord pr = priceRecords.get(i);
            result.put(pp.getQid(), pr);
        }
        return result;
    }

    private Map<Long, Quote> getQuotes(List<PortfolioPosition> positions) {
        final Map<Long, Quote> result = new HashMap<>();

        final List<Instrument> instruments = identifyInstruments(positions);

        for (int i = 0; i < positions.size(); i++) {
            final PortfolioPosition pp = positions.get(i);
            final Instrument instrument = instruments.get(i);
            if (instrument == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getQuotes> no  " + pp.getIid() + ".iid"
                            + " for position " + pp.getId());
                }
                continue;
            }
            final Quote quote = instrument.getQuote(pp.getQid());
            if (quote == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getQuotes> no " + pp.getQid() + ".qid in "
                            + instrument.getId() + ".iid for position " + pp.getId());
                }
                continue;
            }
            result.put(pp.getQid(), quote);
        }

        return result;
    }

    private List<Instrument> identifyInstruments(List<PortfolioPosition> positions) {
        final List<Long> iids = getIids(positions);
        return this.instrumentProvider.identifyInstruments(iids);
    }

    private List<Long> getIids(List<PortfolioPosition> positions) {
        final List<Long> result = new ArrayList<>();
        for (PortfolioPosition pp : positions) {
            result.add(pp.getIid());
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getIids> for " + result);
        }
        return result;
    }
}
