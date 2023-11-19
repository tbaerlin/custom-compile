/*
 * RecordSource2BufferWriter.java
 *
 * Created on 09.02.2005 12:06:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.nio.ByteBuffer;
import java.io.IOException;

import de.marketmaker.istar.feed.BinaryRecordHandler;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BinaryRecordHandler2BufferWriter implements BinaryRecordHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BufferWriter bufferWriter;

    public BinaryRecordHandler2BufferWriter() {
    }

    public void process(ParsedRecord pr, FeedData data, ByteBuffer buffer) {
        try {
            this.bufferWriter.write(buffer);
        }
        catch (IOException e) {
            this.logger.error("<process> failed",e);
        }
    }

    public void setBufferWriter(BufferWriter bufferWriter) {
        this.bufferWriter = bufferWriter;
    }
}
