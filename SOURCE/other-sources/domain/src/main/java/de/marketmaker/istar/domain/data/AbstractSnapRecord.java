/*
 * AbstractSnapRecord.java
 *
 * Created on 27.10.2005 10:43:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.common.util.PriceCoder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "performance")
public abstract class AbstractSnapRecord implements SnapRecord, Serializable {
    protected static final long serialVersionUID = 23816867633L;

    private final int[] fieldids;

    private final int[] offsets;

    private final byte[] data;

    private static final Integer ZERO_INT = 0;

    private static final Long ZERO_LONG = 0L;

    public AbstractSnapRecord(int[] indexArray, int[] offsetArray, byte[] data) {
        this.fieldids = indexArray;
        this.offsets = offsetArray;
        this.data = data;
    }

    public SnapField getField(String fieldname) {
        final int fieldid = getFieldByName(fieldname);
        if (fieldid < 0) {
            return createEmptySnapField(fieldid);
        }
        return getField(fieldid);
    }

    protected abstract int getFieldByName(String fieldname);

    /**
     * Returns the object representing the field with the given fieldId;
     * @param fieldid specifies the field
     * @return field for the given id. If no field with the given id exists in this
     *         record, a field is returned for which {@link SnapField#isDefined} yields false.
     */
    public SnapField getField(int fieldid) {
        final int i = getFieldIndex(fieldid);
        return (i >= 0) ? getSnapField(i) : createEmptySnapField(fieldid);
    }

    protected final int getFieldIndex(int fieldid) {
        return Arrays.binarySearch(this.fieldids, fieldid);
    }

    private SnapField createEmptySnapField(int fieldid) {
        return createSnapField(fieldid, null);
    }

    protected abstract SnapField createSnapField(int fieldid, Object value);

    protected final SnapField getSnapField(int index) {
        final int offset = this.offsets[index];
        final ByteBuffer buffer = ByteBuffer.wrap(this.data);
        buffer.position(offset);

        final int fieldid = this.fieldids[index];
        if (isInt(fieldid)) {
            if (isZero(index)) {
                return createSnapField(fieldid, ZERO_INT);
            }
            // HACK for ADF_Status/Tick that are stored as short
            final int length = this.offsets[index + 1] - offset;
            final int ivalue = (length == 2) ? buffer.getShort() : buffer.getInt();
            if (ivalue > Integer.MIN_VALUE) {
                return createSnapField(fieldid, ivalue);
            }
        }
        else if (isLong(fieldid)) {
            if (isZero(index)) {
                return createSnapField(fieldid, ZERO_LONG);
            }
            final long lvalue;
            if (this.offsets[index + 1] - offset == 5) {
                lvalue = PriceCoder.encode(buffer.getInt(), buffer.get());
            }
            else {
                lvalue = buffer.getLong();
            }
            if (lvalue > Long.MIN_VALUE) {
                return createSnapField(fieldid, lvalue);
            }
        }
        else if (isBoolean(fieldid)) {
            final byte bvalue = buffer.get();
            if (bvalue > Byte.MIN_VALUE) {
                return createSnapField(fieldid, bvalue != 0);
            }
        }
        else if (isString(fieldid)) {
            final String s = readString(index, offset);
            return createSnapField(fieldid, s);
        }
        else {
            throw new IllegalArgumentException("unknown field type for field " + fieldid);
        }

        return createEmptySnapField(fieldid);
    }

    protected String readString(int index, int offset) {
        final int length = this.offsets[index + 1] - offset;
        return readStringStopAt0(offset, length);
    }

    protected String readStringStopAt0(int offset, int length) {
        return ByteUtil.toStringStopAt0(this.data, offset, length);
    }

    protected String readStringStopAt0(int offset, int length, Charset cs) {
        final int end = findEndOfString(offset, length);
        return new String(this.data, offset, end - offset, cs);
    }

    private int findEndOfString(int offset, int length) {
        int result = offset;
        final int maxEnd = offset + length;
        while (result < maxEnd && this.data[result] != 0) {
            result++;
        }
        return result;
    }

    protected abstract boolean isBoolean(int fieldid);

    protected abstract boolean isInt(int fieldid);

    protected abstract boolean isLong(int fieldid);

    protected abstract boolean isString(int fieldid);

    protected boolean isZero(int index) {
        return false;
    }

    public String toString() {
        return "SnapRecord[" + getSnapFields() + "]";
    }

    public String dumpToString() {
        return "SnapRecord[" + getSnapFields() + "]"
                + "\r\n"
                + HexDump.toHex(this.data)
                + "\r\nfieldids: " + Arrays.toString(this.fieldids)
                + "\r\noffsets: " + Arrays.toString(this.offsets);
    }

    public List<SnapField> getSnapFields() {
        final List<SnapField> fields = new ArrayList<>(this.fieldids.length);

        for (int i = 0; i < this.fieldids.length - 1; i++) {
            fields.add(getSnapField(i));
        }

        return fields;
    }

    public int[] getFieldids() {
        return fieldids;
    }

    public int[] getOffsets() {
        return offsets;
    }

    public byte[] getData() {
        return data;
    }
}
