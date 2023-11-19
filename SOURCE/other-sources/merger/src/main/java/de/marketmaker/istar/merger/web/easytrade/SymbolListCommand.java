/*
 * SymbolListCommand.java
 *
 * Created on 01.08.2006 13:13:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.NotNull;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SymbolListCommand extends ListCommandWithOptionalPaging implements SymbolCommand {
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
}
