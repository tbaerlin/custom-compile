/*
 * TopItem.java
 *
 * Created on 14.12.2009 13:08:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

/**
 * @author oflege
 */
public class TopItem {
    private final int count;
    private final String value1;
    private final String value2;
    private String name;

    public TopItem(int count, String value1, String value2) {
        this.count = count;
        this.value1 = value1;
        this.value2 = value2;
        this.name = value1;
    }

    public int getCount() {
        return count;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.count + ": " + this.value1 + ", " + this.value2;
    }
}
