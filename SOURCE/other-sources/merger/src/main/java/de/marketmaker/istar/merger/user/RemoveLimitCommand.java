/*
 * RemoveLimitCommand.java
 *
 * Created on 13.09.2006 12:24:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RemoveLimitCommand {
    private Long userid;

    private Long portfolioid;

    private Long positionid;

    private Long limitid;

    public Long getLimitid() {
        return limitid;
    }

    public void setLimitid(Long limitid) {
        this.limitid = limitid;
    }

    public Long getPortfolioid() {
        return portfolioid;
    }

    public void setPortfolioid(Long portfolioid) {
        this.portfolioid = portfolioid;
    }

    public Long getPositionid() {
        return positionid;
    }

    public void setPositionid(Long positionid) {
        this.positionid = positionid;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

}
