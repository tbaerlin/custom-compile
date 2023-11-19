/*
 * MdpsDelayServer.java
 *
 * Created on 11.06.2010 13:09:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_LENGTH;

/**
 * Processes ByteBuffers that are supposed to contain mdps feed records and adds them to an
 * internal buffer before forwarding them to a delegate BufferWriter. For all records added,
 * the prefix '/D' is prepended to the key of the message (i.e., 710000.ETR,E becomes /D710000.ETR,E)
 * iff {@link MdpsWriter#usePrefix} is true.
 * <p>
 * The internal buffer is flushed at regular intervals, so that records are not kept for too long
 * even if no new records arrive. If the buffer is empty on flush, a heartbeat record will be sent.
 * @author oflege
 */
public class MdpsDelayWriter extends MdpsWriter implements BufferWriter {
    public MdpsDelayWriter() {
        super(ByteUtil.toBytes("/DDELAY.VWD,E"));
    }

    public void setUseDelayPrefix(boolean useDelayPrefix) {
        setUsePrefix(useDelayPrefix);
    }

    @Override
    public final void write(ByteBuffer bb) throws IOException {
        synchronized (this.bufferMutex) {
            append(bb);
        }
        this.numMessagesSent.incrementAndGet();
    }

    private void append(ByteBuffer buffer) {
        if (this.usePrefix) {
            appendWithPrefix(buffer);
        }
        else {
            if (this.bb.remaining() < buffer.remaining()) {
                sendBuffer();
            }
            this.bb.put(buffer);
        }
    }

    private void appendWithPrefix(ByteBuffer buffer) {
        final int p = buffer.position();

        final int msgLen = MdpsFeedUtils.getUnsignedShort(buffer);
        if (this.bb.remaining() < (msgLen + 2)) {
            sendBuffer();
        }

        // at relative position: |0          |6      |8      |9
        //      buffer contains: |mdps-header|key-fid|key-len|key[0]

        this.bb.putShort((short) (msgLen + 2));
        this.bb.put(getDelayedMdpsMessageType(buffer.get()));

        if (this.protocolVersion == 1) {
            this.bb.put(buffer.get());
            this.bb.putShort((short) (MdpsFeedUtils.getUnsignedShort(buffer) + 2)); // body length
            this.bb.putShort((short) MdpsMessageConstants.MDPS_KEY_FID);
        }
        else {
            // copy 3 remaining header bytes and short MDPS_KEY_FID
            buffer.limit(buffer.position() + 5);
            this.bb.put(buffer);
        }

        buffer.position(p + MdpsMessageConstants.HEADER_LENGTH + 2);
        buffer.limit(p + msgLen);

        final int keyLen = MdpsFeedUtils.getUnsignedByte(buffer);
        this.bb.put((byte) (keyLen + 2));
        this.bb.put(MdpsMessageConstants.MDPS_DELAYED_KEY_PREFIX1);
        this.bb.put(MdpsMessageConstants.MDPS_DELAYED_KEY_PREFIX2);

        // at relative position: |0          |6      |8      |9|10|11
        //          bb contains: |mdps-header|key-fid|key-len|/|D |key[0]

        this.bb.put(buffer);
    }

    private byte getDelayedMdpsMessageType(byte mdpsMessageType) {
        switch (mdpsMessageType) {
            case MdpsMessageTypes.STRANGLE_DELETE:
                return (byte) MdpsMessageTypes.DELAY_DELETE;
            case MdpsMessageTypes.STRANGLE_UPDATE:
                return (byte) MdpsMessageTypes.DELAY_UPDATE;
            default:
                return mdpsMessageType;
        }
    }

    protected void createHeartbeat() {
        this.bb.position(HEADER_LENGTH);
        this.bb.putShort((short) MdpsMessageConstants.MDPS_KEY_FID);
        this.bb.put((byte) (this.heartbeatKey.length));
        this.bb.put(this.heartbeatKey);

        final DateTime dt = new DateTime();
        this.bb.putShort((short) VwdFieldDescription.ADF_Datum.id());
        this.bb.putInt(MdpsFeedUtils.encodeDate(dt));

        this.bb.putShort((short) VwdFieldDescription.ADF_Zeit.id());
        this.bb.putInt(MdpsFeedUtils.encodeTime(dt));

        final int msgLength = this.bb.position();

        this.bb.putShort(0, (short) msgLength); // mdps length
        this.bb.put(2, (byte) MdpsMessageTypes.DELAY_UPDATE); // msg type
        if (this.protocolVersion == 1) {
            this.bb.putShort(4, (short) (msgLength - HEADER_LENGTH)); // body length
        }
    }

}
