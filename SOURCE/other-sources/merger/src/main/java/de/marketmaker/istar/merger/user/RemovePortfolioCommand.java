/*
 * RemoveWatchlistCommand.java
 *
 * Created on 07.08.2006 10:13:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RemovePortfolioCommand {
    protected Long userid;
    protected Long portfolioid;

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
}
