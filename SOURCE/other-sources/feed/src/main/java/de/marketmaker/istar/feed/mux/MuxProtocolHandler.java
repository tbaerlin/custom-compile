/*
 * MuxProtocolHandler.java
 *
 * Created on 08.10.12 14:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.marketmaker.istar.feed.connect.FeedStats;

/**
 * @author oflege
 */
public interface MuxProtocolHandler extends FeedStats.MessageSink {

    ByteOrder getByteOrder();

    int getEndOfLastCompleteRecord(ByteBuffer in) throws IOException;

    int getLengthOfRecordAt(ByteBuffer in, int position) throws IOException;
}
