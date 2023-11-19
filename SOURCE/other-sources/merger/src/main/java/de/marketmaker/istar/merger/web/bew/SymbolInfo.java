/*
 * SymbolInfo.java
 *
 * Created on 06.10.2010 10:40:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

/**
 * @author oflege
 */
class SymbolInfo {
    private final String symbol;
    private final String exchange;
    private final String vwdSymbol;
    private final String vwdExchange;
    private final long iid;

    SymbolInfo(String symbol, String exchange, String vwdSymbol, String vwdExchange, long iid) {
        this.symbol = symbol;
        this.exchange = exchange;
        this.vwdSymbol = vwdSymbol;
        this.vwdExchange = vwdExchange;
        this.iid = iid;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public String getVwdSymbol() {
        return vwdSymbol;
    }

    public String getVwdExchange() {
        return vwdExchange;
    }

    public long getIid() {
        return iid;
    }
}
