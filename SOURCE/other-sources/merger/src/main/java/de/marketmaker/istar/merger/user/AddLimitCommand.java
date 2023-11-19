/*
 * AddLimitCommand.java
 *
 * Created on 13.09.2006 12:24:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AddLimitCommand {
    private Long userid;

    private Long portfolioid;

    private Long positionid;

    private String message;

    private String note;

    private Limit.Comparison comparison;

    private BigDecimal price;

    public Limit.Comparison getComparison() {
        return this.comparison;
    }

    public void setComparison(Limit.Comparison comparison) {
        this.comparison = comparison;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getPortfolioid() {
        return this.portfolioid;
    }

    public void setPortfolioid(Long portfolioid) {
        this.portfolioid = portfolioid;
    }

    public Long getPositionid() {
        return this.positionid;
    }

    public void setPositionid(Long positionid) {
        this.positionid = positionid;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getUserid() {
        return this.userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }
}
