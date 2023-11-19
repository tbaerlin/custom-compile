/*
 * UpdatePositionCommand.java
 *
 * Created on 03.08.2006 15:29:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.common.validator.NotNull;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UpdatePositionCommand {
    protected Long userid;

    protected Long portfolioid;

    protected Long positionid;

    protected String symbol;

    @NotNull
    public Long getPositionid() {
        return positionid;
    }

    public void setPositionid(Long positionid) {
        this.positionid = positionid;
    }

    @NotNull
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    @NotNull
    public Long getPortfolioid() {
        return portfolioid;
    }

    public void setPortfolioid(Long portfolioid) {
        this.portfolioid = portfolioid;
    }

    @NotNull
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}
