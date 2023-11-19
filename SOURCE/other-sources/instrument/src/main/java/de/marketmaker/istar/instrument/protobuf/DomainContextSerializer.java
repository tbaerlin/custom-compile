/*
 * DomainContextSerializer.java
 *
 * Created on 19.06.12 18:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.protobuf;

import java.util.Map;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.ItemWithNames;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.Sector;
import de.marketmaker.istar.domainimpl.CountryDp2;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.SectorDp2;

/**
 * @author oflege
 */
public class DomainContextSerializer {
    public byte[] serialize(DomainContextImpl dc) {
        InstrumentProtos.DomainContext.Builder b = InstrumentProtos.DomainContext.newBuilder();

        for (Market market : dc.getMarkets()) {
            if (market.getId() == 0) continue;
            MarketDp2 m2 = (MarketDp2) market;
            InstrumentProtos.Market.Builder mb = InstrumentProtos.Market.newBuilder();
            mb.setId(m2.getId());
            for (Map.Entry<KeysystemEnum, String> entry : m2.getSymbols()) {
                InstrumentProtos.Symbol.Builder sb = InstrumentProtos.Symbol.newBuilder();
                sb.setKeysystemOrd(entry.getKey().ordinal());
                sb.setValue(entry.getValue());
                mb.addSymbols(sb);
            }
            mb.setCategoryOrd(m2.getMarketcategory().ordinal());
            mb.setCountryId(m2.getCountry().getId());
            mb.setName(m2.getName());
            mb.setNames(createLocalizedNameBuilder(m2));
            b.addMarkets(mb);
        }

        for (Country country : dc.getCountries()) {
            if (country.getId() == 0) continue;
            CountryDp2 c2 = (CountryDp2) country;
            InstrumentProtos.Country.Builder cb = InstrumentProtos.Country.newBuilder();
            cb.setId(c2.getId());
            for (Map.Entry<KeysystemEnum, String> entry : c2.getSymbols()) {
                InstrumentProtos.Symbol.Builder sb = InstrumentProtos.Symbol.newBuilder();
                sb.setKeysystemOrd(entry.getKey().ordinal());
                sb.setValue(entry.getValue());
                cb.addSymbols(sb);
            }
            cb.setName(c2.getName());
            cb.setNames(createLocalizedNameBuilder(c2));
            cb.setCurrencyId(c2.getCurrency().getId());
            b.addCountries(cb);
        }

        for (Currency currency : dc.getCurrencies()) {
            if (currency.getId() == 0) continue;
            CurrencyDp2 c2 = (CurrencyDp2) currency;
            InstrumentProtos.Currency.Builder cb = InstrumentProtos.Currency.newBuilder();
            cb.setId(c2.getId());
            for (Map.Entry<KeysystemEnum, String> entry : c2.getSymbols()) {
                InstrumentProtos.Symbol.Builder sb = InstrumentProtos.Symbol.newBuilder();
                sb.setKeysystemOrd(entry.getKey().ordinal());
                sb.setValue(entry.getValue());
                cb.addSymbols(sb);
            }
            cb.setName(c2.getName());
            cb.setNames(createLocalizedNameBuilder(c2));
            b.addCurrencies(cb);
        }

        for (Sector sector : dc.getSectors()) {
            if (sector.getId() == 0) continue;
            SectorDp2 s2 = (SectorDp2) sector;
            InstrumentProtos.Sector.Builder seb = InstrumentProtos.Sector.newBuilder();
            seb.setId(s2.getId());
            for (Map.Entry<KeysystemEnum, String> entry : s2.getSymbols()) {
                InstrumentProtos.Symbol.Builder sb = InstrumentProtos.Symbol.newBuilder();
                sb.setKeysystemOrd(entry.getKey().ordinal());
                sb.setValue(entry.getValue());
                seb.addSymbols(sb);
            }
            seb.setName(s2.getName());
            seb.setNames(createLocalizedNameBuilder(s2));
            b.addSectors(seb);
        }
        return b.build().toByteArray();
    }

    private InstrumentProtos.LocalizedName.Builder createLocalizedNameBuilder(ItemWithNames iwn) {
        InstrumentProtos.LocalizedName.Builder lnb
                = InstrumentProtos.LocalizedName.newBuilder();
        String de = iwn.getName(Language.de);
        if (de != null) lnb.setDe(de);
        
        String en = iwn.getName(Language.en);
        if (en != null) lnb.setEn(en);
        
        String fr = iwn.getName(Language.fr);
        if (fr != null) lnb.setFr(fr);
        
        String it = iwn.getName(Language.it);
        if (it != null) lnb.setIt(it);
        
        String nl = iwn.getName(Language.nl);
        if (nl != null) lnb.setNl(nl);
        
        return lnb;
    }
}
