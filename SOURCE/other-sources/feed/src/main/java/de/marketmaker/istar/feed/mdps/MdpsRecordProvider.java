/*
 * MdpsRecordProvider.java
 *
 * Created on 25.08.2006 10:57:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Constants;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.feed.mux.MuxOutput;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.getUnsignedByte;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.getUnsignedShort;
import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.*;

/**
 * Receives mdps feed data by means of its BufferWriter interface, parses the messages header
 * fields to be able to split the data into individual records (but it does not parse the
 * message body), puts the data in FeedRecord objects, and forwards those to an attached
 * {@link MdpsFeedParser} immediately<p>
 * The FeedRecords created contain the complete mdps message, their
 * {@link de.marketmaker.istar.feed.FeedRecord#getAsByteBuffer()} returns a Buffer wrapped around
 * the header and the body (not the (optional) trailer!), and
 * {@link de.marketmaker.istar.feed.FeedRecord#getData()} returns a byte array with the complete
 * mdps message.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsRecordProvider implements BufferWriter, MuxOutput, InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** target for received messages */
    private Consumer<FeedRecord> consumer;

    private BitSet filter = null;

    private final FeedRecord record = new FeedRecord(new byte[0], 0, 0);

    private int protocolVersion = 1;

    /**
     * Only messages whose message type is given in the names array will be enqueued; If this
     * method is not called, all messages will be enqueued. Valid names must match constants
     * in {@link MdpsMessageTypes}.
     * @param names message type names of messages that should be processed.
     */
    public void setMessageTypeNames(String[] names) {
        final Constants constants = new Constants(MdpsMessageTypes.class);

        this.filter = new BitSet();
        for (String name : names) {
            this.filter.set(constants.asNumber(name).intValue());
        }
    }

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        this.logger.info("<setProtocolVersion> = " + this.protocolVersion);
    }

    @Required
    public void setConsumer(Consumer<FeedRecord> consumer) {
        this.consumer = consumer;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.consumer == null) {
            throw new IllegalArgumentException("parser is null");
        }
    }

    @Override
    public boolean isAppendOnlyCompleteRecords() {
        return false;
    }

    @Override
    public void append(ByteBuffer in) throws IOException {
        write(in);
    }

    public void write(ByteBuffer bb) throws IOException {
        this.record.withOrder(bb.order());
        while (bb.remaining() > HEADER_LENGTH) {
            final int p = bb.position();

            final int msgLen = getUnsignedShort(bb, p);
            if (bb.remaining() < msgLen) {
                return; // leave incomplete record in buffer
            }

            final int messageType = getUnsignedByte(bb, p + HEADER_MESSAGE_TYPE_OFFSET);
            if (isMessageToBeProcessed(messageType)) {
                // reset record so that its getAsByteBuffer method returns a Buffer
                // that wraps around |header|body|
                this.record.reset(bb.array(), p, getLength(bb, p, msgLen));
                this.consumer.accept(this.record);
            }

            bb.position(p + msgLen);
        }
    }

    /**
     * @return length of message in bb starting at p so that (p + length) points to the end of
     * the message body. In mdps V1, it was possible to have additional bytes after the message
     * body, for whatever purpose; In mdps V3, the length is always the same as the length of the
     * entire message.
     */
    private int getLength(ByteBuffer bb, int p, int msgLen) {
        return (this.protocolVersion == 1)
                ? (HEADER_LENGTH + getUnsignedShort(bb, p + HEADER_BODY_LENGTH_OFFSET))
                : msgLen;
    }

    private boolean isMessageToBeProcessed(int messageType) {
        return this.filter == null || this.filter.get(messageType);
    }
}
