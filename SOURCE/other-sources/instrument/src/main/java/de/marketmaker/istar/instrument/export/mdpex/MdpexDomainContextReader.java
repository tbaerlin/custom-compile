/*
 * CurrencyReader.java
 *
 * Created on 21.01.14 13:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export.mdpex;

import java.io.File;
import java.util.EnumSet;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domainimpl.CountryDp2;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.ItemWithNamesDp2;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.SectorDp2;
import de.marketmaker.istar.instrument.protobuf.DomainContextSerializer;

import static de.marketmaker.istar.domain.KeysystemEnum.*;

/**
 * @author oflege
 */
public class MdpexDomainContextReader {

    private final DomainContextImpl dc = new DomainContextImpl();

    private long sectorMdpcn;

    private long sectorsymbolMdpcn;

    private long currencyMdpcn;

    private long currencysymbolMdpcn;

    private long countryMdpcn;

    private long countrysymbolMdpcn;

    private long marketMdpcn;

    private long marketsymbolMdpcn;

    public DomainContext readContext(File dir) throws Exception {
        readSectors(new File(dir, "SECTOR.xml"), new File(dir, "SECTORSYMBOL.xml"));
        readCurrencies(new File(dir, "CURRENCY.xml"), new File(dir, "CURRENCYSYMBOL.xml"));
        readCountries(new File(dir, "COUNTRY.xml"), new File(dir, "COUNTRYSYMBOL.xml"));
        readMarkets(new File(dir, "MARKET.xml"), new File(dir, "MARKETSYMBOL.xml"));
        return this.dc;
    }

    private void readSectors(File sectors, File sectorSymbols) throws Exception {
        this.sectorMdpcn = new MdpexExportReader() {
            @Override
            protected void handleRow() {
                String name = get("NAME");
                SectorDp2 s = new SectorDp2(getLong("MDPID"), name);
                s.setNames(Language.de, name);
                s.setNames(Language.en, get("NAME_EN"));
                dc.addSector(s);
            }

        }.read(sectors);

        this.sectorsymbolMdpcn = new MdpexSymbolReader(EnumSet.of(DP_TEAM)) {
            @Override
            ItemWithNamesDp2 getItem() {
                return (ItemWithNamesDp2) dc.getSector(getLong("SECTOR"));
            }
        }.read(sectorSymbols);
    }

    private void readCurrencies(File currencies, File currencySymbols) throws Exception {
        this.currencyMdpcn = new MdpexExportReader() {
            @Override
            protected void handleRow() {
                String name = get("NAME");
                CurrencyDp2 c = new CurrencyDp2(getLong("MDPID"), name);
                c.setNames(Language.de, name);
                c.setNames(Language.en, get("NAME_EN"));
                dc.addCurrency(c);
            }

        }.read(currencies);

        this.currencysymbolMdpcn = new MdpexSymbolReader(EnumSet.of(ISO)) {
            @Override
            ItemWithNamesDp2 getItem() {
                return (ItemWithNamesDp2) dc.getCurrency(getLong("CURRENCY"));
            }
        }.read(currencySymbols);
    }

    private void readCountries(File countries, File countrySymbols) throws Exception {
        this.countryMdpcn = new MdpexExportReader() {
            @Override
            protected void handleRow() {
                String name = get("NAME");
                CountryDp2 c = new CountryDp2(getLong("MDPID"), name);
                c.setNames(Language.de, name);
                c.setNames(Language.en, get("NAME_EN"));
                c.setCurrency(getCurrency(getLong("CURRENCY")));
                dc.addCountry(c);
            }

        }.read(countries);

        this.countrysymbolMdpcn = new MdpexSymbolReader(EnumSet.of(ISO)) {
            @Override
            ItemWithNamesDp2 getItem() {
                return (ItemWithNamesDp2) getCountry(getLong("COUNTRY"));
            }
        }.read(countrySymbols);
    }

    private void readMarkets(File markets, File marketSymbols) throws Exception {
        this.marketMdpcn = new MdpexExportReader() {
            @Override
            protected void handleRow() {
                String name = get("NAME");
                MarketDp2 m = new MarketDp2(getLong("MDPID"), name);
                m.setNames(Language.de, name);
                m.setNames(Language.en, get("NAME_EN"));
                m.setNames(Language.it, get("NAME_IT"));
                m.setCountry(getCountry(getLong("COUNTRY")));
                m.setMarketcategory(MarketcategoryEnum.valueOf(getInt("MARKETCATEGORY")));
                dc.addMarket(m);
            }
        }.read(markets);

        this.marketsymbolMdpcn = new MdpexSymbolReader(EnumSet.of(ISO, VWDFEED, WM, MM, DP_TEAM)) {
            @Override
            ItemWithNamesDp2 getItem() {
                return (ItemWithNamesDp2) dc.getMarket(getLong("MARKET"));
            }
        }.read(marketSymbols);
    }

    private Country getCountry(Long id) {
        return (id != null) ? dc.getCountry(id) : dc.getCountry(0L);
    }

    private Currency getCurrency(Long id) {
        return (id != null) ? dc.getCurrency(id) : dc.getCurrency(0);
    }


    public static void main(String[] args) throws Exception {
        MdpexDomainContextReader r = new MdpexDomainContextReader();
        long then = System.currentTimeMillis();
        r.readContext(new File("/Users/oflege/tmp/"));
        long now = System.currentTimeMillis();
        System.out.println("Took " + (now - then));
        System.out.println(r.sectorMdpcn);
        System.out.println(r.sectorsymbolMdpcn);
        System.out.println(r.currencyMdpcn);
        System.out.println(r.currencysymbolMdpcn);
        System.out.println(r.countryMdpcn);
        System.out.println(r.countrysymbolMdpcn);
        System.out.println(r.marketMdpcn);
        System.out.println(r.marketsymbolMdpcn);

        DomainContextSerializer s = new DomainContextSerializer();
        byte[] serialize = s.serialize(r.dc);
        System.out.println(serialize.length + " bytes");
    }
}
