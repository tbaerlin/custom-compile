/*
 * SnapRecordScreener.java
 *
 * Created on 26.04.2005 08:28:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import java.io.Serializable;
import java.nio.ByteBuffer;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.HexDump;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "performance")
public class SnapRecordScreener implements SnapRecord, Serializable {
    static final long serialVersionUID = -7L;

    private int[] fieldids;
    private int[] offsets;
    private byte[] data;

    private List<ScreenerAlternative> altGroup;
    private List<ScreenerAlternative> altCountry;

    public SnapRecordScreener(int[] indexArray, int[] offsetArray, byte[] data, List<ScreenerAlternative> altGroup, List<ScreenerAlternative> altCountry) {
        this.fieldids = indexArray;
        this.offsets = offsetArray;
        this.data = data;
        this.altGroup = altGroup;
        this.altCountry = altCountry;
    }

    public SnapField getField(String fieldname) {
        final int fieldid = ScreenerFieldDescription.getFieldByName(fieldname);
        if (fieldid < 0) {
            return createEmptySnapField(fieldid);
        }
        return getField(fieldid);
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }

    /**
     * Returns the object representing the field with the given fieldId;
     *
     * @param fieldid specifies the field
     * @return field for the given id. If no field with the given id exists in this
     *         record, a field is returned for which {@link SnapField#isDefined} yields false.
     */
    public SnapField getField(int fieldid) {
        final int i = Arrays.binarySearch(this.fieldids, fieldid);
        return (i >= 0) ? getSnapField(i) : createEmptySnapField(fieldid);
    }

    private SnapField createEmptySnapField(int fieldid) {
        return new SnapFieldScreener(fieldid, null);
    }

    private SnapField getSnapField(int index) {
        final int offset = this.offsets[index];
        final ByteBuffer buffer = ByteBuffer.wrap(this.data);
        buffer.position(offset);

        switch (ScreenerFieldDescription.TYPES[this.fieldids[index]]) {
            case ScreenerFieldDescription.TYPE_DATE:
//                case ScreenerFieldDescription.TYPE_UNUM2:
            case ScreenerFieldDescription.TYPE_UNUM4:
//                case ScreenerFieldDescription.TYPE_TIME:
                final int ivalue = buffer.getInt();
                if (ivalue > Integer.MIN_VALUE) {
                    return new SnapFieldScreener(this.fieldids[index], ivalue);
                }
                break;
//                case ScreenerFieldDescription.TYPE_CHARV:
            case ScreenerFieldDescription.TYPE_UCHAR:
                int end = this.offsets[index + 1] - 1;

                while (this.data[end] == 0 && end > offset) {
                    end--;
                }

                final ByteString bs = ByteString.readWithLengthFrom(buffer,
                        end - offset + (this.data[end] == 0 ? 0 : 1));
                return new SnapFieldScreener(this.fieldids[index], bs.toString());
            case ScreenerFieldDescription.TYPE_PRICE:
//                case ScreenerFieldDescription.TYPE_TIMESTAMP:
                final long lvalue = buffer.getLong();
                if (lvalue > Long.MIN_VALUE) {
                    return new SnapFieldScreener(this.fieldids[index], lvalue);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown field type: this should never happen: " + ScreenerFieldDescription.TYPES[this.fieldids[index]] + " for field " + ScreenerFieldDescription.NAMES[this.fieldids[index]] + "/" + this.fieldids[index]);
        }

        return createEmptySnapField(this.fieldids[index]);
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

    public List<ScreenerAlternative> getAltGroup() {
        return this.altGroup;
    }

    public List<ScreenerAlternative> getAltCountry() {
        return this.altCountry;
    }
}
