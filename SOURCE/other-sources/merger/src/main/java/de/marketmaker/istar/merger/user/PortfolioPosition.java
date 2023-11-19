/*
 * WatchlistElement.java
 *
 * Created on 24.07.2006 14:25:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioPosition implements Serializable {
    static final long serialVersionUID = 1L;

    private long id;

    private long iid;

    private long qid;

    private InstrumentTypeEnum instrumentType;

    private boolean quotedPerPercent;

    private List<Order> orders;

    PortfolioPosition() {
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append("Position[")
                .append(this.id)
                .append(", qid=").append(this.qid)
                .append(", iid=").append(this.iid)
                .append(", per%=").append(this.quotedPerPercent);
        if (orders != null) {
            sb.append(", orders=").append(this.orders);
        }
        return sb.toString();
    }

    PortfolioPosition deepCopy() {
        final PortfolioPosition result = new PortfolioPosition();
        result.id = this.id;
        result.iid = this.iid;
        result.qid = this.qid;
        result.instrumentType = this.instrumentType;
        result.quotedPerPercent = this.quotedPerPercent;

        if (this.orders == null) {
            result.orders = null;
        }
        else {
            result.orders = new ArrayList<>(this.orders.size());
            for (Order order : orders) {
                result.orders.add(order.deepCopy());
            }
        }
        return result;
    }

    public long getId() {
        return id;
    }

    public long getIid() {
        return iid;
    }

    public List<Order> getOrders() {
        return (orders != null
                ? Collections.unmodifiableList(this.orders)
                : Collections.<Order>emptyList());
    }

    public InstrumentTypeEnum getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(InstrumentTypeEnum instrumentType) {
        this.instrumentType = instrumentType;
    }

    public long getQid() {
        return qid;
    }

    void setId(long id) {
        this.id = id;
    }

    void setIid(long iid) {
        this.iid = iid;
    }

    public boolean isQuotedPerPercent() {
        return quotedPerPercent;
    }

    void setQuotedPerPercent(boolean quotedPerPercent) {
        this.quotedPerPercent = quotedPerPercent;
    }

    void setOrders(List<Order> orders) {
        if (orders != null) {
            this.orders = new ArrayList<>(orders);
            ensureSortedOrders();
        }
        else {
            this.orders = null;
        }
    }

    private void ensureSortedOrders() {
        this.orders.sort(Order.BY_DATE_COMPARATOR);
    }

    boolean removeOrder(long id) {
        final int n = getOrderIndex(id);
        if (n >= 0) {
            this.orders.remove(n);
            return true;
        }
        return false;
    }

    boolean updateOrder(Order o) {
        final int n = getOrderIndex(o.getId());
        if (n >= 0) {
            this.orders.set(n, o);
            ensureSortedOrders();
            return true;
        }
        return false;
    }

    void addOrder(Order o) {
        if (this.orders == null) {
            this.orders = new ArrayList<>();
        }
        this.orders.add(o);
        ensureSortedOrders();
    }

    Order getOrder(long id) {
        final int n = getOrderIndex(id);
        return (n >= 0) ? this.orders.get(n) : null;
    }

    private int getOrderIndex(long id) {
        if (this.orders == null) {
            return -1;
        }
        for (int i = 0; i < orders.size(); i++) {
            if (this.orders.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    void setQid(long qid) {
        this.qid = qid;
    }

    public BigDecimal getTotalVolume() {
        BigDecimal result = BigDecimal.ZERO;
        if (this.orders == null) {
            return result;
        }
        for (Order order : this.orders) {
            result = order.isBuy()
                ? result.add(order.getVolume())
                : result.subtract(order.getVolume());
        }
        return result;
    }
}
