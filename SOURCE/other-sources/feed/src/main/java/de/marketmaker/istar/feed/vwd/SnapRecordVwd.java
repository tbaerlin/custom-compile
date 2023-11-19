/*
 * SnapRecordVwd.java
 *
 * Created on 18.11.2004 16:20:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.nio.charset.Charset;

import de.marketmaker.istar.domain.data.AbstractSnapRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.snap.SnapDataDefault;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;

import static de.marketmaker.istar.feed.vwd.IndexAndOffsetFactoryVwd.INLINE_STRING_LENGTH;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapRecordVwd extends AbstractSnapRecord {
    protected static final long serialVersionUID = 2381685436540467633L;

    // copied from SnapDataDefault, needed for zero fields
    private static final byte[] ZERO_FLAGS = new byte[]{1, 4, 16, 64};

    /**
     * If != 0, this object is based on the new SnapDataDefault and has to treat longer strings
     * specially. If the value is negative, the zero flags are not evaluated and the last value
     * for zeroed fields is returned.
     */
    private final int zeroFlagsOffset;

    private final int nominalDelayInSeconds;

    /**
     * Sometimes, Strings in a record are encoded with a specific charset; in order to be able
     * to decode those strings, we need to use the respective charset.
     */
    // not Serializable, so make it transient and provide readResolve method
    private transient Charset charset;

    private SnapRecordVwd(int[] indexArray, int[] offsetArray, byte[] data, int zeroFlagsOffset,
            int nominalDelayInSeconds) {
        super(indexArray, offsetArray, data);
        this.zeroFlagsOffset = zeroFlagsOffset;
        this.nominalDelayInSeconds = nominalDelayInSeconds;
        this.charset = inferCharset();
    }

    public SnapRecordVwd(int[] indexArray, int[] offsetArray, byte[] data) {
        this(indexArray, offsetArray, data, 0, 0);
    }

    public SnapRecordVwd(int[] indexArray, int[] offsetArray, byte[] data, int nominalDelayInSeconds) {
        this(indexArray, offsetArray, data, 0, nominalDelayInSeconds);
    }

    public SnapRecordVwd(SnapDataDefault sdd, int nominalDelayInSeconds) {
        this(sdd, false, nominalDelayInSeconds);
    }

    public SnapRecordVwd(SnapDataDefault sdd, boolean lastValueForNulledFields, int nominalDelayInSeconds) {
        this(sdd.getIndexAndOffset().getIndexArray(), sdd.getIndexAndOffset().getOffsetArray(),
                sdd.getData(true),
                lastValueForNulledFields ? -sdd.getIndexAndOffset().getSize() : sdd.getIndexAndOffset().getSize(),
                nominalDelayInSeconds);
    }

    private Object readResolve() {
        this.charset = inferCharset();
        return this;
    }

    @Override
    public int getNominalDelayInSeconds() {
        return this.nominalDelayInSeconds;
    }

    /**
     * @see <a href="http://electra2/wiki/doku.php?id=technik:cps:documentation:news">Doku</a>
     * @return Charset for text fields or null if none is defined
     */
    private Charset inferCharset() {
        final int i = getFieldIndex(VwdFieldDescription.NDB_ContentDescriptor.id());
        if (i < 0) {
            return DEFAULT_CHARSET;
        }
        return SnapRecordUtils.getCharset(this);
    }

    public Charset getCharset() {
        return this.charset;
    }

    protected int getFieldByName(String fieldname) {
        final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(fieldname);
        return field != null ? field.id() : -1;
    }

    protected SnapField createSnapField(int fieldid, Object value) {
        return new SnapFieldVwd(fieldid, value);
    }

    protected boolean isBoolean(int fieldid) {
        return false;
    }

    protected boolean isInt(int fieldid) {
        final VwdFieldDescription.Field field = VwdFieldDescription.getField(fieldid);
        return field.type() == VwdFieldDescription.Type.UINT
                || field.type() == VwdFieldDescription.Type.USHORT
                || field.type() == VwdFieldDescription.Type.DATE
                || field.type() == VwdFieldDescription.Type.TIME;
    }

    protected boolean isLong(int fieldid) {
        final VwdFieldDescription.Field field = VwdFieldDescription.getField(fieldid);
        return field.type() == VwdFieldDescription.Type.PRICE
                || field.type() == VwdFieldDescription.Type.TIMESTAMP;
    }

    protected boolean isString(int fieldid) {
        final VwdFieldDescription.Field field = VwdFieldDescription.getField(fieldid);
        return field.type() == VwdFieldDescription.Type.STRING;
    }

    protected String readString(int index, int offset) {
        if (this.zeroFlagsOffset == 0 ||
                VwdFieldDescription.getField(getFieldids()[index]).length() <= INLINE_STRING_LENGTH) {
            return super.readString(index, offset);
        }
        final int stringOffset = getOffset(offset);
        final int sLen = getLength(stringOffset);
        return readStringStopAt0(stringOffset + numBytesForLength(sLen), sLen);
    }

    @Override
    protected String readStringStopAt0(int offset, int length) {
        if (this.charset == null || length == 0) {
            return super.readStringStopAt0(offset, length);
        }
        return readStringStopAt0(offset, length, this.charset);
    }

    protected boolean isZero(int index) {
        return this.zeroFlagsOffset > 0 && isZeroAt(index);
    }

    // copied from SnapDataDefault, needed to decode strings that are not inlined
    private int getOffset(int n) {
        return getData()[n] << 8 | (getData()[n + 1] & 0xFF);
    }

    // copied from SnapDataDefault, needed to decode strings that are not inlined
    private int getLength(int pos) {
        int n = getData()[pos++];
        int x = 0;
        while (n >= 0) {
            x <<= 7;
            x |= n;
            n = getData()[pos++];
        }
        x <<= 7;
        x |= (n & 0x7f);

        return x;
    }

    // copied from SnapDataDefault, needed to decode strings that are not inlined
    private int numBytesForLength(int n) {
        if (n < 0x80) {
            return 1;
        }
        return (n < 0x4000) ? 2 : 3;
    }

    // copied from SnapDataDefault, needed for zero fields
    private boolean isZeroAt(int index) {
        return (getData()[this.zeroFlagsOffset + (index >> 2)] & ZERO_FLAGS[index & 0x3]) != 0;
    }
}
