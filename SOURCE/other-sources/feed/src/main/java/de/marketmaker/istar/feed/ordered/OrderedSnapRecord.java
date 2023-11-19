/*
 * OrderedSnapRecord.java
 *
 * Created on 20.09.12 13:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.marketmaker.istar.domain.data.AbstractSnapField;
import de.marketmaker.istar.domain.data.FieldTypeEnum;
import de.marketmaker.istar.domain.data.NullSnapField;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.decodeDate;
import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.decodeTime;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.hasZeroBit;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.removeZeroBit;

/**
 * @author oflege
 */
public class OrderedSnapRecord implements SnapRecord, Serializable {
    protected static final long serialVersionUID = 1L;

    private static final int TOA_ID = VwdFieldDescription.ADF_TIMEOFARR.id();

    private static final int DOA_ID = VwdFieldDescription.ADF_DATEOFARR.id();

    static class Field extends AbstractSnapField {
        Field(int id, Object value) {
            super(id, value);
        }

        @Override
        public String getName() {
            return getField().name();
        }

        private VwdFieldDescription.Field getField() {
            return VwdFieldDescription.getField(getId());
        }

        @Override
        public FieldTypeEnum getType() {
            return getField().type().getFieldType();
        }

        @Override
        public BigDecimal getPrice() {
            return (getType() == FieldTypeEnum.PRICE)
                    ? (BigDecimal) getValue() : null;
        }

        @Override
        public String toString() {
            return getId() + "=" + getValue();
        }
    }

    private static class ZeroPrice extends Field {
        private ZeroPrice(int id, Object value) {
            super(id, value);
        }

        @Override
        public Object getValue() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal getLastPrice() {
            return (BigDecimal) super.getValue();
        }

        @Override
        public String toString() {
            return getId() + "=0, last=" + getLastPrice();
        }
    }

    private static final int TYPE_SHIFT = 16;

    private static final int FID_SHIFT = 18;

    private final byte[] data;

    private final int lastUpdateTimestamp;

    private final int nominalDelayInSeconds;

    /**
     * Index needed to access individual fields without scanning all prior fields, will only
     * be created when needed. Each array element contains a field id, the field's ordered type
     * and the offset of the field's value in <code>data</code>, elements are sorted to support
     * binary search for field id.
     */
    private transient int[] index;

    public OrderedSnapRecord(byte[] orderedData, int nominalDelayInSeconds) {
        this(orderedData, 0, nominalDelayInSeconds);
    }

    public OrderedSnapRecord(byte[] orderedData, int lastUpdateTimestamp,
            int nominalDelayInSeconds) {
        this.data = orderedData;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.nominalDelayInSeconds = nominalDelayInSeconds;
    }

    @Override
    public String toString() {
        return getSnapFields().toString();
    }

    int getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }

    byte[] getData() {
        return this.data;
    }

    /**
     * number of snap fields in this record
     */
    public int size() {
        return getIndex().length;
    }

    /**
     * length of the ordered data array
     */
    public int length() {
        return this.data.length;
    }

    private int[] getIndex() {
        if (this.index == null) {
            this.index = createIndex();
        }
        return this.index;
    }

    private int[] createIndex() {
        int[] tmp = new int[Math.max(16, this.data.length / 5)];
        int n = 0;

        final ByteBuffer bb = asBuffer();
        final BufferFieldData fd = new BufferFieldData(bb);
        int orderId;
        while ((orderId = fd.readNext()) != 0) {
            int fid = VwdFieldOrder.getFieldId(orderId);
            if (fid == 0) {
                fd.skipCurrent();
                continue;
            }
            if (n == tmp.length) {
                tmp = Arrays.copyOf(tmp, tmp.length * 2);
            }
            tmp[n] = (fid << FID_SHIFT) + (fd.getType() << TYPE_SHIFT) + bb.position();
            if (fd.getType() == FieldData.TYPE_STRING) {
                // for strings, position has to be before the length
                tmp[n] -= (fd.getLength() > 0x7f) ? 2 : 1;
            }
            n++;
            fd.skipCurrent();
        }
        Arrays.sort(tmp, 0, n);
        return Arrays.copyOf(tmp, n);
    }

    private ByteBuffer asBuffer() {
        return BufferFieldData.asBuffer(this.data);
    }

    public Collection<SnapField> getSnapFields(boolean timeAsLocalTime) {
        final ArrayList<SnapField> result = new ArrayList<>(32);
        final BufferFieldData fd = asFieldData();
        int orderId;
        while ((orderId = fd.readNext()) != 0) {
            int fid = VwdFieldOrder.getFieldId(orderId);
            if (fid == 0) {
                fd.skipCurrent();
                continue;
            }
            final Field field;
            if (fd.getType() == FieldData.TYPE_PRICE)
                field = newPriceField(fid, fd.getInt(), fd.getByte());
            else if (timeAsLocalTime && fd.getType() == FieldData.TYPE_TIME) {
                field = new Field(fid, MdpsFeedUtils.decodeLocalTime(fd.getInt()));
            }
            else {
                field = new Field(fid, getFieldValue(fd));
            }
            result.add(field);
        }

        if (this.lastUpdateTimestamp != 0) {
            result.add(createTimeOfArrival());
            result.add(createDateOfArrival());
        }

        return result;
    }

    public Collection<SnapField> getSnapFields() {
        return getSnapFields(false);
    }

    private Field createDateOfArrival() {
        return new Field(DOA_ID, decodeDate(this.lastUpdateTimestamp));
    }

    private Field createTimeOfArrival() {
        return new Field(TOA_ID, decodeTime(this.lastUpdateTimestamp));
    }

    public static Object getFieldValue(FieldData fd) {
        return getFieldValue(fd, false);
    }

    public static Object getFieldValue(FieldData fd, boolean escapeNulls) {
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                if (VwdFieldOrder.getField(fd.getId()).type() == VwdFieldDescription.Type.UINT) {
                    return fd.getUnsignedInt();
                } else {
                    return fd.getInt();
                }
            case FieldData.TYPE_TIME:
                return MdpsFeedUtils.decodeTime(fd.getInt());
            case FieldData.TYPE_PRICE:
                return getDecimal(fd.getInt(), fd.getByte(), escapeNulls);
            case FieldData.TYPE_STRING:
                return fd.getLength() == 0 ? "" : toString(fd.getBytes());
            default:
                throw new IllegalStateException("Unknown type: " + fd.getType());
        }
    }

    @Override
    public SnapField getField(int fieldId) {
        final int pos = (fieldId <= 0) ? -1 : binarySearch(0, size(), fieldId);
        if (pos < 0) {
            if (fieldId == TOA_ID && this.lastUpdateTimestamp != 0) {
                return createTimeOfArrival();
            }
            if (fieldId == DOA_ID && this.lastUpdateTimestamp != 0) {
                return createDateOfArrival();
            }
            return NullSnapField.INSTANCE;
        }
        return readField(fieldId, pos);
    }

    private SnapField readField(int fieldId, int pos) {
        assert this.index != null;
        final int position = this.index[pos] & 0xFFFF;
        final int type = (this.index[pos] >> TYPE_SHIFT) & 0x03;
        final ByteBuffer bb = asBuffer();
        bb.position(position);
        switch (type) {
            case FieldData.TYPE_INT:
                if (VwdFieldDescription.getField(fieldId).type() == VwdFieldDescription.Type.UINT) {
                    return new Field(fieldId, Integer.toUnsignedLong(bb.getInt()));
                } else {
                    return new Field(fieldId, bb.getInt());
                }
            case FieldData.TYPE_TIME:
                return new Field(fieldId, MdpsFeedUtils.decodeTime(bb.getInt()));
            case FieldData.TYPE_PRICE:
                return newPriceField(fieldId, bb.getInt(), bb.get());
            case FieldData.TYPE_STRING:
                final int length = getStopBitEncoded(bb);
                if (length == 0) {
                    return new Field(fieldId, "");
                }
                byte[] bytes = new byte[length];
                bb.get(bytes);
                return new Field(fieldId, toString(bytes));
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private Field newPriceField(int fieldId, int base, int exp) {
        final BigDecimal bd = getDecimal(base, exp, true);
        return hasZeroBit(exp) ? new ZeroPrice(fieldId, bd) : new Field(fieldId, bd);
    }

    public static BigDecimal getDecimal(int base, int exp, boolean escapeNulls) {
        if (hasZeroBit(exp)) {
            return escapeNulls ? BigDecimal.valueOf(base, -removeZeroBit(exp)) : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(base, -exp);
    }

    public static String toString(byte[] bytes) {
        int n = 0;
        while (n < bytes.length && bytes[n] != 0) n++;
        return new String(bytes, 0, n, SnapRecord.DEFAULT_CHARSET);
    }

    private int getStopBitEncoded(ByteBuffer bb) {
        final int first = bb.get();
        if (first < 0) {
            return 0x7F & first;
        }
        return (first << 7) | (0x7F & bb.get());
    }

    @Override
    public SnapField getField(String fieldname) {
        final VwdFieldDescription.Field f = VwdFieldDescription.getFieldByName(fieldname);
        return getField(f != null ? f.id() : 0);
    }

    @Override
    public int getNominalDelayInSeconds() {
        return this.nominalDelayInSeconds;
    }

    /**
     * temporary hack: as long as there are clients that cannot handle zero flags for prices,
     * those prices have to be explicitly removed.
     */
    public void eraseLastValueForZeroPrices() {
        asFieldData().eraseLastValueForZeroPrices();
    }

    private BufferFieldData asFieldData() {
        return new BufferFieldData(this.data);
    }

    /**
     * Return a new snap record that has all fields present in <tt>this</tt> and <tt>update</tt>,
     * values for fields present in both will be taken from <tt>update</tt>, which is also
     * the source for all other attributes besides snap fields.
     * @param update contains src fields to be merged with this object's fields
     * @return new snap record, <tt>this</tt> and <tt>update</tt> remain unchanged.
     */
    public OrderedSnapRecord merge(OrderedSnapRecord update) {
        FieldDataMerger merger = new FieldDataMerger(2 * (this.data.length + update.data.length));
        byte[] copy = Arrays.copyOf(this.data, this.data.length);
        byte[] merged = merger.merge(new BufferFieldData(copy), update.asFieldData());
        return new OrderedSnapRecord(merged != null ? merged : copy,
                update.lastUpdateTimestamp, update.nominalDelayInSeconds);
    }

    private int binarySearch(int fromIndex, int toIndex, int key) {
        assert this.index != null;

        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = (this.index[mid] >> FID_SHIFT);

            if (midVal < key) {
                low = mid + 1;
            }
            else if (midVal > key) {
                high = mid - 1;
            }
            else {
                return mid; // key found
            }
        }
        return -1;  // key not found.
    }
}
