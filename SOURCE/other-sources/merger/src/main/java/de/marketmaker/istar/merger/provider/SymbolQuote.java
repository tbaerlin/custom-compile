/*
 * SymbolQuote.java
 *
 * Created on 14.07.2009 09:42:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * Intended to replace Quote objects in requests that are sent over the wire, due to its small
 * size de-/serialization is much more efficient than for Quotes.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SymbolQuote implements Serializable {
    static final long serialVersionUID = 1L;

    private final long id;

    private final String symbolVwdfeed;

    private final String symbolMmwkn;

    private final String currencyIso;

    private final int quotedef;

    private final boolean cent;

    public static List<SymbolQuote> create(List<Quote> quotes) {
        if (quotes == null) {
            return null;
        }
        final ArrayList<SymbolQuote> result = new ArrayList<>(quotes.size());
        for (Quote quote : quotes) {
            result.add(create(quote));
        }
        return result;
    }

    public static SymbolQuote create(Quote q) {
        if (q == null) {
            return null;
        }
        return new SymbolQuote(q);
    }

    private SymbolQuote(Quote q) {
        this.id = q.getId();
        this.symbolVwdfeed = q.getSymbolVwdfeed();
        this.symbolMmwkn = q.getSymbolMmwkn();
        this.currencyIso = q.getCurrency().getSymbolIso();
        this.quotedef = q.getQuotedef();
        this.cent = q.getCurrency().isCent();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getId() {
        return id;
    }

    public String getSymbolVwdfeed() {
        return symbolVwdfeed;
    }

    public String getSymbolMmwkn() {
        return symbolMmwkn;
    }

    public String getCurrencyIso() {
        return currencyIso;
    }

    public int getQuotedef() {
        return quotedef;
    }

    public boolean isCent() {
        return cent;
    }

    @Override
    public String toString() {
        return "SymbolQuote[" + this.id
                + ".qid, vwdfeed='" + symbolVwdfeed + '\''
                + ", mmwkn='" + symbolMmwkn + '\''
                + ", currency='" + currencyIso + '\''
                + ']';
    }
}
