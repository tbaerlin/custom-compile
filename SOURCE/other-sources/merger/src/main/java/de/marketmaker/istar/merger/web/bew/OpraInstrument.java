/*
 * OpraInstrument.java
 *
 * Created on 03.08.2010 11:44:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Issuer;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.Sector;
import de.marketmaker.istar.domain.instrument.DetailedInstrumentType;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MmInstrumentclass;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.ratios.opra.OpraItem;

/**
 * @author oflege
 */
class OpraInstrument implements Instrument {
    private final String symbol;

    private final List<Quote> quotes;

    OpraInstrument(String symbol, List<OpraItem> items) {
        this.symbol = symbol;
        this.quotes = new ArrayList<>(items.size());
        for (OpraItem item : items) {
            this.quotes.add(new OpraQuote(item, this));
        }
    }

    @Override
    public String toString() {
        return "OpraInstrument[" + this.symbol + ", items="  + this.quotes + "]";
    }

    @Override
    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.OPT;
    }

    @Override
    public MmInstrumentclass getMmInstrumentclass() {
        return MmInstrumentclass.OPT;
    }

    @Override
    public Sector getSector() {
        return null;
    }

    @Override
    public List<Quote> getQuotes() {
        return this.quotes;
    }

    @Override
    public Market getHomeExchange() {
        return null;
    }

    @Override
    public boolean isHomeExchangeByHeuristic() {
        return false;
    }

    @Override
    public Market getHomeExchangeWithoutHeuristic() {
        return getHomeExchange();
    }

    @Override
    public Issuer getIssuer() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Country getCountry() {
        return null;
    }

    @Override
    public String getLei() {
        return null;
    }

    @Override
    public int getExpirationDate() {
        return 0;
    }

    @Override
    public DateTime getExpiration() {
        return null;
    }

    @Override
    public DetailedInstrumentType getDetailedInstrumentType() {
        return null;
    }

    @Override
    public Quote getQuote(long quoteid) {
        return null;
    }

    @Override
    public String getSymbolWmGd195Id() {
        return null;
    }

    @Override
    public String getSymbolWmGd195Name() {
        return null;
    }

    @Override
    public String getSymbolIsin() {
        return null;
    }

    @Override
    public String getSymbolWkn() {
        return null;
    }

    @Override
    public String getSymbolOeWkn() {
        return null;
    }

    @Override
    public String getSymbolValorsymbol() {
        return null;
    }

    @Override
    public String getSymbolValor() {
        return null;
    }

    @Override
    public String getSymbolSedol() {
        return null;
    }

    @Override
    public String getSymbolCusip() {
        return null;
    }

    @Override
    public String getSymbolEurexTicker() {
        return null;
    }

    @Override
    public String getSymbolTicker() {
        return null;
    }

    @Override
    public String getSymbolWmGd664() {
        return null;
    }

    @Override
    public String getSymbolWmGd260() {
        return null;
    }

    @Override
    public String getSymbolMmName() {
        return null;
    }

    @Override
    public String getSymbol(KeysystemEnum id) {
        return null;
    }

    @Override
    public String getSymbol(String id) {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }
}
