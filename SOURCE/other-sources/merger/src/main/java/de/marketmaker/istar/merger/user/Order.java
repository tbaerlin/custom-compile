/*
 * Order.java
 *
 * Created on 03.08.2006 11:13:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Order implements Serializable {
    public final static Comparator<Order> BY_DATE_COMPARATOR = new Comparator<Order>() {
        public int compare(Order o1, Order o2) {
            int compareByDate = o1.getDate().compareTo(o2.getDate());
            int compareById = Long.compare(o1.getId(), o2.getId());
            return (compareByDate == 0 ? compareById : compareByDate);
        }
    };

    private long id;

    private BigDecimal price;

    private BigDecimal exchangerate;

    private BigDecimal volume;

    private BigDecimal charge;

    private boolean buy;

    private DateTime date;

    private long positionId;

    Order() {
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(50);
        sb.append(this.buy ? "Buy[" : "Sell[")
                .append(this.id)
                .append(", price=").append(this.price)
                .append(", exchrate=").append(this.exchangerate)
                .append(", volume=").append(this.volume)
                .append(", charge=").append(this.charge)
                .append(", date=").append(this.date)
                .append("]");
        return sb.toString();
    }

    Order deepCopy() {
        final Order result = new Order();
        result.id = this.id;
        result.price = this.price;
        result.exchangerate = this.exchangerate;
        result.volume = this.volume;
        result.charge = this.charge;
        result.buy = this.buy;
        result.date = this.date;
        result.positionId = this.positionId;
        return result;
    }

    void setBuy(boolean buy) {
        this.buy = buy;
    }

    void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    void setDate(DateTime date) {
        this.date = date;
    }

    void setExchangerate(BigDecimal exchangerate) {
        this.exchangerate = exchangerate;
    }

    void setId(long id) {
        this.id = id;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public boolean isBuy() {
        return buy;
    }

    public BigDecimal getCharge() {
        return this.charge != null ? this.charge : BigDecimal.ZERO;
    }

    public DateTime getDate() {
        return date;
    }

    public BigDecimal getExchangerate() {
        return exchangerate;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (buy != order.buy) return false;
        if (id != order.id) return false;
        if (positionId != order.positionId) return false;
        if (charge != null ? !charge.equals(order.charge) : order.charge != null) return false;
        if (date != null ? !date.equals(order.date) : order.date != null) return false;
        if (exchangerate != null ? !exchangerate.equals(order.exchangerate) : order.exchangerate != null)
            return false;
        if (price != null ? !price.equals(order.price) : order.price != null) return false;
        if (volume != null ? !volume.equals(order.volume) : order.volume != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (exchangerate != null ? exchangerate.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (charge != null ? charge.hashCode() : 0);
        result = 31 * result + (buy ? 1 : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (int) (positionId ^ (positionId >>> 32));
        return result;
    }
}
