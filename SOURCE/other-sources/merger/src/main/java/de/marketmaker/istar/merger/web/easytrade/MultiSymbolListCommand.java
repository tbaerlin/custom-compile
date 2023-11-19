/*
 * MultiSymbolListCommand.java
 *
 * Created on 01.08.2006 13:13:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.merger.web.HttpRequestUtil;

/**
 * @author Michael Lösch
 */
public class MultiSymbolListCommand extends ListCommandWithOptionalPaging implements MultiSymbolCommand {
    private SymbolStrategyEnum symbolStrategy;

    private String[] symbol;

    private String market;

    private String marketStrategy;

    private String[] marketStrategyOverride;

    public String[] getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String[] symbol) {
        this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
    }

    public SymbolStrategyEnum getSymbolStrategy() {
        return this.symbolStrategy;
    }

    public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
        this.symbolStrategy = symbolStrategy;
    }

    public String getMarket() {
        return this.market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMarketStrategy() {
        return this.marketStrategy;
    }

    public void setMarketStrategy(String marketStrategy) {
        this.marketStrategy = marketStrategy;
    }

    public String[] getMarketStrategyOverride() {
        return this.marketStrategyOverride;
    }

    public void setMarketStrategyOverride(String[] marketStrategyOverride) {
        this.marketStrategyOverride = marketStrategyOverride;
    }
}
