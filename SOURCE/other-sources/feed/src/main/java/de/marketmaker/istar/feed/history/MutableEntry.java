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
public class MutableEntry {
    private int days;

    private byte[] data;

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getDataDecompress() {
        return EntryFactory.decompress(this.data);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public MutableEntry copy() {
        MutableEntry entry = new MutableEntry();
        entry.setDays(this.days);
        entry.setData(this.data);

        return entry;
    }
}
