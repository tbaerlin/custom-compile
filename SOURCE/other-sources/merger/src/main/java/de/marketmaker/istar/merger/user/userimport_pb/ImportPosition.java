/*
 * ImportPosition.java
 *
 * Created on 23.11.2006 19:13:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_pb;

import java.math.BigDecimal;

import org.joda.time.YearMonthDay;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class ImportPosition {
    final String wkn;
    final String currency;
    final String market;
    final BigDecimal ordervalue;
    final YearMonthDay orderdate;
    final BigDecimal volume;
    final String notiz;

    public ImportPosition(String wkn, String currency, String market, BigDecimal ordervalue, YearMonthDay orderdate, BigDecimal volume, String notiz) {
        this.wkn = wkn;
        this.currency = currency;
        this.market = market;
        this.ordervalue = ordervalue;
        this.orderdate = orderdate;
        this.volume = volume;
        this.notiz = notiz;
    }

    public String getWkn() {
        return wkn;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMarket() {
        return market;
    }

    public BigDecimal getOrdervalue() {
        return ordervalue;
    }

    public YearMonthDay getOrderdate() {
        return orderdate;
    }

    public BigDecimal getVolume() {
        if(this.volume ==null) {
            return null;
        }
        return this.volume.compareTo(BigDecimal.ZERO)==0 ? BigDecimal.ONE:this.volume;
    }

    public String getNotiz() {
        return notiz;
    }

    public String toString() {
        return "ImportPosition[wkn=" + wkn
                + ", currency=" + currency
                + ", market=" + market
                + ", ordervalue=" + ordervalue
                + ", orderdate=" + orderdate
                + ", volume=" + volume
                + ", notiz=" + notiz
                + "]";
    }
}
