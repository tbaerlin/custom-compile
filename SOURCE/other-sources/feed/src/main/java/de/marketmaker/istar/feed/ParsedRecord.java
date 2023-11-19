/*
 * ParsedRecord.java
 *
 * Created on 28.10.2004 13:51:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class ParsedRecord {

    private static final int FIELD_ID_MASK = 0xFFFF;

    private static final int ORDER_ID_MASK = 0x7FFF0000;

    /**
     * Store one parsed field for each field that can possibly be parsed. This avoids to create
     * new ParsedField elements during parsing.
     */
    private ParsedField[] parsedFields = new ParsedField[VwdFieldDescription.length()];

    /**
     * Used to detect duplicate fields in update records. Duplicate fields have to
     * be detected and ignored to avoid, for example, illegal SQL statements.
     */
    private final BitSet fields = new BitSet(VwdFieldDescription.length());

    /**
     * Stores the ids of all fields that are actually contained in the parsed record; only the
     * first {@link #numberOfFields} elements are valid at any given time.
     */
    private int[] fieldIds = new int[VwdFieldDescription.length()];

    private int numberOfFields = 0;

    private BitSet duplicateFields = null;

    /**
     * binary representation of the ADF_Track field, see <a href="http://cfl.market-maker.de/display/CPS/Architecture+non-functional+requirements">confluence</a> for details.
     */
    private byte[] track;

    private int fieldFlags;

    private FeedRecord record;

    private int adfTimeOfArrival;

    private int adfZeit;

    private int adfBoersenzeit;

    private int adfDateOfArrival;

    private int sourceId;

    private byte messageTypeMdps;

    private byte messageType;

    private int messageTimestamp;

    private int keyType;

    private final DateTimeProvider dateTimeProvider;

    private int flags;

    public ParsedRecord() {
        this(DateTimeProviderImpl.INSTANCE);
    }

    public ParsedRecord(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
        final BitSet bs = VwdFieldDescription.getFieldIds();
        for (int fieldid = bs.nextSetBit(0); fieldid >= 0; fieldid = bs.nextSetBit(fieldid + 1)) {
            this.parsedFields[fieldid] = new ParsedField(VwdFieldDescription.getField(fieldid));
        }
    }

    public void reset(FeedRecord record) {
        this.record = record;
        this.numberOfFields = 0;
        this.fieldFlags = 0;
        this.sourceId = 0;
        this.duplicateFields = null;
        this.track = null;

        this.adfDateOfArrival = Integer.MIN_VALUE;
        this.adfTimeOfArrival = Integer.MIN_VALUE;
        this.adfBoersenzeit = Integer.MIN_VALUE;
        this.adfZeit = Integer.MIN_VALUE;

        this.fields.clear();
    }

    /**
     * Create a new ParsedRecord based on an already existing instance.
     * Since this class is usually instanced only once and reused by resetting the contained data
     * if can't be used in a multithreaded context where a different thread will access the data
     * after the parser may have already reset the reused instance.
     *
     * @param copyFrom The instance to copy the data from.
     */
    public ParsedRecord(ParsedRecord copyFrom) {
        this.dateTimeProvider = copyFrom.dateTimeProvider;
        this.parsedFields = copyFrom.parsedFields.clone();

        this.record = copyFrom.record;
        this.numberOfFields = copyFrom.numberOfFields;
        this.fieldFlags = copyFrom.fieldFlags;
        this.sourceId = copyFrom.sourceId;
        this.duplicateFields = copyFrom.duplicateFields;
        this.track = copyFrom.track;


        this.messageTimestamp = copyFrom.messageTimestamp;
        this.adfDateOfArrival = copyFrom.adfDateOfArrival;
        this.adfTimeOfArrival = copyFrom.adfTimeOfArrival;
        this.adfBoersenzeit = copyFrom.adfBoersenzeit;
        this.adfZeit = copyFrom.adfZeit;

        for (int i = 0; i < copyFrom.fields.length(); i++) {
            this.fields.set(i, copyFrom.fields.get(i));
        }
    }

    int getLastParsedField() {
        if (this.numberOfFields == 0) {
            return -1;
        }
        return getFieldId(this.numberOfFields - 1);
    }

    public int getAdfDateOfArrival() {
        return this.adfDateOfArrival;
    }

    public int getAdfTimeOfArrival() {
        return this.adfTimeOfArrival;
    }

    public boolean isEmpty() {
        return this.numberOfFields == 0;
    }

    public int[] getFieldids() {
        final int[] result = new int[this.numberOfFields];
        for (int i = result.length; i-- != 0;) {
            result[i] = getFieldId(i);
        }
        return result;
    }

    public BitSet getFields() {
        final BitSet result = new BitSet(getFieldId(this.numberOfFields - 1));
        result.or(this.fields);
        return result;
    }

    public boolean isFieldPresent(int fieldid) {
        return this.fields.get(fieldid);
    }

    public boolean isAnyFieldPresent(BitSet bs) {
        return this.fields.intersects(bs);
    }

    public boolean isAnyDuplicateField() {
        return this.duplicateFields != null;
    }

    public BitSet getDuplicateFields() {
        return this.duplicateFields;
    }

    public boolean isWithFieldFlags(int flags) {
        return (this.fieldFlags & flags) != 0;
    }

    public void setField(int fieldId, int start, int length) {
        setField(fieldId, encode(start, length));
        this.parsedFields[fieldId].undefineAscii();
    }

    public void setField(int fieldId, long value) {
        if (this.fields.get(fieldId)) {
            if (this.duplicateFields == null) {
                this.duplicateFields = new BitSet(VwdFieldDescription.length());
            }
            this.duplicateFields.set(fieldId);
            return;
        }
        this.fields.set(fieldId);

        final ParsedRecord.ParsedField pf = this.parsedFields[fieldId];
        pf.setValue(value);
        this.fieldFlags |= pf.field.flags();

        if (pf.field == VwdFieldDescription.ADF_TIMEOFARR) {
            this.adfTimeOfArrival = (int) value;
        }
        else if (pf.field == VwdFieldDescription.ADF_DATEOFARR) {
            this.adfDateOfArrival = (int) value;
        }
        else if (pf.field == VwdFieldDescription.ADF_Zeit) {
            this.adfZeit = (int) value;
        }
        else if (pf.field == VwdFieldDescription.ADF_Boersenzeit) {
            this.adfBoersenzeit = (int) value;
        }

        this.fieldIds[this.numberOfFields++] = pf.order;
    }

    public int getMarketTime() {
        return (this.adfBoersenzeit > 0) ? this.adfBoersenzeit :
                (this.adfZeit > 0) ? this.adfZeit :
                        (this.adfTimeOfArrival > 0) ? this.adfTimeOfArrival :
                                this.dateTimeProvider.secondOfDay();
    }

    public int getAdfZeit() {
        return this.adfZeit;
    }

    public int getAdfBoersenzeit() {
        return adfBoersenzeit;
    }

    /**
     * Invokes the builder's set methods for all fields the builder is interested in
     * @param builder target for updates
     */
    public void setFields(FieldBuilder builder) {
        final int builderFlags = builder.getFieldFlags();
        if (builderFlags == 0) {
            setAllFields(builder);
            return;
        }
        if ((builderFlags & this.fieldFlags) == 0) {
            return;
        }

        for (int i = 0; i < this.numberOfFields; i++) {
            final ParsedField pf = this.parsedFields[getFieldId(i)];
            if ((pf.field.flags() & builderFlags) != 0) {
                setField(builder, pf);
            }
        }
    }

    /**
     * Invokes the builder's set methods for all fields, ignores the builder's
     * {@link FieldBuilder#getFieldFlags()} method.
     * @param builder target for updates
     */
    public void setAllFields(FieldBuilder builder) {
        for (int i = 0; i < this.numberOfFields; i++) {
            setField(builder, this.parsedFields[getFieldId(i)]);
        }
    }

    public void setAllFields(OrderedFieldBuilder builder) {
        Arrays.sort(this.fieldIds, 0, this.numberOfFields);
        for (int i = firstFieldWithOrder(), n = this.numberOfFields; i < n; i++) {
            setField(builder, this.parsedFields[getFieldId(i)]);
        }
    }

    public void setFields(OrderedFieldBuilder builder, int flags) {
        if (flags == 0) {
            setAllFields(builder);
            return;
        }
        if ((flags & this.fieldFlags) == 0) {
            return;
        }
        Arrays.sort(this.fieldIds, 0, this.numberOfFields);
        for (int i = firstFieldWithOrder(), n = this.numberOfFields; i < n; i++) {
            final ParsedField pf = this.parsedFields[getFieldId(i)];
            if ((pf.field.flags() & flags) != 0) {
                setField(builder, pf);
            }
        }
    }

    private int firstFieldWithOrder() {
        final int n = this.numberOfFields;
        for (int i = 0; i < n; i++) {
            if ((this.fieldIds[i] & ORDER_ID_MASK) != 0) {
                return i;
            }
        }
        return n;
    }

    private void setField(OrderedFieldBuilder builder, ParsedField prf) {
        final int orderId = prf.order >> 16;
        if (orderId == 0) {
            return; // ignore field with unspecified order
        }
        switch (prf.field.type()) {
            case TIME:
                builder.setTime(orderId, prf.getIntValue());
                break;
            case UINT:
            case USHORT:
            case DATE:
                builder.setInt(orderId, prf.getIntValue());
                break;
            case PRICE:
                builder.setPrice(orderId, prf.getLongValue());
                break;
            case STRING:
                builder.setString(orderId, this.record.getData(), prf.getStartValue(), prf.getLengthValue());
                break;
        }
    }

    private void setField(FieldBuilder builder, ParsedField prf) {
        switch (prf.field.type()) {
            case UINT:
            case USHORT:
            case TIME:
            case DATE:
                builder.set(prf.field, prf.getIntValue());
                break;
            case PRICE:
                builder.set(prf.field, prf.getLongValue());
                break;
            case STRING:
                builder.set(prf.field, this.record.getData(), prf.getStartValue(), prf.getLengthValue());
                break;
        }
    }

    /**
     * @return the FeedRecord that was parsed to create the information in this object
     */
    public FeedRecord getRecord() {
        return this.record;
    }

    public long getNumericValue(final int fieldId) {
        if (!this.fields.get(fieldId)) {
            return Long.MIN_VALUE;
        }
        return this.parsedFields[fieldId].getLongValue();
    }

    public byte[] getBytes(final int fieldId) {
        if (!this.fields.get(fieldId)) {
            return null;
        }
        return this.parsedFields[fieldId].getBytes();
    }

    public String getString(final int fieldId) {
        if (!this.fields.get(fieldId)) {
            return null;
        }
        return this.parsedFields[fieldId].getString();
    }

    public String getString(final int fieldId, Charset cs) {
        if (!this.fields.get(fieldId)) {
            return null;
        }
        return this.parsedFields[fieldId].getString(cs);
    }

    public int getLength(final int fieldId) {
        if (!this.fields.get(fieldId)) {
            return 0;
        }
        return this.parsedFields[fieldId].getLengthValue();
    }

    public AsciiBytes getAsciiBytes(final int fieldId) {
        if (!this.fields.get(fieldId)) {
            return null;
        }
        return this.parsedFields[fieldId].getAsciiBytes();
    }

    public String toString() {
        final StringBuilder stb = new StringBuilder();
        stb.append("ParsedRecord[");
        stb.append((char) this.messageType);

        for (int i = 0; i < this.numberOfFields; i++) {
            stb.append(",");
            stb.append(this.parsedFields[getFieldId(i)]);
        }

        stb.append("]");
        return stb.toString();
    }

    private int getFieldId(int i) {
        return this.fieldIds[i] & FIELD_ID_MASK;
    }

    public byte getMessageType() {
        return messageType;
    }

    public int getMessageTimestamp() {
        return messageTimestamp;
    }

    public int getKeyType() {
        return keyType;
    }

    public void setKeyType(int keyType) {
        this.keyType = keyType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public void setMessageTypeMdps(byte messageTypeMdps) {
        this.messageTypeMdps = messageTypeMdps;
    }

    public byte getMessageTypeMdps() {
        return messageTypeMdps;
    }

    public void setMessageTimestamp(int messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public boolean isDelete() {
        return VwdFeedConstants.isDelete(this.messageType);
    }

    public boolean isRecap() {
        return VwdFeedConstants.isRecap(this.messageType);
    }

    static long encode(int start, int length) {
        return (((long) start) << 32) + length;
    }

    static int decodeStart(long value) {
        return (int) (value >> 32);
    }

    static int decodeLength(long value) {
        return (int) value;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setTrack(byte[] track) {
        this.track = track;
    }

    public byte[] getTrack() {
        return track;
    }

    final class ParsedField {
        private final VwdFieldDescription.Field field;

        private final int order;

        private final AsciiBytes asciiBytes = new AsciiBytes();

        private long value;

        ParsedField(VwdFieldDescription.Field field) {
            this.field = field;
            this.order = (VwdFieldOrder.getOrder(field.id()) << 16) + field.id();
        }

        void setValue(long value) {
            if (this.value != value) {
                undefineAscii();
                this.value = value;
            }
        }

        private void undefineAscii() {
            this.asciiBytes.setUndefined();
        }

        int getIntValue() {
            return (int) this.value;
        }

        int getStartValue() {
            return decodeStart(this.value);
        }

        int getLengthValue() {
            return decodeLength(this.value);
        }

        private long getLongValue() {
            return this.value;
        }

        public String toString() {
            final AsciiBytes ab = getAsciiBytes();
            return this.field + "=" + new String(ab.getData(), ab.getOffset(), ab.getLength());
        }

        public AsciiBytes getAsciiBytes() {
            if (this.asciiBytes.isUndefined()) {
                switch (this.field.type()) {
                    case STRING:
                        this.asciiBytes.setString(ParsedRecord.this.record.getData(), getStartValue(), getLengthValue());
                        break;
                    case TIME:
                        this.asciiBytes.setTime((int) this.value);
                        break;
                    case DATE:
                        this.asciiBytes.setDate((int) this.value);
                        break;
                    case PRICE:
                        this.asciiBytes.setPrice(this.value);
                        break;
                    case UINT:
                    case USHORT:
                        this.asciiBytes.setNumber(this.value & 0xFFFFFFFFL);
                        break;
                    default:
                        this.asciiBytes.setOffset(0);
                        this.asciiBytes.setLength(0);
                }
            }
            return asciiBytes;
        }

        public byte[] getBytes() {
            final byte[] result = new byte[getLengthValue()];
            System.arraycopy(ParsedRecord.this.record.getData(), getStartValue(), result, 0, result.length);
            return result;
        }

        public String getString() {
            return ByteUtil.toString(ParsedRecord.this.record.getData(), getStartValue(), getLengthValue());
        }

        public String getString(Charset cs) {
            return new String(ParsedRecord.this.record.getData(), getStartValue(), getLengthValue(), cs);
        }
    }
}
