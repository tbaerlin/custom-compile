/*
 * TickDataMerger.java
 *
 * Created on 28.09.12 14:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

/**
 * @author zzhao
 */
public class EntryMergerTickJoin extends EntryMergerJoin<MutableTickEntry> {

    public EntryMergerTickJoin(int pivotDays) {
        super(pivotDays, MutableTickEntry.class);
    }
}
