/*
 * ImgPortfolioHistoryCommand.java
 *
 * Created on 10.08.2015 11:31:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.MscPortfolioVaRLight;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgPortfolioHistoryCommand extends BaseImgCommand {

    private MscPortfolioVaRLight.Position[] position;

    private LocalDate date;

    private String currency;

    private SymbolStrategyEnum symbolStrategy;

    private String marketStrategy;

    private Period period;

    public ImgPortfolioHistoryCommand() {
        super(300, 200);
    }

    public MscPortfolioVaRLight.Position[] getPosition() {
        return position;
    }

    public void setPosition(MscPortfolioVaRLight.Position[] position) {
        this.position = position;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public SymbolStrategyEnum getSymbolStrategy() {
        return symbolStrategy;
    }

    public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
        this.symbolStrategy = symbolStrategy;
    }

    public String getMarketStrategy() {
        return marketStrategy;
    }

    public void setMarketStrategy(String marketStrategy) {
        this.marketStrategy = marketStrategy;
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

    @Override
    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        for (MscPortfolioVaRLight.Position p : this.position) {
            final String[] str;
            if (p.getPurchasePrice() == null) {
                str = new String[]{p.getSymbol(), p.getQuantity().toString()};
            }
            else {
                str = new String[]{p.getSymbol(), p.getQuantity().toString(), p.getPurchasePrice().toString()};
            }

            appendParameters(sb, str, "position");
        }

        appendParameter(sb, this.symbolStrategy, "symbolStrategy");
        appendParameter(sb, this.marketStrategy, "marketStrategy");
        appendParameter(sb, this.currency, "currency");
        appendParameter(sb, this.date.toString(), "date");
        appendParameter(sb, this.period, "period");

        return sb;
    }
}
