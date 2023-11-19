/*
 * Instrument.java
 *
 * Created on 17.09.2004 09:16:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Issuer;
import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.Sector;

/**
 * An instrument identifies a financial product traded on financial market. One instrument can be traded
 * on different financial market. The reference of this instrument on each financial market is known as
 * a quote.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Instrument extends ItemWithSymbols {
    InstrumentTypeEnum getInstrumentType();

    MmInstrumentclass getMmInstrumentclass();

    Sector getSector();

    List<Quote> getQuotes();

    Market getHomeExchange();

    boolean isHomeExchangeByHeuristic();

    Market getHomeExchangeWithoutHeuristic();

    Issuer getIssuer();

    String getName();

    Country getCountry();

    String getLei();

    @Deprecated
    int getExpirationDate();
    DateTime getExpiration();

    DetailedInstrumentType getDetailedInstrumentType();

    Quote getQuote(long quoteid);

    String getSymbolWmGd195Id();
    String getSymbolWmGd195Name();
    String getSymbolIsin();
    String getSymbolWkn();
    String getSymbolOeWkn();
    String getSymbolValorsymbol();
    String getSymbolValor();
    String getSymbolSedol();
    String getSymbolCusip();
    String getSymbolEurexTicker();
    String getSymbolTicker();
    String getSymbolWmGd664();
    /**
     * @return security short name as delivered by WM
     */
    String getSymbolWmGd260();
    String getSymbolMmName();
}
