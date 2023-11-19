/*
 * MdpsParser.java
 *
 * Created on 07.09.15 08:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import de.marketmaker.istar.feed.IllegalFieldException;
import de.marketmaker.istar.feed.Parser;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.*;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * @author oflege
 */
public abstract class MdpsParser extends Parser {
    private static final int TRACK_ID = getTrackId(1);

    private boolean processBigSourceId = false;

    private boolean processInvalidDates = false;

    private boolean ignoreInvalidDates = true;

    private boolean useMdpsDateFormat = Boolean.getBoolean("istar.useMdpsDateFormat");

    private final AtomicReference<IllegalFieldException> fieldException = new AtomicReference<>();

    public void setUseMdpsDateFormat(boolean useMdpsDateFormat) {
        this.useMdpsDateFormat = useMdpsDateFormat;
    }

    public void setIgnoreInvalidDates(boolean ignoreInvalidDates) {
        this.ignoreInvalidDates = ignoreInvalidDates;
    }

    public void setProcessInvalidDates(boolean processInvalidDates) {
        this.processInvalidDates = processInvalidDates;
    }

    public void setProcessBigSourceId(boolean processBigSourceId) {
        this.processBigSourceId = processBigSourceId;
    }

    protected IllegalFieldException getFieldException() {
        return this.fieldException.getAndSet(null);
    }

    protected final boolean parseField(int fieldId, ByteBuffer buffer) throws IllegalFieldException {
        final VwdFieldDescription.Field f = VwdFieldDescription.getField(fieldId);
        if (f == null) {
            this.fieldException.set(new IllegalFieldException(fieldId));
            return false;
        }

        switch (f.mdpsType()) {
            case SIZE:
                final long unum4 = getUnsignedInt(buffer);
                this.parsedRecord.setField(fieldId, unum4);
                return true;
            case TIME:
                final int time = buffer.getInt();
                this.parsedRecord.setField(fieldId, time);
                return true;
            case DATE:
                final int date = this.useMdpsDateFormat ? buffer.getInt() : getDate(buffer);
                if (date == -1 && !this.processInvalidDates) {
                    return this.ignoreInvalidDates;
                }
                this.parsedRecord.setField(fieldId, date);
                return true;
            case PRICE:
                final long price = getMdpsPrice0(buffer);
                this.parsedRecord.setField(fieldId, price);
                return true;
            case USHORT:
                final int ushort = getUnsignedShort(buffer);
                if (fieldId != ID_BIG_SOURCE_ID) {
                    this.parsedRecord.setField(fieldId, ushort);
                }
                else if (this.processBigSourceId) {
                    this.parsedRecord.setSourceId(ushort);
                }
                return true;
            case FLSTRING:
                parseFixedLengthString(fieldId, buffer, f);
                return true;
            case VLSHSTRING:
                if (fieldId == ID_ADF_TRACK) {
                    parseTrack(buffer);
                    return true;
                }
                // intentional fall-through
            case VLLGSTRING:
                parseString(fieldId, buffer, f);
                return true;
            default:
                // ?!
                throw new IllegalStateException("unknown type for " + f);
        }
    }

    private void parseFixedLengthString(int fieldId, ByteBuffer buffer,
            VwdFieldDescription.Field f) {
        if (f == ADF_Status || f == ADF_Tick) {
            // hack for mdps hack: 2 byte string contains UNUM2 value in little endian
            final int value = getUnsignedShort(buffer, LITTLE_ENDIAN);
            this.parsedRecord.setField(fieldId, value);
            return;
        }
        parseString(fieldId, buffer, f);
    }

    private void parseString(int fieldId, ByteBuffer buffer, VwdFieldDescription.Field f) {
        final int length = getLength(buffer, f);

        int start = buffer.position();

        // make sure field is not longer than vwd field desc. assumes
        final int maxLen = f.length();
        int end = start + Math.min(maxLen, length);

        if (isTrimStrings()) {
            // remove white spaces
            final byte[] data = buffer.array();
            while (end > start && (((int) data[end - 1]) & 0xff) <= 0x20) {
                end--;
            }
            while (start < end && (((int) data[start]) & 0xff) <= 0x20) {
                start++;
            }
        }

        this.parsedRecord.setField(fieldId, start, end - start);

        buffer.position(buffer.position() + length);
    }

    public static int getLength(ByteBuffer buffer, VwdFieldDescription.Field f) {
        switch (f.mdpsType()) {
            case VLLGSTRING:
                return buffer.getChar();
            case VLSHSTRING:
                return ((int) buffer.get()) & 0xff;
            default:
                return f.length();
        }
    }

    private void parseTrack(ByteBuffer buffer) {
        int length = buffer.get() & 0xff;
        if (TRACK_ID == 0 || length > (255 - TRACK_ENTRY_LENGTH)) {
            buffer.position(buffer.position() + length);
            return;
        }
        final byte[] track = new byte[length + TRACK_ENTRY_LENGTH];
        buffer.get(track, 0, length);
        expandTrack(track, TRACK_ID);
        this.parsedRecord.setTrack(track);
    }
}
