/*
 * Limit.java
 *
 * Created on 24.07.2006 15:52:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Limit implements Serializable {
    public enum Comparison {
        GT {
            boolean matches(int cmp) {
                return cmp > 0;
            }
        },
        GEQ {
            boolean matches(int cmp) {
                return cmp >= 0;
            }
        },
        LT {
            boolean matches(int cmp) {
                return cmp < 0;
            }
        },
        LEQ {
            boolean matches(int cmp) {
                return cmp <= 0;
            }
        };

        abstract boolean matches(int cmp);
    }

    static final long serialVersionUID = 1L;

    private long id;

    private long userid;

    private long quoteid;

    private Comparison comparison;

    private BigDecimal price;

    private BigDecimal priceWhenRaised;

    private String message;

    private String note;

    private DateTime raisedon;

    private DateTime notifiedon;

    private DateTime createdon;

    public Limit() {
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(80);
        sb.append("Limit[").append(this.id)
                .append(", uid=").append(this.userid)
                .append(", qid=").append(this.quoteid)
                .append(", ").append(this.comparison).append(" ").append(this.price)
                .append(", message=").append(this.message)
                .append(", note=").append(this.note)
                .append(", createdon=").append(this.createdon);
        if (this.raisedon != null) {
            sb.append(", raisedon=").append(this.raisedon)
                    .append(", pricewhenraised=").append(this.priceWhenRaised)
                    .append(", notifiedon=").append(this.notifiedon);
        }
        sb.append("]");
        return sb.toString();
    }

    Limit deepCopy() {
        final Limit result = new Limit();
        result.id = this.id;
        result.userid = this.userid;
        result.quoteid = this.quoteid;
        result.comparison = this.comparison;
        result.price = this.price;
        result.priceWhenRaised = this.priceWhenRaised;
        result.message = this.message;
        result.note = this.note;
        result.raisedon = this.raisedon;
        result.notifiedon = this.notifiedon;
        result.createdon = this.createdon;
        return result;
    }

    public boolean isActive() {
        return this.raisedon == null;
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public long getUserid() {
        return userid;
    }

    void setUserid(long userid) {
        this.userid = userid;
    }

    public long getQuoteid() {
        return quoteid;
    }

    public void setQuoteid(long quoteid) {
        this.quoteid = quoteid;
    }

    public Comparison getComparison() {
        return comparison;
    }

    void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public BigDecimal getPrice() {
        return price;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    public String getNote() {
        return note;
    }

    void setNote(String note) {
        this.note = note;
    }

    public DateTime getCreatedon() {
        return createdon;
    }

    void setCreatedon(DateTime createdon) {
        this.createdon = createdon;
    }

    public DateTime getNotifiedon() {
        return notifiedon;
    }

    public void setNotifiedon(DateTime notifiedon) {
        this.notifiedon = notifiedon;
    }

    public BigDecimal getPriceWhenRaised() {
        return priceWhenRaised;
    }

    public void setPriceWhenRaised(BigDecimal priceWhenRaised) {
        this.priceWhenRaised = priceWhenRaised;
    }

    public DateTime getRaisedon() {
        return raisedon;
    }

    public void setRaisedon(DateTime raisedon) {
        this.raisedon = raisedon;
    }

    public boolean isTriggeredBy(BigDecimal bd) {
        if (bd == null || bd.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.comparison.matches(bd.compareTo(this.price));
    }
}
