/*
 * MultiFeedRecord.java
 *
 * Created on 13.07.2008 11:15:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.RecordSource;

/**
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class MultiFeedRecord implements RecordSource {
    public static final MultiFeedRecord EMPTY = new MultiFeedRecord(new FeedRecord[0], 0);

    private final int length;
    private int n = 0;
    private final FeedRecord[] records;

    MultiFeedRecord(FeedRecord[] records, int length) {
        this.records = records;
        this.length = length;
    }

    public FeedRecord getFeedRecord() {
        return this.n < this.length ? this.records[this.n++] : null;
    }

    public int getNumRecords() {
        return this.length;
    }
}
