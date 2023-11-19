/*
 * FeedBuilder.java
 *
 * Created on 25.10.2004 13:25:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FeedBuilder {
    byte[] getApplicableMessageTypes();

    void process(FeedData data, ParsedRecord pr);
}
