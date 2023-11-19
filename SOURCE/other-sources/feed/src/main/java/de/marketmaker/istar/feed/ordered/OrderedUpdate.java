/*
 * OrderedUpdate.java
 *
 * Created on 30.08.12 12:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.nio.ByteBuffer;

import io.netty.util.AsciiString;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
public class OrderedUpdate {
    private final VendorkeyFilter filter;

    private final BufferFieldData update = new BufferFieldData();

    private final int hashCodeMask;

    private final int acceptableHashCode;

    private byte mdpsMsgType;

    private byte msgType;

    private int timestamp;

    private int keyType;

    private int flags;

    private int keyFrom;

    private int keyLength;

    /**
     * If multiple components need to access the delay time provided by an
     */
    private int delayInSeconds = 0;

    public OrderedUpdate() {
        this(null);
    }

    public OrderedUpdate(VendorkeyFilter filter) {
        this(filter, 0, 0);
    }

    public OrderedUpdate(VendorkeyFilter filter, int hashCodeMask, int acceptableHashCode) {
        this.filter = filter;
        this.hashCodeMask = hashCodeMask;
        this.acceptableHashCode = acceptableHashCode;
    }

    /**
     * @param buffer incoming feed data
     * @return true if the buffer is accepted, false if the content is matched by one of the filters
     */
    public boolean reset(ByteBuffer buffer) {
        if ((buffer.get() & this.hashCodeMask) != this.acceptableHashCode) {
            return false;
        }
        this.keyLength = buffer.get() & 0xFF;  // max size of a vwdKey is 255
        this.keyFrom = buffer.position();
        buffer.position(this.keyFrom + this.keyLength);
        if (this.filter != null
                && !this.filter.test(buffer.array(), this.keyFrom, buffer.position())) {
            return false;
        }
        // copy the record meta data
        this.mdpsMsgType = buffer.get();
        this.msgType = buffer.get();
        this.keyType = buffer.getShort();
        this.flags = buffer.getShort() & 0xFFFF;
        this.timestamp = buffer.getInt();
        // the field data
        this.update.reset(buffer);
        return true;
    }

    VendorkeyVwd getVendorkey() {
        return VendorkeyVwd.getInstance(getVwdcode(), this.keyType);
    }

    /**
     * @return ByteString with a copy of the vwdCode section of the update instance
     */
    public ByteString getVwdcode() {
        return new ByteString(this.update.getArray(), this.keyFrom, this.keyLength);
    }

    /**
     * @return AsciiString using the update instance as byte array
     * the returned AsciiString shares a byte array with this update instance
     */
    public AsciiString getVwdCode() {
        return new AsciiString(this.update.getArray(), this.keyFrom, this.keyLength, false);
    }

    public int size() {
        return this.update.size();
    }

    public int getVwdKeyType() {
        return this.keyType & 0xFF;
    }

    public int getMdpsKeyType() {
        return (this.keyType >> 8) & 0xFF;
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public int getDate() {
        return DateTimeProvider.Timestamp.decodeDate(this.timestamp);
    }

    public int getTime() {
        return DateTimeProvider.Timestamp.decodeTime(this.timestamp);
    }

    public boolean isDelete() {
        return VwdFeedConstants.isDelete(this.msgType);
    }

    public boolean isRecap() {
        return VwdFeedConstants.isRecap(this.msgType);
    }

    public byte getMsgType() {
        return this.msgType;
    }

    public byte getMdpsMsgType() {
        return this.mdpsMsgType;
    }

    public BufferFieldData getFieldData() {
        return this.update.rewind();
    }

    public boolean isWithCloseDateYesterday() {
        return hasFlag(FeedUpdateFlags.FLAG_WITH_CLOSE_DATE_YESTERDAY);
    }

    public boolean hasFlag(int f) {
        return (this.flags & f) != 0;
    }

    public int getFlags() {
        return this.flags;
    }

    /**
     * flags contains sourceId if
     * {@link de.marketmaker.istar.feed.ordered.OrderedFeedBuilder#setWithSourceId(boolean)}
     * had been set to <tt>true</tt>; this method exists for improved readability
     */
    public int getSourceId() {
        return this.flags;
    }

    public ByteBuffer asMessageWithLength() {
        return (ByteBuffer) this.update.asBuffer().position(this.keyFrom - 4);
    }

    public void putHeader(ByteBuffer bb) {
        final int offset = this.keyFrom - 2;
        bb.put(this.update.getArray(), offset, this.update.getStart() - offset);
    }

    public void putFields(ByteBuffer bb) {
        this.update.putFields(bb);
    }

    public int getDelayInSeconds() {
        return this.delayInSeconds;
    }

    public void setDelayInSeconds(int delayInSeconds) {
        this.delayInSeconds = delayInSeconds;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder(1 << 8);
        sb.append(DateTimeProvider.Timestamp.toDateTime(getTimestamp()))
                .append(" ").append((int) getMdpsMsgType())
                .append(" ").append((char) getMsgType())
                .append(" ");
        sb.append(getVwdKeyType()).append(".").append(getVwdcode()).append("(,")
                .append(MdpsTypeMappings.getMdpsKeyTypeById(getMdpsKeyType())).append("), fields=");
        BufferFieldData fd = getFieldData();
        String sep = "[";
        for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
            sb.append(sep).append(VwdFieldOrder.getField(oid))
                    .append('=').append(getFieldValue(fd));
            sep = ", ";
        }
        sb.append("]");
        return sb.toString();
    }

    private Object getFieldValue(BufferFieldData fd) {
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                if (VwdFieldOrder.getField(fd.getId()).type() == VwdFieldDescription.Type.UINT) {
                    return fd.getUnsignedInt();
                } else {
                    return fd.getInt();
                }
            case FieldData.TYPE_TIME:
                return MdpsFeedUtils.decodeLocalTime(fd.getInt());
            case FieldData.TYPE_PRICE:
                return OrderedSnapRecord.getDecimal(fd.getInt(), fd.getByte(), false);
            case FieldData.TYPE_STRING:
                return fd.getLength() == 0 ? "" : OrderedSnapRecord.toString(fd.getBytes());
            default:
                throw new IllegalStateException("Unknown type: " + fd.getType());
        }
    }
}
