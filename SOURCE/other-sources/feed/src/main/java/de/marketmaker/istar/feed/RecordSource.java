/*
 * RecordSource.java
 *
 * Created on 25.10.2004 13:22:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RecordSource {
    FeedRecord getFeedRecord() throws InterruptedException;
}
