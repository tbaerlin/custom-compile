/*
 * CurrencyConversionMethod.java
 *
 * Created on 07.05.2010 15:27:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.HighLowImpl;
import de.marketmaker.istar.domainimpl.data.PriceRecordWithFactor;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;

/**
 * Helper method to multiply prices in PriceRecords and HighLows with a certain cross rate factor
 * so as to be able to show those prices in a different currency.
 * @author oflege
 */
public class CurrencyConversionMethod {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyConversionMethod.class);

    private final IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    private final String targetCurrency;

    public CurrencyConversionMethod(IsoCurrencyConversionProvider isoCurrencyConversionProvider,
            String targetCurrency) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
        this.targetCurrency = targetCurrency;
    }

    /**
     * Converts the given price into the target currency.
     * @param quote price's quote
     * @param price to be converted
     * @return converted price, or price if no conversion was necessary
     */
    public PriceRecord invoke(Quote quote, PriceRecord price) {
        final ArrayList<PriceRecord> priceRecords
                = new ArrayList<>(Collections.singleton(price));
        invoke(Collections.singletonList(quote), priceRecords, null);
        return priceRecords.get(0);
    }

    /**
     * convert the prices and highLows into equivalent values in the targetCurrency
     * @param quotes quotes related to prices and highLows
     * @param prices if not null, prices will be replaced as needed by converted prices
     * @param highLows if not null, elements will be replaced as needed by converted high lows
     */
    void invoke(List<Quote> quotes, List<PriceRecord> prices, List<HighLow> highLows) {
        final Map<String, BigDecimal> factors = getCurrencyConversionFactors(quotes);
        if (factors == null) { // nothing to convert
            return;
        }
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote == null || !factors.containsKey(quote.getCurrency().getSymbolIso())) {
                continue;
            }
            final BigDecimal factor = factors.get(quote.getCurrency().getSymbolIso());
            if (prices != null) {
                prices.set(i, PriceRecordWithFactor.create(prices.get(i), factor));
            }
            if (highLows != null) {
                highLows.set(i, HighLowImpl.create(highLows.get(i), factor));
            }
        }
    }

    private Map<String, BigDecimal> getCurrencyConversionFactors(List<Quote> quotes) {
        final HashSet<String> currencies = getCurrenciesToConvert(quotes);
        if (currencies.isEmpty()) {
            return null;
        }
        final Map<String, BigDecimal> result = new HashMap<>();
        for (String currency : currencies) {
            result.put(currency, getFactor(currency));
        }
        return result;
    }

    private BigDecimal getFactor(String currency) {
        try {
            final IsoCurrencyConversionProviderImpl.ConversionResult conversionResult
                    = this.isoCurrencyConversionProvider.getConversion(currency, this.targetCurrency);
            return conversionResult.getFactor();
        } catch (Exception e) {
            LOGGER.warn("<getFactor> failed for " + currency + " to " + this.targetCurrency);
            return null;
        }
    }

    private HashSet<String> getCurrenciesToConvert(List<Quote> quotes) {
        final HashSet<String> result = new HashSet<>();
        for (Quote quote : quotes) {
            result.add(getSourceCurrency(quote));
        }
        result.remove(this.targetCurrency);
        return result;
    }

    private String getSourceCurrency(Quote quote) {
        if (quote != null) {
            final String s = quote.getCurrency().getSymbolIso();
            if (s != null && !s.equals(this.targetCurrency) && !s.equals("XXP")) {
                return s;
            }
        }
        return this.targetCurrency;
    }
}
