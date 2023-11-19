/*
 * PushData.java
 *
 * Created on 08.03.2010 15:05:34
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author oflege
 */
public class PushData implements Serializable {
    private ArrayList<PushPrice> prices;

    private ArrayList<PushOrderbook> orderbooks;

    // needs to be volatile as it is set by external thread, not the periodic pusher's thread
    private volatile boolean stopPush = false;

    public ArrayList<PushPrice> getPrices() {
        return this.prices;
    }

    public ArrayList<PushOrderbook> getOrderbooks() {
        return this.orderbooks;
    }

    public void add(PushPrice price) {
        if (this.prices == null) {
            this.prices = new ArrayList<>();
        }
        this.prices.add(price);
    }

    public void add(PushOrderbook orderbook) {
        if (this.orderbooks == null) {
            this.orderbooks = new ArrayList<>();
        }
        this.orderbooks.add(orderbook);
    }

    public void setStopPush() {
        this.stopPush = true;
    }

    public boolean isStopPush() {
        return this.stopPush;
    }

    public boolean isEmpty() {
        return this.prices == null && this.orderbooks == null && !this.stopPush;
    }

    public void clear() {
        this.prices = null;
        this.orderbooks = null;
    }
}
