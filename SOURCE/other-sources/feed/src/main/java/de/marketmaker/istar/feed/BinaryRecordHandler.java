/*
 * BinaryRecordHandler.java
 *
 * Created on 07.02.2005 15:46:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.nio.ByteBuffer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface BinaryRecordHandler {
    /**
     * Implemented by a handler of a binary record.
     * @param pr record as it has been parsed
     * @param data FeedData object whose update is to be processed
     * @param buffer the data to be processed, starting with an int containing the length
     */
    void process(ParsedRecord pr, FeedData data, ByteBuffer buffer);
}
