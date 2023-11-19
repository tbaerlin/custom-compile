/*
 * Entry.java
 *
 * Created on 26.10.12 15:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

/**
 * @author zzhao
 */
public class MutableTickEntry extends MutableEntry {
    private int tickNum;

    public int getTickNum() {
        return tickNum;
    }

    public void setTickNum(int tickNum) {
        this.tickNum = tickNum;
    }

    @Override
    public MutableTickEntry copy() {
        MutableTickEntry entry = new MutableTickEntry();
        entry.setDays(getDays());
        entry.setTickNum(getTickNum());
        entry.setData(getData());

        return entry;
    }
}
