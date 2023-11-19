/*
 * RemoveOrderCommand.java
 *
 * Created on 07.08.2006 13:58:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RemoveOrderCommand {
    protected Long userid;
    protected Long portfolioid;
    protected Long orderid;

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

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
}
