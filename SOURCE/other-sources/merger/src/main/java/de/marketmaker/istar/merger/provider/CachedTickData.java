/*
 * CachedTickData.java
 *
 * Created on 04.02.14 13:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.ordered.BufferFieldData;

/**
 * Caches tick bytes for a given day and symbol. Tick bytes are not stored on the heap as
 * we want to support large caches but don't want to increase the VM size to keep garbage collection
 * times low.
 */
class CachedTickData {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedTickData.class);

    private static final sun.misc.Unsafe UNSAFE = getUnsafe();

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

    private final String serverId;

    private long address;

    // number of bytes allocated at address
    private int allocated;

    // number of tick bytes stored at address ff
    private int length;

    /**
     * {@link de.marketmaker.istar.feed.ordered.tick.MemoryTickStore#getStorageInfo(long, int)}
     */
    private int[] storageInfo;

    static CachedTickData create(String serverId, byte[] bytes, int[] storageInfo) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new CachedTickData(serverId, bytes, storageInfo);
    }

    private CachedTickData(String serverId, byte[] bytes, int[] storageInfo) {
        this.serverId = serverId;
        this.storageInfo = storageInfo;
        putBytes(bytes);
    }

    boolean isResultFromSameServer(IntradayResponse r) {
        return this.serverId.equals(r.getServerId());
    }

    boolean isCacheComplete(IntradayResponse.Item item) {
        return Arrays.equals(this.storageInfo, item.getTickStorageInfo());
    }

    String getServerId() {
        return serverId;
    }

    int[] getStorageInfo() {
        return storageInfo;
    }

    byte[] getCachedTickBytes() {
        return getBytes(this.length);
    }

    int getNumBytesCached() {
        return this.allocated;
    }

    @Override
    public String toString() {
        return "0x" + Long.toHexString(this.address) + "#" + this.allocated + "(" + this.length + ")"
                + Arrays.toString(this.storageInfo);
    }

    byte[] extendWith(byte[] extension, int[] newStorageInfo) {
        final byte[] result = doExtendWith(extension, newStorageInfo);
        putBytes(result);
        this.storageInfo = newStorageInfo;
        return result;
    }

    private byte[] doExtendWith(byte[] extension, int[] newStorageInfo) {
        if (isIncrement(newStorageInfo)) {
            return mergeWithIncrement(extension, newStorageInfo);
        }
        else {
            return mergeChunks(extension);
        }
    }

    /**
     * @param increment as obtained from
     * {@link de.marketmaker.istar.feed.ordered.tick.MemoryTickStore#getTicksIncrement(long, int[], int[])}
     * @param newStorageInfo storageInfo for merged data
     */
    private byte[] mergeWithIncrement(byte[] increment, int[] newStorageInfo) {
        int newLength = ArraysUtil.sum(newStorageInfo);
        assert newLength > this.length : (newLength + "<=" + this.length);

        final byte[] result = getBytes(newLength);
        final ByteBuffer bb = BufferFieldData.asBuffer(result);
        int bytesOffset = 0;
        int incOffset = 0;
        for (int i = 0; i < newStorageInfo.length; i++) {
            if (i < this.storageInfo.length) {
                if (this.storageInfo[i] == newStorageInfo[i]) {
                    bytesOffset = put(bb, result, bytesOffset, this.storageInfo[i]);
                }
                else {
                    bb.putInt(newStorageInfo[i] - 4);
                    bytesOffset = put(bb, result, bytesOffset + 4, this.storageInfo[i] - 4);
                    incOffset = put(bb, increment, incOffset, newStorageInfo[i] - this.storageInfo[i]);
                }
            }
            else {
                final int length = newStorageInfo[i] - 4;
                bb.putInt(length);
                incOffset = put(bb, increment, incOffset, length);
            }
        }
        return result;
    }

    private int put(ByteBuffer bb, byte[] bytes, int offset, int length) {
        if (bb.array() == bytes) {
            bb.position(bb.position() + length);
        }
        else {
            bb.put(bytes, offset, length);
        }
        return offset + length;
    }

    private byte[] mergeChunks(byte[] extension) {
        // after getBytes, result already contains the previously stored ticks
        final byte[] result = getBytes(new byte[this.storageInfo[0] + extension.length], this.storageInfo[0]);
        System.arraycopy(extension, 0, result, this.storageInfo[0], extension.length);
        return result;
    }

    private boolean isIncrement(int[] newStorageInfo) {
        return this.storageInfo[0] == newStorageInfo[0];
    }

    private byte[] getBytes(int size) {
        return getBytes(new byte[size], Math.min(size, this.length));
    }

    /**
     * Copies the first <code>size</code> bytes stored off heap into <code>dst</code>
     * @return <code>dst</code>
     */
    private byte[] getBytes(byte[] dst, int size) {
        assert size <= dst.length && size <= this.length : (this.length + "/" + dst.length + "/" + size);
//        LOGGER.debug("copy " + this.length + "@0x" + Long.toHexString(this.address) + " -> [" + size + "]");
        UNSAFE.copyMemory(null, this.address, dst, BASE_OFFSET, size);
        return dst;
    }

    private void putBytes(byte[] data) {
        final int requiredSize = align(data.length);
        if (this.address == 0L) {
            this.address = UNSAFE.allocateMemory(requiredSize);
            this.allocated = requiredSize;
//            LOGGER.debug("allocate " + requiredSize + " -> " + Long.toHexString(this.address));
        }
        else if (data.length > this.allocated) {
//            long prev = this.address;
            this.address = UNSAFE.reallocateMemory(this.address, requiredSize);
            this.allocated = requiredSize;
//            LOGGER.debug("reallocate " + requiredSize + " "
//                    + Long.toHexString(prev) + " -> " + Long.toHexString(this.address));
        }
//        LOGGER.debug("copy " + data.length + " -> " + Long.toHexString(this.address));
        UNSAFE.copyMemory(data, BASE_OFFSET, null, this.address, data.length);
        this.length = data.length;
    }

    private int align(int v) {
        // grow by at at least 12.5%
        final int k = Math.max(v + (v >> 3), this.allocated + (this.allocated >> 3));
        return (k + 0x400) & 0x7FFFFC00;
    }

    void freeMemory() {
        UNSAFE.freeMemory(this.address);
//        LOGGER.debug("free " + Long.toHexString(this.address));
        this.address = 0L;
        this.allocated = 0;
        this.length = 0;
    }
}
