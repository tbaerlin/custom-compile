package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;

import java.math.BigDecimal;


/**
 * need to convert dividend into quote currency so we can calculate yield in the frontend
 */
public interface DividendCalculationStrategy {

    BigDecimal calculate(IsoCurrencyConversionProvider converter, Quote quote, MasterDataStock masterDataStock);

    default BigDecimal getDividendInQuoteCurrency(IsoCurrencyConversionProvider converter, Quote quote,
                                                      BigDecimal dividend, String dividendCurrency) {
        if (converter == null) {
            return null;
        }

        final String quoteCurrency = quote.getCurrency().getSymbolIso();
        if (quoteCurrency == null || dividendCurrency == null || dividend == null
                || dividendCurrency.equals(quoteCurrency) ) {
            return dividend;
        }
        try {
            return converter.getConversion(dividendCurrency, quoteCurrency).convert(dividend);
        } catch (Exception e) {
            return null;
        }
    }


    DividendCalculationStrategy DIV_IN_QUOTE_CURRENCY = new DividendCalculationStrategy() {
        @Override
        public BigDecimal calculate(IsoCurrencyConversionProvider converter, Quote quote, MasterDataStock masterDataStock) {
            return DIV_IN_QUOTE_CURRENCY.getDividendInQuoteCurrency(converter, quote,
                    masterDataStock.getDividend(), masterDataStock.getDividendCurrency());
        }
    };

    DividendCalculationStrategy DIV_LAST_YEAR_IN_QUOTE_CURRENCY = new DividendCalculationStrategy() {
        @Override
        public BigDecimal calculate(IsoCurrencyConversionProvider converter, Quote quote, MasterDataStock masterDataStock) {
            return DIV_LAST_YEAR_IN_QUOTE_CURRENCY.getDividendInQuoteCurrency(converter, quote,
                    masterDataStock.getDividendLastYear(), masterDataStock.getDividendCurrency());
        }
    };

};




