/*
 * FeedRecord.java
 *
 * Created on 17.11.2004 10:54:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.feed.mdps.MdpsMessageConstants;
import de.marketmaker.istar.feed.mdps.MdpsMessageTypes;
import de.marketmaker.istar.feed.mdps.MdpsParser;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;
import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_MESSAGE_TYPE_OFFSET;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Status;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Tick;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeedRecord {
    /**
     * Special record that causes a parser thread to re-synchronize with dependent components
     */
    public static final FeedRecord SYNC = new FeedRecord(null, 0, 0);

    private static final String LS = System.getProperty("line.separator");

    private static final byte[] EMPTY_DATA = new byte[0];

    private byte[] data;

    private int offset;

    private int end;

    private int index;

    private ByteOrder order = ByteOrder.BIG_ENDIAN;

    private ByteBuffer bb;

    public FeedRecord() {
        this.data = EMPTY_DATA;
        this.offset = 0;
        this.end = 0;
        this.index = 0;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public FeedRecord(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.end = offset + length;
        this.index = this.offset;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public void reset(byte[] data, int offset, int length) {
        if (data != this.data) {
            this.data = data;
            this.bb = null;
        }
        this.offset = offset;
        this.end = offset + length;
        this.index = this.offset;
    }

    public FeedRecord withOrder(ByteOrder order) {
        this.order = order;
        return this;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public byte[] getData() {
        return data;
    }

    public int getOffset() {
        return offset;
    }

    public ByteBuffer getAsByteBuffer() {
        if (this.bb == null) {
            this.bb = ByteBuffer.wrap(this.data).order(this.order);
        }
        this.bb.clear().position(this.offset).limit(this.end);
        return this.bb;
    }

    public int getLength() {
        return this.end - this.offset;
    }

    public int getIndex() {
        return index;
    }

    public int getEnd() {
        return this.end;
    }

    public int remaining() {
        return this.end - this.index;
    }

    public byte getMdpsMessageType() {
        return this.data[this.offset + HEADER_MESSAGE_TYPE_OFFSET];
    }

    public String toFullString()  {
        final int rows = (getLength() + 31) / 32;
        return toString(rows);
    }

    public String toString() {
        return toString(4);
    }

    public String toString(int rows) {
        final StringBuilder sb = new StringBuilder(400);
        sb.append("FeedRecord[offset=0x").append(Integer.toHexString(this.offset))
            .append(", index=0x").append(Integer.toHexString(this.index))
            .append(", length=").append(getLength()).append(LS);
        for (int i = 0; i < rows; i++) {
            if (i * 32 < getLength()) {
                sb.append(HexDump.toHex(this.data, this.offset + i * 32, 1,
                    Math.min(32, getLength() - i * 32)));
            }
        }
        return sb.append("]").toString();
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("[size=").append(getLength())
                    .append(";type=").append(getMdpsMessageType())
                    .append(";version=").append(this.data[this.offset + MdpsMessageConstants.HEADER_V3_VERSION_OFFSET])
                    .append(";procid=").append(this.data[this.offset + MdpsMessageConstants.HEADER_V3_TARGET_PROCESSID_OFFSET])
                    .append("] - ");
            if (getMdpsMessageType() == MdpsMessageTypes.BLOB) {
                sb.append("BLOB");
            }
            else {
                final ByteBuffer buffer = getAsByteBuffer();
                buffer.position(buffer.position() + MdpsMessageConstants.HEADER_LENGTH);

                // Deal with key separately
                final short keyFieldId = buffer.getShort();
                int mdpsKeyLength = buffer.get() & 0xFF;
                ByteString key = ByteString.readWithLengthFrom(bb, mdpsKeyLength);
                sb.append(keyFieldId).append("=").append(key);

                while (buffer.hasRemaining()) {
                    final short fieldId = buffer.getShort();
                    final VwdFieldDescription.Field f = VwdFieldDescription.getField(fieldId);
                    if (f == null) {
                        break; // TODO: really break?
                    }
                    sb.append(";").append(fieldId).append("=");

                    switch (f.mdpsType()) {
                        case SIZE:
                            sb.append(getUnsignedInt(buffer));
                            break;
                        case TIME:
                        case DATE:
                            sb.append(buffer.getInt());
                            break;
                        case PRICE:
                            sb.append(getMdpsPrice0(buffer));
                            break;
                        case USHORT:
                            sb.append(getUnsignedShort(buffer));
                            break;
                        case FLSTRING:
                            if (f == ADF_Status || f == ADF_Tick) {
                                // hack for mdps hack: 2 byte string contains UNUM2 value in little endian
                                sb.append(getUnsignedShort(buffer, ByteOrder.LITTLE_ENDIAN));
                                break;
                            }
                        case VLSHSTRING:
                        case VLLGSTRING:
                            final int length = MdpsParser.getLength(buffer, f);
                            ByteString string = ByteString.readWithLengthFrom(bb, length);
                            sb.append(string);
                            break;
                        default:
                            // ?!
                            throw new IllegalStateException("unknown type for " + f);
                    }
                }
            }
        } catch (Exception e) {
            sb.append(" -> Exception caught while creating debug string: ");
            sb.append(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString()).append("\n");
            }
        }

        return sb.toString();
    }

    public void skip(int length) {
        this.index += length;
    }
}
