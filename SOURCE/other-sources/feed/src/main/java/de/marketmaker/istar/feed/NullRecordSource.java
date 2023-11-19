/*
 * NullRecordSource.java
 *
 * Created on 14.12.2004 16:31:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullRecordSource implements RecordSource {
    public final static RecordSource INSTANCE = new NullRecordSource();
    
    private NullRecordSource() {
    }

    public FeedRecord getFeedRecord() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
        return null;
    }
}
