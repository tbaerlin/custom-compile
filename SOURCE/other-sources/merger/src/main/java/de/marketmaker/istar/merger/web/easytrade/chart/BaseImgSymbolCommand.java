/*
 * AbstractImgSymbolCommand.java
 *
 * Created on 28.08.2006 16:40:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import org.joda.time.Period;

import de.marketmaker.istar.common.validator.HasText;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BaseImgSymbolCommand extends BaseImgCommand implements SymbolCommand {
    private SymbolStrategyEnum symbolStrategy;

    private SymbolStrategyEnum benchmarkSymbolStrategy;

    private String symbol;

    private String market;

    private Period period;

    private String marketStrategy;

    // color of the main chart line
    private String color;

    public BaseImgSymbolCommand() {
    }

    protected BaseImgSymbolCommand(int width, int height, Period period) {
        super(width, height);
        this.period = period;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    @HasText
    @NotNull
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Render a chart for the given time period.
     * @sample P3M
     */
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public SymbolStrategyEnum getSymbolStrategy() {
        return symbolStrategy;
    }

    public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
        this.symbolStrategy = symbolStrategy;
    }

    /**
     * Symbol strategy specific for benchmarks, defaults to {@link #getSymbolStrategy}.
     */
    public SymbolStrategyEnum getBenchmarkSymbolStrategy() {
        return this.benchmarkSymbolStrategy == null ? getSymbolStrategy() : this.benchmarkSymbolStrategy;
    }

    public void setBenchmarkSymbolStrategy(SymbolStrategyEnum benchmarkSymbolStrategy) {
        this.benchmarkSymbolStrategy = benchmarkSymbolStrategy;
    }

    /**
     * Overrides the color of the chart's main line. The value can either be a hex rgb value (rrggbb),
     * in which case the other attributes of the main line are unchanged (line width, stroke, etc),
     * or the name of a dedicated line style (includes color, line width, stroke, etc). Contact us
     * if you need line style overrides.
     * @sample 00ff00
     */
    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMarketStrategy() {
        return this.marketStrategy;
    }

    public void setMarketStrategy(String marketStrategy) {
        this.marketStrategy = marketStrategy;
    }

    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        sb.append("&symbol=").append(this.symbol);
        appendParameter(sb, this.symbolStrategy, "symbolStrategy");
        appendParameter(sb, this.market, "market");
        appendParameter(sb, this.marketStrategy, "marketStrategy");
        appendParameter(sb, this.color, "color");
        appendParameter(sb, this.period, "period");
        return sb;
    }

    public boolean isIntraday() {
        return getPeriod() != null && getPeriod().getDays() > 0 && getPeriod().getDays() <= 10;
    }
}
