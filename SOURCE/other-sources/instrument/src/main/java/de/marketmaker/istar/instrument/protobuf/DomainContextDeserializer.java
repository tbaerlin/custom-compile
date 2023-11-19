/*
 * DomainContextDeserializer.java
 *
 * Created on 20.06.12 10:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.protobuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import de.marketmaker.istar.common.io.ByteBufferInputStream;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domainimpl.CountryDp2;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.ItemWithNamesDp2;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.SectorDp2;

/**
 * @author oflege
 */
public class DomainContextDeserializer {
    private static final KeysystemEnum[] KEYSYSTEM_ENUMS = KeysystemEnum.values();

    private static final MarketcategoryEnum[] MARKETCATEGORY_ENUMS = MarketcategoryEnum.values();

    DomainContext deserialize(byte[] bb) throws Exception {
        return deserialize(InstrumentProtos.DomainContext.parseFrom(bb));
    }

    public DomainContextImpl deserialize(ByteBuffer bb) throws IOException {
        return deserialize(InstrumentProtos.DomainContext.parseFrom(new ByteBufferInputStream(bb)));
    }

    private DomainContextImpl deserialize(InstrumentProtos.DomainContext dc) {
        DomainContextImpl result = new DomainContextImpl();

        for (InstrumentProtos.Currency c: dc.getCurrenciesList()) {
            CurrencyDp2 currencyDp2 = new CurrencyDp2(c.getId(), c.getName());
            setSymbols(currencyDp2, c.getSymbolsList());
            setNames(currencyDp2, c.getNames());
            result.addCurrency(currencyDp2);
        }

        for (InstrumentProtos.Country c : dc.getCountriesList()) {
            CountryDp2 countryDp2 = new CountryDp2(c.getId(), c.getName());
            setSymbols(countryDp2, c.getSymbolsList());
            setNames(countryDp2, c.getNames());
            countryDp2.setCurrency(result.getCurrency(c.getCurrencyId()));
            result.addCountry(countryDp2);
        }

        for (InstrumentProtos.Market m: dc.getMarketsList()) {
            MarketDp2 marketDp2 = new MarketDp2(m.getId(), m.getName());
            setSymbols(marketDp2, m.getSymbolsList());
            setNames(marketDp2, m.getNames());
            marketDp2.setCountry(result.getCountry(m.getCountryId()));
            marketDp2.setMarketcategory(MARKETCATEGORY_ENUMS[m.getCategoryOrd()]);
            result.addMarket(marketDp2);
        }

        for (InstrumentProtos.Sector s : dc.getSectorsList()) {
            SectorDp2 sectorDp2 = new SectorDp2(s.getId(), s.getName());
            setSymbols(sectorDp2, s.getSymbolsList());
            setNames(sectorDp2, s.getNames());
            result.addSector(sectorDp2);
        }

        return result;
    }

    private void setSymbols(ItemWithSymbolsDp2 item, List<InstrumentProtos.Symbol> symbols) {
        for (InstrumentProtos.Symbol ref : symbols) {
            if (ref.getKeysystemOrd() < KEYSYSTEM_ENUMS.length) {
                item.setSymbol(KEYSYSTEM_ENUMS[ref.getKeysystemOrd()], ref.getValue());
            }
        }
    }

    private void setNames(ItemWithNamesDp2 item, InstrumentProtos.LocalizedName names) {
        if (names.hasDe()) {
            item.setNames(Language.de, names.getDe());
        }
        if (names.hasEn()) {
            item.setNames(Language.en, names.getEn());
        }
        if (names.hasFr()) {
            item.setNames(Language.fr, names.getFr());
        }
        if (names.hasIt()) {
            item.setNames(Language.it, names.getIt());
        }
        if (names.hasNl()) {
            item.setNames(Language.nl, names.getNl());
        }
    }
}
