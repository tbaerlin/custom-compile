/*
 * MdpsRecordFactory.java
 *
 * Created on 21.05.2008 15:25:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.PriceCoder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsRecordFactory {
    private final ByteBuffer bb = ByteBuffer.allocate(1 << 16);

    public static MdpsRecordFactory forProtocolVersion(int v) {
        if (v != 1 && v != 3) {
            throw new IllegalArgumentException("unsupported version " + v);
        }
        return new MdpsRecordFactory(MdpsFeedUtils.getByteOrder(v));
    }


    private MdpsRecordFactory(ByteOrder order) {
        bb.order(order);
    }

    public byte[] getBytes() {
        this.bb.putShort(MdpsMessageConstants.HEADER_MESSAGE_LENGTH_OFFSET, (short) bb.position());
        if (bb.order() == ByteOrder.BIG_ENDIAN) { // V1
            this.bb.putShort(MdpsMessageConstants.HEADER_BODY_LENGTH_OFFSET,
                    (short)(bb.position() - MdpsMessageConstants.HEADER_LENGTH));
        }
        return Arrays.copyOf(bb.array(), bb.position());
    }

    public void reset(byte messageType) {
        this.bb.clear();
        this.bb.put(MdpsMessageConstants.HEADER_MESSAGE_TYPE_OFFSET, messageType);
        this.bb.position(MdpsMessageConstants.HEADER_LENGTH);
    }

    public void addKey(String key) {
        assert this.bb.position() == MdpsMessageConstants.HEADER_LENGTH;
        addVlshString(MdpsMessageConstants.MDPS_KEY_FID, key);
    }

    public void addVlshString(int fid, String value) {
        this.bb.putShort((short) fid);
        ByteString bs = new ByteString(value);
        bs.writeTo(this.bb, ByteString.LENGTH_ENCODING_BYTE);
    }

    public void addTime(int fid, int value) {
        this.bb.putShort((short) fid);
        this.bb.putInt(MdpsFeedUtils.encodeTime(value));
    }

    public void addDate(int fid, int yyyymmdd) {
        this.bb.putShort((short) fid);
        this.bb.putShort((short) (yyyymmdd / 10000));
        this.bb.put((byte) ((yyyymmdd % 10000) / 100));
        this.bb.put((byte) (yyyymmdd % 100));
    }

    public void addPrice(int fid, long value) {
        this.bb.putShort((short) fid);
        long mantissa = PriceCoder.clearFractionFlags(value);
        int scale = -PriceCoder.getFractionDigits(value);

        while (scale < 0 && (mantissa % 10) == 0) {
            scale++;
            mantissa /= 10;
        }

        this.bb.putInt((int) mantissa);
        this.bb.put((byte ) scale);
    }

    public void addSize(int fid, long value) {
        this.bb.putShort((short) fid);
        this.bb.putInt((int) value);
    }

    public void addUShort(int fid, int value) {
        this.bb.putShort((short) fid);
        this.bb.putShort((short) value); // todo: is this correct?!
    }
}
