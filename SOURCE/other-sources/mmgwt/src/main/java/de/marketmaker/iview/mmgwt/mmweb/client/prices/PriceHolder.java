/*
 * PriceHolder.java
 *
 * Created on 05.03.2010 14:45:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

import de.marketmaker.iview.dmxml.MSCOrderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushOrderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;

/**
 * Keeps information about a price
 */
class PriceHolder extends Holder {
    private Orderbook orderbook;

    private Price price;

    PriceHolder(Price price) {
        this.price = price;
    }

    PriceHolder(MSCOrderbook data) {
        this.orderbook = new Orderbook(data);
    }

    Price getPrice() {
        return price;
    }

    Orderbook getOrderbook() {
        return orderbook;
    }

    void setGeneration(int generation) {
        if (this.price != null) {
            price.setGeneration(generation);
        }
        if (this.orderbook != null) {
            orderbook.setGeneration(generation);
        }
    }

    public void pushUpdate(PushPrice pushPrice) {
        this.price.copyFrom(pushPrice);
    }

    public void pushUpdate(PushOrderbook orderbook) {
        this.orderbook.copyFrom(orderbook);
    }

    public void update(Price price) {
        if (this.price == null) {
            this.price = price;
        }
        else {
            this.price.copyFrom(price);
        }
    }

    public void update(MSCOrderbook data) {
        if (this.orderbook == null) {
            this.orderbook = new Orderbook(data);
        }
        else {
            this.orderbook.update(data);
        }
    }

    @Override
    protected void removePrevious() {
        if (this.price != null) {
            this.price.setPrevious(null);
        }
        // an orderbook has no previous values
    }
}
