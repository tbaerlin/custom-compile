/*
 * EodPair.java
 *
 * Created on 16.01.13 11:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

/**
 * @author zzhao
 */
public class EodFieldPrice {

    private int date;

    private String price;

    EodFieldPrice() {
    }

    public void reset(int date, String price) {
        this.date = date;
        this.price = price;
    }

    public int getDate() {
        return date;
    }

    public String getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return date + ":" + price;
    }
}
