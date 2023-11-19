/*
 * OffHeapStore.java
 *
 * Created on 19.01.12 13:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import de.marketmaker.istar.common.util.ByteBufferUtils;

/**
 * Direct (i.e., off heap) ByteBuffer split into a number of equal sized chunks used for storing
 * arbitrary data. The main idea is to have as few as possible objects that are susceptible to garbage
 * collection. To be able to find available chunks fast, this buffer maintains a two level index of free
 * chunks. See {@link MemoryTickStore} for explanation.
 * @author oflege
 */
class OffHeapStore {
    private final static int ADDRESS_BITS_PER_WORD = 6;

    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    /**
     * top level index, each bit corresponds to a single long in <tt>index1</tt>, the bit is set iff
     * the related long is not zero.
     */
    private final long[] index0;

    /**
     * mid level index, each bit corresponds to a single long in <tt>words</tt>, the bit is set iff
     * the related long is not zero
     */
    private final long[] index1;

    /**
     * a stripped down and faster (no checks, no resizing, etc) BitSet; there's a bit for each chunk
     * which will be set iff the chunk is available.
     */
    private final long[] words;

    /**
     * the direct buffer that wraps all the chunks; never used directly, reading/writing
     * uses duplicate(s) of the buffer, so that thread-safety is not an issue.
     */
    private ByteBuffer bb;

    private ByteBuffer bbWrite;

    /**
     * size of each chunk in the buffer in bytes
     */
    private final int chunkSize;

    /**
     * total number of chunks in this store
     */
    final int numChunks;

    /**
     * number of available chunks in this store
     */
    private int numFree;

    /**
     * since addresses in this store are multiples of 2, address pointers can be converted to
     * an index for the corresponding nextChunk by shifting them right this many bits
     */
    private final int addressIndexShift;

    private final int sizeMask;

    private final int addressMask;

    /**
     * last index0 index at which a value != 0 was found
     */
    private int index0Idx;

    private long numGetNextFreeOk = 0;

    private long numGetNextFreeFail = 0;

    OffHeapStore(int size, int chunkSize) {
        if (size % chunkSize != 0) {
            throw new IllegalArgumentException("size " + size
                    + " not multiple of chunkSize: " + chunkSize);
        }
        if (Integer.bitCount(chunkSize) != 1) {
            throw new IllegalArgumentException("not 2^x: " + chunkSize);
        }
        if (chunkSize < 64) {
            throw new IllegalArgumentException("chunkSize not >= 64: " + chunkSize);
        }

        this.chunkSize = chunkSize;
        this.bb = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
        this.bbWrite = duplicateBuffer();
        this.addressIndexShift = Integer.numberOfTrailingZeros(chunkSize);
        this.sizeMask = this.chunkSize - 1;
        this.addressMask = ~this.sizeMask;

        /* number of chunks in this store */
        this.numChunks = size / chunkSize;
        this.numFree = this.numChunks;
        this.words = createBitset(this.numFree);

        this.index1 = createBitset(this.words.length);
        this.index0 = createBitset(this.index1.length);
    }

    void destroy() {
        try {
            TickFileMapper.unmap(this.bb);
        } catch (IOException e) {
            // ignore
        }
        this.bb = null;
        this.bbWrite = null;
    }

    @Override
    public String toString() {
        return "OffHeapStore[" + this.bb.capacity() + "/" + this.chunkSize
                + ", #free=" + getNumBytesFree() + "/" + getNumChunksFree()
                + ", #gets=" + getNumGetNextFreeOk() + "/" + getNumGetNextFreeFail()
                + "]";
    }

    private static long[] createBitset(int numBits) {
        final long[] result = new long[wordIndex(numBits - 1) + 1];
        Arrays.fill(result, -1L);

        final int numBitsInLastValue = numBits % BITS_PER_WORD;
        if (numBitsInLastValue != 0) {
            result[result.length - 1] = (1L << numBitsInLastValue) - 1L;
        }
        return result;
    }

    private synchronized long getNumGetNextFreeOk() {
        return numGetNextFreeOk;
    }

    private synchronized long getNumGetNextFreeFail() {
        return numGetNextFreeFail;
    }

    ByteBuffer duplicateBuffer() {
        return ByteBufferUtils.duplicate(this.bb);
    }

    synchronized int getNextFree() {
        if (this.numFree == 0) {
            this.numGetNextFreeFail++;
            return -1;
        }
        int lastFree = doGetNextFree();
        this.numFree--;
        this.numGetNextFreeOk++;
        return getAddressByIndex(lastFree);
    }

    int getIndexOfAddress(int addr) {
        return addr >> this.addressIndexShift;
    }

    int getAddressByIndex(int idx) {
        return idx << this.addressIndexShift;
    }

    int getBaseAddress(long key) {
        final int base = ((int) key) & this.addressMask;
        return (base == (int) key) ? (base - this.chunkSize) : base;
    }

    int getRemaining(long key) {
        return this.chunkSize - getSize(key);
    }

    int getSize(long key) {
        final int size = ((int) key) & this.sizeMask;
        return (size == 0) ? this.chunkSize : size;
    }

    long put(long key, ByteBuffer src) {
        final long result;
        synchronized (this.bbWrite) {
            this.bbWrite.position((int) key);
            result = key + src.remaining();
            this.bbWrite.put(src);
        }
        return result;
    }

    long putLong(long key, long value) {
        synchronized (this.bbWrite) {
            ((ByteBuffer) this.bbWrite.position((int) key)).putLong(value);
        }
        return key + 8;
    }

    private int doGetNextFree() {
        // this loop always terminates as we only get here if there is at least one available chunk
        final int fin = this.index0Idx;
        while (this.index0[this.index0Idx] == 0) {
            if (this.index0Idx == 0) {
                this.index0Idx = this.index0.length;
            }
            this.index0Idx -= 1;
            if (fin == this.index0Idx) { // it does not terminate!?
                throw new IllegalStateException(this.numFree + "/" + Arrays.toString(this.index0));
            }
        }

        final int bit0Idx = Long.numberOfTrailingZeros(this.index0[this.index0Idx]);
        final int index1Idx = (this.index0Idx << ADDRESS_BITS_PER_WORD) + bit0Idx;

        final int bit1Idx = Long.numberOfTrailingZeros(this.index1[index1Idx]);
        final int index = (index1Idx << ADDRESS_BITS_PER_WORD) + bit1Idx;

        final long word = this.words[index];
        final int bitIndex = Long.numberOfTrailingZeros(word);
        this.words[index] ^= (1L << bitIndex);
        if (this.words[index] == 0) {
            this.index1[index1Idx] ^= (1L << bit1Idx);
            if (this.index1[index1Idx] == 0) {
                this.index0[this.index0Idx] ^= (1L << bit0Idx);
            }
        }
        return (index << ADDRESS_BITS_PER_WORD) + bitIndex;
    }

    synchronized void setAsFree(long addr) {
        final int bitIndex = getIndexOfAddress((int) addr);
        final int wordIndex = wordIndex(bitIndex);
        if (this.words[wordIndex] == 0) {
            final int index1Idx = wordIndex(wordIndex);
            if (this.index1[index1Idx] == 0) {
                this.index0[wordIndex(index1Idx)] |= (1L << index1Idx);
            }
            this.index1[index1Idx] |= (1L << wordIndex);
        }
        this.words[wordIndex] |= (1L << bitIndex);
        this.numFree++;
    }

    int getChunkSize() {
        return this.chunkSize;
    }

    int getCapacity () {
        return this.bb.capacity();
    }

    synchronized int getNumChunksFree() {
        return this.numFree;
    }

    long getNumBytesFree() {
        return getNumChunksFree() * getChunkSize();
    }
}
