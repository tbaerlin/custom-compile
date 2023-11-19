/*
 * BaseMultiSymbolCommand.java
 *
 * Created on 11.03.2010 11:27:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.merger.web.HttpRequestUtil;

/**
 * @author oflege
 */
public class BaseMultiSymbolCommand implements MultiSymbolCommand {
    private SymbolStrategyEnum symbolStrategy;

    private String[] symbol;

    private String market;

    private String marketStrategy;

    private String[] marketStrategyOverride;

    public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
        this.symbolStrategy = symbolStrategy;
    }

    public void setSymbol(String[] symbol) {
        this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public void setMarketStrategy(String marketStrategy) {
        this.marketStrategy = marketStrategy;
    }

    public void setMarketStrategyOverride(String[] marketStrategyOverride) {
        this.marketStrategyOverride = marketStrategyOverride;
    }

    public SymbolStrategyEnum getSymbolStrategy() {
        return this.symbolStrategy;
    }

    public String[] getSymbol() {
        return this.symbol;
    }

    public String getMarketStrategy() {
        return this.marketStrategy;
    }

    public String[] getMarketStrategyOverride() {
        return this.marketStrategyOverride;
    }

    public String getMarket() {
        return this.market;
    }
}
