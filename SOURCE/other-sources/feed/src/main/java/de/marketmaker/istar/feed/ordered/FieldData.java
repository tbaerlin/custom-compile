/*
 * FieldData.java
 *
 * Created on 11.07.12 10:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

/**
 * Access field data in an ordered feed record.
 * @author oflege
 */
public interface FieldData {
    int TYPE_INT = 0x00;

    int TYPE_PRICE = 0x01;

    int TYPE_STRING = 0x02;

    int TYPE_TIME = 0x03;

    /**
     * start over, {@link #readNext()} after this will return the first field order-id
     * @return this
     */
    FieldData rewind();

    /**
     * @return the next field order-id or 0 if no next field is available
     */
    int readNext();

    /**
     * @return the current field order-id (whatever the latest call of {@link #readNext()} returned)
     */
    int getId();

    /**
     * @return the current field's type: TYPE_INT, TYPE_PRICE, TYPE_STRING, or TYPE_TIME
     */
    int getType();

    /**
     * should not be called if {@link #getType()} returned TYPE_STRING
     * @return the value of the current int field or the mantissa of the current price field
     */
    int getInt();

    /**
     * This will return a int value as a unsigned int (i.e. long) for types like
     * {@link de.marketmaker.istar.feed.vwd.VwdFieldDescription.MdpsType#SIZE}
     * @return the value of the current int field as unsigned int (i.e. long)
     */
    default long getUnsignedInt() {
        return Integer.toUnsignedLong(getInt());
    }

    /**
     * should only be called if {@link #getType()} returned TYPE_PRICE
     * and only after {@link #getInt()} has
     * been called previously to retrieve the mantissa; may never be called multiple times
     * for the same field
     * @return the exponent of the current price field
     */
    byte getByte();

    /**
     * @return the length of the current field's value: 4 for int and time, 5 for price, and a variable
     * value for a string
     */
    int getLength();

    /**
     * @return the value of the current field as an array with {@link #getLength()} bytes. Mostly
     * useful if {@link #getType()} returned TYPE_STRING.
     */
    byte[] getBytes();

    /**
     * skip the current value so that the next order-id can be read; may not be called after the
     * some data of the current field value has been read; must be called for every field whose
     * id has been read and whose value is supposed to be ignored.
     */
    void skipCurrent();

    /**
     * @return field data for all fields
     */
    byte[] getAsByteArray();

    /**
     * @return data for all fields up to (and excluding) the current field
     */
    byte[] getAsByteArrayBeforeCurrent();

    void applyStringUpdateTo(FieldData existing);

    void putStringBytes(byte[] aByte, int offset, int length);

    /**
     * Overrides the value of the current field with the value of the current field in update;
     * must only be called if the current field's id is the same in this and update. If this method
     * returns true, {@link #readNext()} can be called for both update and this.
     * @param update source for new value
     * @return true iff the update could be applied (false may only happen for string fields
     * when the updated string requires more space than is available in this)
     */
    boolean mergeFieldFrom(FieldData update);
}
