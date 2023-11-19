/*
 * SymbolCommand.java
 *
 * Created on 01.08.2006 12:49:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.NotNull;

/**
 * Base class for commands that refer to a specific instrument or quote
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultSymbolCommand implements SymbolCommand {
    private SymbolStrategyEnum symbolStrategy;
    private String symbol;
    private String market;
    private String marketStrategy;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMarketStrategy() {
        return marketStrategy;
    }

    public void setMarketStrategy(String marketStrategy) {
        this.marketStrategy = marketStrategy;
    }

    @NotNull
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SymbolStrategyEnum getSymbolStrategy() {
        return symbolStrategy;
    }

    public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
        this.symbolStrategy = symbolStrategy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(40).append(this.symbol);
        if (this.symbolStrategy != null) {
            sb.append(", ss=").append(this.symbolStrategy);
        }
        if (this.market != null) {
            sb.append(", m=").append(this.market);
        }
        if (this.marketStrategy != null) {
            sb.append(", ms=").append(this.marketStrategy);
        }

        return sb.toString();
    }
}
