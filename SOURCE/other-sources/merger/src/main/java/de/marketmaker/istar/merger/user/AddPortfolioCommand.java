/*
 * AddWatchlistCommand.java
 *
 * Created on 07.08.2006 09:37:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AddPortfolioCommand {
    private Long userid;
    private String name;
    private boolean isWatchlist;
    private BigDecimal cash = BigDecimal.ZERO;
    private String currencycode = "EUR";

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

    public boolean isWatchlist() {
        return isWatchlist;
    }

    public void setWatchlist(boolean watchlist) {
        isWatchlist = watchlist;
    }

    public String getCurrencycode() {
        return currencycode;
    }

    public void setCurrencycode(String currencycode) {
        this.currencycode = currencycode;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        if (cash != null) {
            this.cash = cash;
        }
    }
}
