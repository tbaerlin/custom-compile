/*
 * DeletePositionCommand.java
 *
 * Created on 07.08.2006 09:22:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RemovePositionCommand {
    protected Long userid;
    protected Long portfolioid;
    protected Long positionid;

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

    public Long getPositionid() {
        return positionid;
    }

    public void setPositionid(Long positionid) {
        this.positionid = positionid;
    }
}
