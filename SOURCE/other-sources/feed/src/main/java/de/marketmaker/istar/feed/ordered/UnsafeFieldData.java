/*
 * UnsafeFieldData.java
 *
 * Created on 11.07.12 10:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.addZeroBit;

/**
 * Maximum performance version; stores data in off heap allocated memory, that data has to be
 * deallocated by the user, as it is not in GC scope. About 20% faster than {@link BufferFieldData}.
 * @author oflege
 */
final class UnsafeFieldData implements FieldData {
    private static final Unsafe UNSAFE = getUnsafe();

    private static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static final int BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    private long address;

    private long limit;

    private long position;

    private int fieldOffset;

    private int id;

    private int type;

    private int length;

    private static int align(int v) {
//        return v;
        return (v + 0x10) & 0x8FFFFFF0;
    }

    public static byte[] getAsByteArray(long address, int size) {
        final byte[] result = new byte[size];
        UNSAFE.copyMemory(null, address, result, BASE_OFFSET, result.length);
        return result;
    }

    public static long storeOffHeap(byte[] data, long oldAddress, int oldSize) {
        final long result;
        if (oldAddress == 0L) {
            result = UNSAFE.allocateMemory(align(data.length));
        }
        else {
            final int requiredSize = align(data.length);
            if (requiredSize <= align(oldSize)) {
                result = oldAddress;
            }
            else {
                result = UNSAFE.reallocateMemory(oldAddress, requiredSize);
            }
        }
        UNSAFE.copyMemory(data, BASE_OFFSET, null, result, data.length);
        return result;
    }

    public static void freeMemory(long address) {
        UNSAFE.freeMemory(address);
    }

    public void dispose() {
        if (this.address > 0) {
            freeMemory(this.address);
            this.address = 0;
            this.limit = 0;
            this.position = 0;
            this.id = 0;
        }
    }

    public void reset(byte[] data) {
        reset(storeOffHeap(data, this.address, (int) (this.limit - this.address)), data.length);
    }

    public UnsafeFieldData reset(long address, int size) {
        this.address = address;
        this.limit = address + size;
        this.position = address;
        this.id = 0;
        return this;
    }

    public UnsafeFieldData rewind() {
        this.position = this.address;
        this.id = 0;
        return this;
    }

    public boolean hasNext() {
        return this.position < this.limit;
    }

    public int readNext() {
        this.fieldOffset = (int) (this.position - this.address);
        if (!hasNext()) {
            this.id = 0;
            return 0;
        }
        int i = getByte();
        this.type = (i & 0x60) >> FieldDataBuilder.TYPE_SHIFT;
        int diff = i & 0x1F;
        if (i >= 0) {
            diff = (diff << 8) + (getByte() & 0xFF);
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
        final int first = getByte();
        if (first < 0) {
            return 0x7F & first;
        }
        return (first << 7) | (0x7F & getByte());
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public byte[] getBytes() {
        final byte[] result = getAsByteArray(this.position, this.length);
        this.position += this.length;
        return result;
    }

    private void putInt(int anInt) {
        UNSAFE.putInt(this.position, anInt);
        this.position += 4;
    }

    private void putByte(byte aByte) {
        UNSAFE.putByte(this.position, aByte);
        this.position++;
    }

    @Override
    public void putStringBytes(byte[] aByte, int offset, int sLength) {
        UNSAFE.copyMemory(aByte, BASE_OFFSET + offset, null, position, sLength);
        if (sLength < this.length) {
            UNSAFE.putByte(this.position + sLength, (byte) 0);
        }
        this.position += this.length;
    }

    public void applyStringUpdateTo(FieldData existing) {
        // makes no sense to use this class for updates
        throw new UnsupportedOperationException();
/*
        // but if you insist, it would look like this:
        final byte[] s = getStringBytes();
        existing.putStringBytes(s, 0, s.length);
*/
    }

    @Override
    public void skipCurrent() {
        this.position += this.length;
    }

    @Override
    public byte[] getAsByteArray() {
        return getAsByteArray(this.address, (int) (this.limit - this.address));
    }

    @Override
    public byte[] getAsByteArrayBeforeCurrent() {
        return getAsByteArray(this.address, this.fieldOffset);
    }

    public byte getByte() {
        return UNSAFE.getByte(this.position++);
    }

    public int getInt() {
        final int result = UNSAFE.getInt(this.position);
        this.position += 4;
        return result;
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
                if (!mergeStringFrom(update)) {
                    // update has longer string, cannot merge in place
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void mergePriceFrom(FieldData update) {
        final int base = update.getInt();
        if (base == 0) {
            this.position += 4; // skip base
            putByte((byte) addZeroBit(UNSAFE.getByte(this.position)));
            update.getByte(); // ignored
        }
        else {
            putInt(base);
            putByte(update.getByte());
        }
    }

    private boolean mergeStringFrom(FieldData update) {
        if (update.getLength() > this.length) {
            return false;
        }
        update.applyStringUpdateTo(this);
        return true;
    }
}
