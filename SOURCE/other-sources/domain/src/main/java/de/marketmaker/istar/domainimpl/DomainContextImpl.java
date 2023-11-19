/*
 * DomainContextImpl.java
 *
 * Created on 15.08.2005 13:33:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.Sector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DomainContextImpl implements DomainContext {

    public static final SectorDp2 UNKNOWN_SECTOR;

    public static final CurrencyDp2 UNKNOWN_CURRENCY;

    public static final CountryDp2 UNKNOWN_COUNTRY;

    public static final MarketDp2 UNKNOWN_MARKET;

    static {
        UNKNOWN_SECTOR = new SectorDp2(0L, "Unknown");
        initUnknown(UNKNOWN_SECTOR);
        UNKNOWN_SECTOR.setSymbol(KeysystemEnum.DP_TEAM, "Unknown");

        UNKNOWN_CURRENCY = new CurrencyDp2(0L, "Unknown");
        initUnknown(UNKNOWN_CURRENCY);

        UNKNOWN_COUNTRY = new CountryDp2(0L, "Unknown");
        initUnknown(UNKNOWN_COUNTRY);
        UNKNOWN_COUNTRY.setCurrency(UNKNOWN_CURRENCY);

        UNKNOWN_MARKET = new MarketDp2(0L, "Unknown");
        initUnknown(UNKNOWN_MARKET);
        UNKNOWN_MARKET.setCountry(UNKNOWN_COUNTRY);
        UNKNOWN_MARKET.setMarketcategory(MarketcategoryEnum.UNKNOWN);
    }

    private static void initUnknown(ItemWithNamesDp2 item) {
        item.setNames(Language.de, "Unbekannt");
        item.setNames(Language.en, "Unknown");
        item.setNames(Language.it, "Sconosciuto");  //#Translations from mmgwt i18n
    }

    private final Map<Long, SectorDp2> sectors = new HashMap<>();

    private final Map<Long, CurrencyDp2> currencies = new HashMap<>();

    private final Map<Long, CountryDp2> countries = new HashMap<>();

    private final Map<Long, MarketDp2> markets = new HashMap<>();

    public DomainContextImpl() {
    }

    public void addSector(SectorDp2 sector) {
        addObject(sector, sectors);
    }

    public void addCurrency(CurrencyDp2 currency) {
        addObject(currency, currencies);
    }

    public void addCountry(CountryDp2 country) {
        addObject(country, countries);
    }

    public void addMarket(MarketDp2 market) {
        addObject(market, markets);
    }

    private <T extends ItemWithSymbols> void addObject(T o, Map<Long, T> map) {
        map.put(o.getId(), o);
    }

    public Market getMarket(long id) {
        return getByIdOrDefault(id, this.markets, UNKNOWN_MARKET);
    }

    public Sector getSector(long id) {
        return getByIdOrDefault(id, this.sectors, UNKNOWN_SECTOR);
    }

    public Currency getCurrency(long id) {
        return getByIdOrDefault(id, this.currencies, UNKNOWN_CURRENCY);
    }

    public Country getCountry(long id) {
        return getByIdOrDefault(id, this.countries, UNKNOWN_COUNTRY);
    }

    private <T> T getByIdOrDefault(long id, Map<Long, T> map, T defaultValue) {
        final T ret = map.get(id);
        return (ret != null) ? ret : defaultValue;
    }

    public List<Market> getMarkets() {
        return new ArrayList<Market>(this.markets.values());
    }

    public List<Currency> getCurrencies() {
        return new ArrayList<Currency>(this.currencies.values());
    }

    public List<Sector> getSectors() {
        return new ArrayList<Sector>(this.sectors.values());
    }

    public List<Country> getCountries() {
        return new ArrayList<Country>(this.countries.values());
    }
}
