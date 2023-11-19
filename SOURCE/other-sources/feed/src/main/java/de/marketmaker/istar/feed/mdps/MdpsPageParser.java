/*
 * MdpsPageParser.java
 *
 * Created on 07.03.2008 08:29:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;

import com.netflix.servo.annotations.Monitor;

import de.marketmaker.istar.feed.FeedRecord;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsPageParser extends MdpsParser {

    @Monitor(type = COUNTER)
    public final long numRecordsParsed() {
        return this.numRecordsParsed.get();
    }

    public void parse(FeedRecord feedRecord) {
        final ByteBuffer buffer = feedRecord.getAsByteBuffer();
        final byte mdpsMessageType =
                buffer.get(feedRecord.getOffset() + MdpsMessageConstants.HEADER_MESSAGE_TYPE_OFFSET);

        buffer.position(feedRecord.getOffset() + MdpsMessageConstants.HEADER_LENGTH); // skip header

        final byte messageType = MdpsFeedUtils.toXfeedMessageType(mdpsMessageType);
        if (!isAnyBuilderInterested(messageType)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<parse> no builder interested in type " + messageType
                        + " (" + mdpsMessageType + ")");
            }
            return;
        }

        this.parsedRecord.reset(feedRecord);
        this.parsedRecord.setMessageType(messageType);

        while (buffer.hasRemaining()) {
            final int fieldId = buffer.getShort();
            parseField(fieldId, buffer);
        }

        setToaAndDoa();
        build();
    }
}