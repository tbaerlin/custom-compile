/*
 * DomainContext.java
 *
 * Created on 15.08.2005 12:19:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.util.List;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.Sector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DomainContext {
    Market getMarket(long id);

    Currency getCurrency(long id);

    Country getCountry(long id);

    Sector getSector(long id);

    List<Market> getMarkets();

    List<Currency> getCurrencies();

    List<Country> getCountries();

    List<Sector> getSectors();
}
