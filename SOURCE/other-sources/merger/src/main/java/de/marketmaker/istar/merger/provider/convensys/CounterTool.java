/*
 * JodaTool.java
 *
 * Created on 06.02.2008 17:18:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.istar.merger.provider.convensys;

/**
 * @author Ulrich Maurer
 */
public class CounterTool {
    private long defaultStart = 0;
    private long defaultIncrement = 1;

    public Counter getCounter() {
        return new Counter(this.defaultStart, this.defaultIncrement);
    }

    public Counter getCounter(long start) {
        return new Counter(start, this.defaultIncrement);
    }

    public Counter getCounter(long start, long increment) {
        return new Counter(start, increment);
    }
}