/*
 * IsoCurrencyConversionProvider.java
 *
 * Created on 08.08.2006 15:49:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import org.joda.time.YearMonthDay;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.Map;
import java.util.List;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.isocurrencyconversion")
public interface IsoCurrencyConversionProvider {
    IsoCurrencyConversionProviderImpl.ConversionResult getConversion(String isocode1, String isocode2);

    /**
     * @deprecated use {@link #getConversion(String, String, org.joda.time.LocalDate)}
     */
    IsoCurrencyConversionProviderImpl.ConversionResult getConversion(String isocode1, String isocode2, YearMonthDay date);

    IsoCurrencyConversionProviderImpl.ConversionResult getConversion(String isocode1, String isocode2, LocalDate date);

    /**
     * Returns a map with vwdfeed symbols for all pairs of currency iso codes. The keys in the map
     * are formed by concatenating the from-code and the to-code by "-".<p>
     * Example:<br>
     * <tt>isocodes = ["EUR", "USD", "CHF"]</tt>, the result will contain symbols for the keys
     * <tt>"EUR-USD", "EUR-CHF", "USD-EUR", "USD-CHF", "CHF-EUR", "CHF-USD"</tt>
     *
     * @param isocodes currency iso codes
     * @return map with vwdfeed symbols that can be used for currency conversions; for combinations
     * without a cross rate symbol, the map will not contain any mapping.
     */
    Map<String, String> getCrossRateSymbols(Collection<String> isocodes);

    /**
     * Returns the vwdcode for the conversion from a source to a target currency
     * @param from source currency isocode (e.g. EUR)
     * @param to target currency isocode (e.g. USD)
     * @return feed symbol or null if no symbol for the conversion is available
     */
    String getCrossRateSymbol(String from, String to);
}
