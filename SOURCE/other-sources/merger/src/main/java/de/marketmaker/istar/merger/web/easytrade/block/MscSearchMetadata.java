/*
 * StkStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.ItemWithNames;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscSearchMetadata implements AtomController {
    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    protected EasytradeInstrumentProvider instrumentProvider;
    private EntitlementProviderVwd entitlementProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setEntitlementProvider(EntitlementProviderVwd entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        final SearchMetaResponse response = this.instrumentProvider.getSearchMetadata();

        final Language language = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());


        final Map<String, Object> model = new HashMap<>();
        model.put("countries", sort(response.getCountries(), language));
        model.put("currencies", sort(response.getCurrencies(), language));
        model.put("types", sort(response.getInstrumentTypes(), language));

        final Set<String> vwdMarketSymbols = this.entitlementProvider.getMarketNames(RequestContextHolder.getRequestContext().getProfile());

        final List<Market> markets = response.getMarkets();
        for (final Iterator<Market> it = markets.iterator(); it.hasNext();) {
            final String vwdMarketSymbol = it.next().getSymbolVwdfeed();
            if (vwdMarketSymbol == null || !vwdMarketSymbols.contains(vwdMarketSymbol)) {
                it.remove();
            }

        }
        model.put("markets", sort(cleanup(markets), language));
        return new ModelAndView("mscsearchmetadata", model);
    }

    private List<Market> cleanup(List<Market> markets) {
        if (markets.isEmpty()) {
            return markets;
        }
        if (!(markets.get(0) instanceof MarketDp2)) {
            return markets;
        }

        final Map<String, Market> joined = new HashMap<>();
        for (final Market market : markets) {
            final String name = market.getName().trim();
            if (!StringUtils.hasText(name)) {
                continue;
            }
            final MarketDp2 m = (MarketDp2) joined.get(name);

            if (m == null) {
                joined.put(name, market);
                continue;
            }

            m.setSymbol(KeysystemEnum.ISO, join(m.getSymbolIso(), market.getSymbolIso()));
            m.setSymbol(KeysystemEnum.VWDFEED, join(m.getSymbolVwdfeed(), market.getSymbolVwdfeed()));
        }

        return new ArrayList<>(joined.values());
    }

    private String join(String s1, String s2) {
        if (!StringUtils.hasText(s1)) {
            return s2;
        }
        if (!StringUtils.hasText(s2)) {
            return s1;
        }
        return s1 + "," + s2;
    }

    private <V extends ItemWithNames> List<V> sort(List<V> items, final Language language) {
        items.sort(new Comparator<V>() {
            public int compare(V o1, V o2) {
                return GERMAN_COLLATOR.compare(
                        o1.getNameOrDefault(language), o2.getNameOrDefault(language));
            }
        });
        return items;
    }
}