/*
 * MdpsNewsParser.java
 *
 * Created on 31.08.2006 09:10:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;

import com.netflix.servo.annotations.Monitor;

import de.marketmaker.istar.feed.FeedRecord;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_NDB_FLAGS;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_SOURCE_ID;

/**
 * Parser for mdps news messages.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsNewsParser extends MdpsParser {

    public MdpsNewsParser() {
        setTrimStrings(false);
    }

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

            switch (fieldId) {
                case ID_SOURCE_ID:
                    buffer.get(); // ignore field
                    break;
                case ID_NDB_FLAGS:
                    this.parsedRecord.setFlags(MdpsFeedUtils.getUnsignedShort(buffer));
                    break;
                default:
                    parseField(fieldId, buffer);
            }
        }

        setToaAndDoa();
        build();
    }        
}
