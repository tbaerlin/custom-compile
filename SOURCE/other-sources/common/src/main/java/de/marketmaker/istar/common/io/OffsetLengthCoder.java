/*
 * OffsetLength.java
 *
 * Created on 15.05.2014 11:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.io;

/**
 * @author zzhao
 */
public final class OffsetLengthCoder {

    private final int lengthBits;

    private final int lengthMask;

    private final long offsetMask;

    public OffsetLengthCoder(int lengthBits) {
        if (lengthBits <= 0) {
            throw new IllegalArgumentException("length bits must be positive number");
        }
        if (lengthBits >= Integer.SIZE) {
            throw new IllegalArgumentException("max length bits " + (Integer.SIZE - 1)
                    + ", given " + lengthBits);
        }

        // At this point lengthBits can only be between 1 and 31
        this.lengthBits = lengthBits;

        long offsetMask = 0L;
        for (int i = 0; i < (Long.SIZE - lengthBits); i++) {
            offsetMask |= (1L << i);
        }
        // offsetMask will have the lower 32 bits all set to 1 and bit 63 set to 0
        // all bits between 32 and (63 - lengthBits) will also be 1
        this.offsetMask = offsetMask;

        int lengthMask = 0;
        for (int i = 0; i < lengthBits; i++) {
            lengthMask |= (1 << i);
        }
        this.lengthMask = lengthMask;
    }

    /**
     * This method will store offset and length in one long value.
     * Offset will be shifted to the left as many bits as the amount
     * needed to represent the maximum length.
     * @param offset Offset
     * @param length Length
     * @return Offset and length encoded into a long value
     */
    public long encode(long offset, int length) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset underflow: " + offset);
        }
        if (offset > maxOffset()) {
            throw new IllegalArgumentException("offset overflow: " + offset);
        }

        if (length < 0) {
            throw new IllegalArgumentException("length underflow: " + length);
        }
        if (length > maxLength()) {
            throw new IllegalArgumentException("length overflow: " + length);
        }

        return offset << this.lengthBits | length;
    }

    /**
     * Extract offset from combined offsetAndLength value
     * @param offsetAndLength Offset and length encoded into one long value
     * @return Offset
     */
    public long decodeOffset(long offsetAndLength) {
        // Move value as many bits to the right as required to encode length
        // and apply mask
        return this.offsetMask & (offsetAndLength >> this.lengthBits);
    }

    /**
     * Remove offset information from combined value and return length as int
     * @param offsetAndLength Offset and length encoded into one long value
     * @return Length
     */
    public int decodeLength(long offsetAndLength) {
        return (int) offsetAndLength & this.lengthMask;
    }

    public int maxLength() {
        return this.lengthMask;
    }

    public long maxOffset() {
        return this.offsetMask;
    }

    public int getLengthBits() {
        return lengthBits;
    }

    @Override
    public String toString() {
        return "OffsetLengthCoder{" + lengthBits + '}';
    }
}
