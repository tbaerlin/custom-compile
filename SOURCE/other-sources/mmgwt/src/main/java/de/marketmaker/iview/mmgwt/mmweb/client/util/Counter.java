package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * Created on 21.02.13 08:55
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class Counter {
    private int value;

    public Counter(int startValue) {
        this.value = startValue;
    }

    public Counter() {
        this(0);
    }

    public int inc() {
        return ++value;
    }

    public int getValue() {
        return this.value;
    }
}