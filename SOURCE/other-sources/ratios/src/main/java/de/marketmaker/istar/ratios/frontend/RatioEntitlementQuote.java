/*
 * RatioEntitlementQuote.java
 *
 * Created on 10.08.12 09:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.apache.commons.lang3.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.EntitlementQuote;

/**
 * Extended <tt>EntitlementQuote</tt> with a static instance cache to limit the number of different
 * instances (currently, we have 614 instances for millions of quote ratios). Each instance
 * remembers whether it is allowed wrt. the profile of the current search.
 *
 * @author oflege
 */
public class RatioEntitlementQuote extends EntitlementQuote {

    /**
     * the allowed field will be modified by searches; as searches are mutually exclusive within
     * but not across types, RatioEntitlementQuotes have to be cached per type.
     */
    private static final Map<InstrumentTypeEnum, Map<String, RatioEntitlementQuote>> CACHES
            = new EnumMap<>(InstrumentTypeEnum.class);

    static {
        for (InstrumentTypeEnum e : InstrumentTypeEnum.values()) {
            CACHES.put(e, new HashMap<>());
        }
    }

    /**
     * if the stamp equals {@link de.marketmaker.istar.ratios.frontend.SearchParameterParser#getSearchId()},
     * the ref specifies whether the quote is allowed or not; otherwise, the ref is stale and
     * has to be re-evaluated/-set with the current profile.
     */
    final AtomicStampedReference<Boolean> allowed
            = new AtomicStampedReference<>(Boolean.FALSE, -1);

    private RatioEntitlementQuote(MarketcategoryEnum marketcategory, String marketCountrySymbolIso,
            String... entitlements) {
        super(marketcategory, marketCountrySymbolIso, entitlements);
    }

    static RatioEntitlementQuote getFor(RatioEntitlementQuote current, Quote quote) {
        final MarketcategoryEnum marketcategory = quote.getMarket().getMarketcategory();
        final String countrySymbolIso = quote.getMarket().getCountry().getSymbolIso();
        final String[] strs = quote.getEntitlement().getEntitlements(KeysystemEnum.VWDFEED);
        if (current != null
                && current.getMarketcategory() == marketcategory
                && StringUtils.equals(current.getMarketCountrySymbolIso(), countrySymbolIso)
                && Arrays.equals(strs, current.getEntitlements(KeysystemEnum.VWDFEED))) {
            return current;
        }


        final String strsKey = Arrays.toString(strs);
        final String key = marketcategory + "-" + countrySymbolIso + "-" + strsKey;

        final Map<String, RatioEntitlementQuote> cache
                = CACHES.get(quote.getInstrument().getInstrumentType());

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cache) {
            final RatioEntitlementQuote existing = cache.get(key);
            if (existing != null) {
                return existing;
            }

            final RatioEntitlementQuote result = new RatioEntitlementQuote(marketcategory,
                    countrySymbolIso, strs);
            cache.put(key, result);
            return result;
        }
    }
}
