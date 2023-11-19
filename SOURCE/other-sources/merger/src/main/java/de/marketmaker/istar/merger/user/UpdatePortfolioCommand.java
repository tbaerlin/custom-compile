/*
 * UpdateWatchlistCommand.java
 *
 * Created on 07.08.2006 09:52:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UpdatePortfolioCommand {
    private Long userid;
    private Long portfolioid;
    private String name;
    private boolean watchlist;
    private BigDecimal cash = BigDecimal.ZERO;

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Long getPortfolioid() {
        return portfolioid;
    }

    public void setPortfolioid(Long portfolioid) {
        this.portfolioid = portfolioid;
    }

    boolean isWatchlist() {
        return watchlist;
    }

    void setWatchlist(boolean watchlist) {
        this.watchlist = watchlist;
    }
}
