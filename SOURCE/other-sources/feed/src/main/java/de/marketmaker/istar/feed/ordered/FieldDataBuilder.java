/*
 * FieldDataBuilder.java
 *
 * Created on 12.10.12 11:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Base for subclasses that need to assemble field data
 * @author oflege
 */
public class FieldDataBuilder {
    static final int TYPE_SHIFT = 5;

    private static final int TYPE_INT_BITS = FieldData.TYPE_INT << TYPE_SHIFT;

    private static final int TYPE_TIME_BITS = FieldData.TYPE_TIME << TYPE_SHIFT;

    private static final int TYPE_PRICE_BITS = FieldData.TYPE_PRICE << TYPE_SHIFT;

    private static final int TYPE_STRING_BITS = FieldData.TYPE_STRING << TYPE_SHIFT;

    protected final ByteBuffer bb;

    protected int lastOrder;

    public FieldDataBuilder(ByteBuffer bb) {
        this.bb = bb;
    }

    public FieldDataBuilder(int bufferSize) {
        this.bb = BufferFieldData.asBuffer(new byte[bufferSize]);
    }

    public void reset() {
        this.lastOrder = 0;
        this.bb.clear();
    }

    public final void putIntFid(int order) {
        putFid(order, TYPE_INT_BITS);
    }

    public final void putTimeFid(int order) {
        putFid(order, TYPE_TIME_BITS);
    }

    public final void putPriceFid(int order) {
        putFid(order, TYPE_PRICE_BITS);
    }

    public final void putStringFid(int order) {
        putFid(order, TYPE_STRING_BITS);
    }

    private void putFid(int order, int type) {
        final int diff = order - this.lastOrder;
        if (diff <= 0) {
            throw new IllegalStateException("Wrong field order: " + order + "<=" + this.lastOrder
                + ". See: https://vauwede.atlassian.net/wiki/spaces/CP/pages/112099374/Wrong+Field+Order");
        }
        if (diff <= 0x1F) {
            this.bb.put((byte) (0x80 | type | diff));
        }
        else {
            this.bb.put((byte) (type | (diff >> 8)));
            this.bb.put((byte) diff);
        }
        this.lastOrder = order;
    }


    protected final void putStopBitEncoded(int i) {
        if (i > 0x7F) {
            this.bb.put((byte) (i >> 7));
        }
        this.bb.put((byte) ((i & 0x7f) | 0x80));
    }

    public final void putInt(int value) {
        this.bb.putInt(value);
    }

    public final void putPrice(long value) {
        this.bb.putInt((int) value);
        this.bb.put((byte) (value >> 32));
    }

    public final void putString(byte[] value) {
        putStopBitEncoded(value.length);
        this.bb.put(value);
    }

    public final void addFieldToBuffer(BufferFieldData fd) {
        final int oid = fd.getId();
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                putIntFid(oid);
                break;
            case FieldData.TYPE_PRICE:
                putPriceFid(oid);
                break;
            case FieldData.TYPE_STRING:
                putStringFid(oid);
                putStopBitEncoded(fd.getLength());
                break;
            case FieldData.TYPE_TIME:
                putTimeFid(oid);
                break;
            default:
                throw new IllegalStateException();
        }
        fd.putCurrent(this.bb);
    }

    public final byte[] asArray() {
        return Arrays.copyOfRange(this.bb.array(), 0, this.bb.position());
    }

    public void getFrom0(ByteBuffer dst) {
        dst.put(this.bb.array(), 0, this.bb.position());
    }

    public int length() {
        return this.bb.position();
    }
}
