/*
 * BufferFieldData.java
 *
 * Created on 11.07.12 10:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import de.marketmaker.istar.common.util.ByteBufferUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.addZeroBit;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.hasZeroBit;

/**
 * FieldData implementation with a backing ByteBuffer. Idiomatic usage:
 * <pre>
 *     BufferFieldData fd = ...
 *     for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
 *         switch (fd.getType()) {
 *             // read data up until next oid
 *         }
 *     }
 * </pre>
 * <b>Important</b>: The api contains a lot of low level methods that only make sense when called
 * in the correct context (e.g., calling {@link #getLength()} returns the size of the current field,
 * value, but the current field has to be set up using {@link #readNext()}.
 * However, the code will not perform
 * any checks to ensure that a method can only be called in the correct state. Since this class
 * is all about high performance with millions of fields read per second, such checks should be
 * present in test code only.
 * @author oflege
 */
public final class BufferFieldData implements FieldData {

    private static final byte[] EMPTY = new byte[0];

    private ByteBuffer bb;

    private int type;

    private int id;

    private int length;

    private int mark;

    private int start;

    private int end;

    public static ByteBuffer asBuffer(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static ByteBuffer asBuffer(byte[] bytes, int length) {
        return ByteBuffer.wrap(bytes, 0, length).order(ByteOrder.LITTLE_ENDIAN);
    }

    public BufferFieldData() {
    }

    public BufferFieldData(ByteBuffer bb) {
        reset(bb);
    }

    public BufferFieldData(byte[] bytes) {
        this(asBuffer(bytes));
    }

    public BufferFieldData reset(byte[] bytes) {
        return reset(asBuffer(bytes));
    }

    public BufferFieldData reset(ByteBuffer bb) {
        this.bb = bb;
        this.id = 0;
        this.start = this.bb.position();
        this.end = this.bb.limit();
        return this;
    }

    public int size() {
        return this.end - this.start;
    }

    int getStart() {
        return this.start;
    }

    public void putFields(ByteBuffer dst) {
        dst.put(getArray(), this.start, size());
    }

    void putFieldsBeforeCurrent(ByteBuffer dst) {
        dst.put(getArray(), this.start, this.mark - this.start);
    }

    void putCurrent(ByteBuffer dst) {
        dst.put(getArray(), this.bb.position(), this.length);
        skipCurrent();
    }

    @Override
    public BufferFieldData rewind() {
        return rewind(0);
    }

    public BufferFieldData rewind(int offset) {
        this.bb.position(this.start + offset);
        this.id = 0;
        return this;
    }

    public boolean hasNext() {
        return this.bb.position() < this.end;
    }

    @Override
    public int readNext() {
        if (this.bb.position() >= this.end) {
            this.id = 0;
            return 0;
        }
        this.mark = bb.position();

        int i = bb.get();
        this.type = (i & 0x60) >> FieldDataBuilder.TYPE_SHIFT;
        int diff = i & 0x1F;
        if (i >= 0) {
            diff = (diff << 8) + (bb.get() & 0xFF);
        }
        this.id += diff;
        this.length = initLength();
        return this.id;
    }

    private int initLength() {
        switch (this.type) {
            case FieldData.TYPE_INT:
                // intentional fallthrough
            case FieldData.TYPE_TIME:
                return 4;
            case FieldData.TYPE_PRICE:
                return 5;
            case FieldData.TYPE_STRING:
                return getStopBitEncoded();
            default:
                throw new IllegalStateException();
        }
    }

    private int getStopBitEncoded() {
        final int first = this.bb.get();
        if (first < 0) {
            return 0x7F & first;
        }
        return (first << 7) | (0x7F & this.bb.get());
    }

    @Override
    public int getId() {
        return this.id;
    }

    public int getVwdId() {
        return VwdFieldOrder.getFieldId(this.id);
    }

    @Override
    public void skipCurrent() {
        this.bb.position(this.bb.position() + this.length);
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getInt() {
        return this.bb.getInt();
    }

    @Override
    public byte getByte() {
        return this.bb.get();
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public byte[] getBytes() {
        byte[] result = new byte[this.length];
        this.bb.get(result);
        return result;
    }

    private void putInt(int value) {
        this.bb.putInt(value);
    }

    private void putByte(byte value) {
        this.bb.put(value);
    }

    @Override
    public void putStringBytes(byte[] value, int offset, int length) {
        final int pos = this.bb.position() + this.length;
        this.bb.put(value, offset, length);
        if (length < this.length) {
            this.bb.put((byte) 0);
        }
        this.bb.position(pos);
    }

    @Override
    public byte[] getAsByteArray() {
        return copyFromStartTo(this.end);
    }

    @Override
    public byte[] getAsByteArrayBeforeCurrent() {
        return copyFromStartTo(this.mark);
    }

    private byte[] copyFromStartTo(final int limit) {
        if (this.start == limit) {
            return EMPTY;
        }
        return Arrays.copyOfRange(getArray(), this.start, limit);
    }

    byte[] getArray() {
        return this.bb.array();
    }

    public byte[] copyBufferArray() {
        return Arrays.copyOfRange(getArray(), this.bb.position(), this.bb.limit());
    }

    public ByteBuffer asBuffer() {
        return (ByteBuffer) ByteBufferUtils.duplicate(this.bb).position(this.start);
    }

    @Override
    public void applyStringUpdateTo(FieldData existing) {
        existing.putStringBytes(getArray(), this.bb.position(), this.length);
        skipCurrent();
    }

    @Override
    public boolean mergeFieldFrom(FieldData update) {
        switch (this.type) {
            case FieldData.TYPE_INT:
                // intentional fallthrough
            case FieldData.TYPE_TIME:
                putInt(update.getInt());
                break;
            case FieldData.TYPE_PRICE:
                mergePriceFrom(update);
                break;
            case FieldData.TYPE_STRING:
                return mergeStringFrom(update);
            default:
                break;
        }
        return true;
    }

    private void mergePriceFrom(FieldData update) {
        final int base = update.getInt();
        if (base == 0) {
            final int p = this.bb.position() + 4;
            this.bb.position(p);
            this.bb.put(p, (byte) addZeroBit(this.bb.get()));
            update.getByte(); // ignored
        }
        else {
            putInt(base);
            putByte(update.getByte());
        }
    }

    private boolean mergeStringFrom(FieldData update) {
        if (update.getLength() > this.length) {
            // update has longer string, cannot merge in place
            return false;
        }
        update.applyStringUpdateTo(this);
        return true;
    }

    public boolean stringEquals(byte[] bytes) {
        return stringEquals(bytes, bytes.length);
    }

    public boolean stringEquals(byte[] bytes, int len) {
        assert this.type == FieldData.TYPE_STRING;

        final byte[] data = getArray();
        final int p = this.bb.position();

        for (int i = 0, n = Math.min(this.length, len); i < n; i++) {
            if (data[p + i] != bytes[i]) {
                return false;
            }
            if (bytes[i] == 0) {
                return true;
            }
        }

        return (this.length < len)
                ? bytes[this.length] == 0
                : ((this.length == len) || (data[p + len] == 0));
    }

    void eraseLastValueForZeroPrices() {
        while (readNext() != 0) {
            if (getType() == FieldData.TYPE_PRICE && hasZeroBit(this.bb.get(this.bb.position() + 4))) {
                putInt(0);
                putByte((byte) 0);
            }
            else {
                skipCurrent();
            }
        }
    }
}
