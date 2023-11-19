/*
 * IqsMessageBuilder.java
 *
 * Created on 23.09.13 11:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import net.jcip.annotations.NotThreadSafe;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.mdps.util.FieldFormatter;

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;
import static de.marketmaker.istar.domain.data.SnapRecord.US_ASCII;
import static de.marketmaker.istar.mdps.iqs.Constants.*;

/**
 * Helper class to build iqs messages of message fragments.
 * @author oflege
 */
@NotThreadSafe
class IqsMessageBuilder {

    static final byte[] HEX = "0123456789abcdef".getBytes(US_ASCII);

    private final ByteBuffer bb;

    private final FieldFormatter formatter = new FieldFormatter(false); // not used for times with millis

    private final byte[] intBuffer = new byte[12];

    private final byte[] dateBuffer = new byte[10];

    private final byte[] timeBuffer = new byte[8];

    IqsMessageBuilder() {
        this(1 << 16);
    }

    IqsMessageBuilder(int i) {
        this.bb = ByteBuffer.allocate(i);
    }

    IqsMessageBuilder(ByteBuffer bb) {
        this.bb = bb;
    }

    /**
     * Prepares a message of the given type and adds fields copied from msg
     * @param msgType type of new message
     * @param msg copy fields from this message
     * @param copyFields ids of fields to be copied in addition to FID_REQUEST_ID which will
     * always be copied.
     * @return this
     */
    IqsMessageBuilder prepare(int msgType, IqsRequest msg, int... copyFields) {
        prepare(msgType).header(FID_REQUEST_ID, msg);
        for (int copyField : copyFields) {
            header(copyField, msg);
        }
        return this;
    }

    IqsMessageBuilder clear() {
        this.bb.clear();
        return this;
    }

    IqsMessageBuilder prepare(int msgType) {
        bb.clear();
        bb.put(Constants.STX);
        return header(Constants.FID_MESSAGE_TYPE, msgType);
    }

    IqsMessageBuilder timeHeader(int fid, int value) {
        return putFid(fid).putTime(value);
    }

    private IqsMessageBuilder putTime(int value) {
        this.formatter.renderSecondOfDay(value, timeBuffer);
        this.bb.put(timeBuffer);
        return this;
    }

    IqsMessageBuilder dateHeader(int fid, int value) {
        return putFid(fid).putDate(value);
    }

    private IqsMessageBuilder putDate(int value) {
        this.formatter.renderDate(value, this.dateBuffer);
        this.bb.put(this.dateBuffer);
        return this;
    }

    IqsMessageBuilder header(int fid, int value) {
        return putFid(fid).putValue(value);
    }

    IqsMessageBuilder header(int fid, String value) {
        if (value != null) {
            return putFid(fid).putValue(value.getBytes(CP_1252));
        }
        return this;
    }

    IqsMessageBuilder header(int fid, IqsRequest msg) {
        return header(fid, msg.getString(fid));
    }

    IqsMessageBuilder header(int fid, ByteString value) {
        if (value != null) {
            putFid(fid);
            return append(value);
        }
        return this;
    }

    IqsMessageBuilder header(int fid, byte value, byte value2) {
        return putFid(fid).putValue(value).putValue(value2);
    }

    IqsMessageBuilder header(int fid, byte value) {
        return putFid(fid).putValue(value);
    }

    IqsMessageBuilder header(int fid, byte[] value) {
        return putFid(fid).putValue(value);
    }

    IqsMessageBuilder header(int fid) {
        return putFid(fid);
    }

    IqsMessageBuilder header(int fid, byte[] value, int offset, int length) {
        return putFid(fid).putValue(value, offset, length);
    }

    IqsMessageBuilder permission(byte[] value, byte[] qualitySuffix) {
        putFid(Constants.FID_PERMISSION);
        return permissionValue(value, qualitySuffix);
    }

    IqsMessageBuilder permissionValue(byte[] value, byte[] qualitySuffix) {
        putValue(value);
        if (qualitySuffix != null) {
            putValue(qualitySuffix);
        }
        return this;
    }

    IqsMessageBuilder append(ByteString bs) {
        bs.writeTo(this.bb, ByteString.LENGTH_ENCODING_NONE);
        return this;
    }

    IqsMessageBuilder hex(byte[] msg) {
        for (int i = 0; i < msg.length && bb.remaining() > 4; i++) {
            int val = msg[i] & 0xFF;
            final int hi = val >> 4;
            final int lo = val & 0xF;
            bb.put(HEX[hi]).put(HEX[lo]).put((byte) ' ');
        }
        return this;
    }

    IqsMessageBuilder serviceTable(Collection<FeedMarket> markets, byte serviceId, String qualityStr) {
        String serviceName = (serviceId == Constants.SERVICE_ID_PRICE)
            ? SERVICE_NAME_PRICE : SERVICE_NAME_EXCHANGE_SUBSCRIBE;
        String serviceType = (serviceId == Constants.SERVICE_ID_PRICE)
                ? SERVICE_TYPE_PRICE : SERVICE_TYPE_EXCHANGE_SUBSCRIBE;
        String serviceIdStr = Character.toString((char) serviceId);

        putFid(Constants.FID_SERVICE_TABLE);
        bb.put("<ServiceStatusTable servicecount=\"1\">".getBytes(CP_1252));
        bb.put(String.format("<ServiceStatus name=\"%s\" id=\"%s\" type=\"%s\" status=\"A\" quality=\"%s\" message=\"%s is available\">",
                serviceName, serviceIdStr, serviceType, qualityStr, serviceName).getBytes(CP_1252));

        bb.put("<Exchanges>".getBytes(CP_1252));

        final byte[] marketPrefix = "<Exchange id=\"".getBytes(CP_1252);
        final byte[] marketSuffix = ("\" quality=\"" + qualityStr + "\" />").getBytes(CP_1252);
        for (FeedMarket market : markets) {
            bb.put(marketPrefix).put(market.getName().getBytes()).put(marketSuffix);
        }

        bb.put("</Exchanges></ServiceStatus></ServiceStatusTable>".getBytes(CP_1252));
        return this;
    }

    IqsMessageBuilder putValue(byte[] value) {
        bb.put(value);
        return this;
    }

    IqsMessageBuilder putValue(byte[] value, int offset, int length) {
        bb.put(value, offset, length);
        return this;
    }

    IqsMessageBuilder putValue(byte value) {
        bb.put(value);
        return this;
    }

    IqsMessageBuilder putValue(int value) {
        return putItoa(value);
    }

    private IqsMessageBuilder putFid(int fid) {
        bb.put(Constants.RS);
        putItoa(fid);
        bb.put(Constants.FS);
        return this;
    }

    private IqsMessageBuilder putItoa(int i) {
        if ((i & ~0xFFFF) == 0) {
            FieldFormatter.appendUnsignedShort(bb, i);
        }
        else {
            final int from = this.formatter.renderInt(i, this.intBuffer);
            bb.put(this.intBuffer, from, this.intBuffer.length - from);
        }
        return this;
    }

    byte[] asBytes() {
        return Arrays.copyOf(this.bb.array(), this.bb.position());
    }

    /**
     * adds the final message byte to the message and returns a buffer that contains the complete
     * message in its remaining bytes. As soon as another method of the object is called, the
     * contents of the returned buffer will be modified, so the message has to be processed completely
     * before or it has to be copied to another buffer for later use.
     */
    ByteBuffer build() {
        bb.put(Constants.ETX);
        bb.flip();
        return bb;
    }

    IqsMessageBuilder openEnvelope() {
        bb.put(Constants.SI);
        return this;
    }

    IqsMessageBuilder body() {
        bb.put(Constants.GS);
        return this;
    }

    IqsMessageBuilder closeEnvelope() {
        bb.put(Constants.SO);
        return this;
    }
}
