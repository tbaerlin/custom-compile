/*
 * SnapRecord.java
 *
 * Created on 25.10.2004 15:19:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import de.marketmaker.istar.common.util.SimpleBitSet;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.IndexAndOffsetFactoryVwd;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static de.marketmaker.istar.feed.vwd.IndexAndOffsetFactoryVwd.INLINE_STRING_LENGTH;
import static de.marketmaker.istar.feed.vwd.IndexAndOffsetFactoryVwd.PRICE_LENGTH;

/**
 * Stores snap data in a byte array that is organized as follows:
 * <pre>
 * |value-0|value-1|...|value-n|flags|string-0|string-1|...|string-m|
 * </pre>
 * <dl>
 * <dt>value-i</dt>
 * <dd>is the data for the i-th field in the iao's index array and starts at the offset given
 * by the iao's i-th element in the offset array. If however, the field contains a String with
 * a maximum length &gt; {@link de.marketmaker.istar.feed.vwd.IndexAndOffsetFactoryVwd#INLINE_STRING_LENGTH},
 * then the value is just a short that contains the offset of the string value in the data array</dd>
 * <dt>flags</dt>
 * <dd>bits that indicate whether a field has been updated or whether it has been set to 0</dd>
 * <dt>string-i</dt>
 * <dd>string values that have not been inlined. Each string starts with a length in the first
 * one, two, or three bytes (stop-bit encoded) and then the string value. The length defines the
 * maximum length of a string that can be stored in this field. To store a longer string, the data
 * array will have to be expanded. If the string value
 * is shorter than the length, the first byte after the value will be 0.</dd>
 * </dl>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapDataDefault implements SnapData {
    private static final byte[] UPDATED_FLAGS = new byte[]{2, 8, 32, (byte) 128};

    private static final byte[] NOT_UPDATED_MASKS = new byte[]{~2, ~8, ~32, (byte) ~128};

    private static final byte[] ZERO_FLAGS = new byte[]{1, 4, 16, 64};

    private static final byte[] NOT_ZERO_MASKS = new byte[]{~1, ~4, ~16, ~64};

    private static int getNumFlagBytes(IndexAndOffset iao) {
        final int nf = iao.getNumFields();
        return (nf / 4) + (((nf % 4) == 0) ? 0 : 1);
    }

    private static int getStringOffset(IndexAndOffset iao) {
        return iao.getSize() + getNumFlagBytes(iao);
    }

    private byte[] data = null;

    private IndexAndOffset iao = null;

    public SnapDataDefault() {
    }

    public void copy(int sourceFieldid, int targetFieldid) {
        int sourceIndex = this.iao.getIndex(sourceFieldid);
        if (sourceIndex < 0) {
            sourceIndex = expand(sourceFieldid, 0);
        }
        int targetIndex = this.iao.getIndex(targetFieldid);
        if (targetIndex < 0) {
            targetIndex = expand(targetFieldid, 0);
        }

        final int offsetSource = this.iao.getOffsetByIndex(sourceIndex);
        final int offsetTarget = this.iao.getOffsetByIndex(targetIndex);
        final int length = this.iao.getLengthByIndex(sourceIndex);
        System.arraycopy(this.data, offsetSource, this.data, offsetTarget, length);

        setUpdatedAt(targetIndex);
    }

    public void copyValue(VwdFieldDescription.Field field, byte[] store) {
        int index = this.iao.getIndex(field.id());
        if (index < 0) {
            index = expand(field.id(), 0);
        }
        final int offset = this.iao.getOffsetByIndex(index);
        System.arraycopy(this.data, offset, store, 0, field.length());
    }

    public boolean differs(int fieldId, byte[] store) {
        int n = this.iao.getIndex(fieldId);
        if (n < 0) {
            n = expand(fieldId, store.length);
        }
        int offset = this.iao.getOffsetByIndex(n);

        final int len = store.length;
        for (int i = 0; i < len; i++) {
            if (this.data[offset + i] != store[i]) {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        this.iao = null;
        this.data = null;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public byte[] getData(boolean copy) {
        if (this.data != null && copy) {
            return Arrays.copyOf(this.data, this.data.length);
        }
        return this.data;
    }

    public IndexAndOffset getIndexAndOffset() {
        return iao;
    }

    public int getInt(int fieldId, boolean withLastValueForNullField) {
        if (this.iao == null) {
            return 0;
        }
        final int index = this.iao.getIndex(fieldId);
        if (index < 0) {
            return 0;
        }
        if (!withLastValueForNullField) {
            if (isZeroAt(index)) {
                return 0;
            }
        }
        final ByteBuffer bb = ByteBuffer.wrap(this.data);
        return bb.getInt(iao.getOffsetByIndex(index));
    }

    public long getLong(int fieldId, boolean withLastValueForNullField) {
        if (this.iao == null) {
            return 0L;
        }
        final int index = this.iao.getIndex(fieldId);
        if (index < 0) {
            return 0L;
        }
        if (!withLastValueForNullField) {
            if (isZeroAt(index)) {
                return 0L;
            }
        }
        final ByteBuffer bb = (ByteBuffer) ByteBuffer.wrap(this.data).position(iao.getOffsetByIndex(index));
        return MdpsFeedUtils.getMdpsPrice(bb) & 0xFFFFFFFFFFL;
    }

    public byte[] getString(int fieldId) {
        if (this.iao == null) {
            return null;
        }
        final int index = this.iao.getIndex(fieldId);
        if (index < 0) {
            return null;
        }
        final VwdFieldDescription.Field field = VwdFieldDescription.getField(fieldId);
        if (isLongString(field)) {
            return getLongString(index);
        } else if (field.type() == VwdFieldDescription.Type.STRING) {
            return getString(this.iao.getOffsetByIndex(index), this.iao.getLengthByIndex(index));
        }
        return null;
    }

    private byte[] getLongString(int index) {
        final int offset = getStringOffset(this.iao.getOffsetByIndex(index));
        final int len = getLength(offset);
        return getString(offset + numBytesForLength(len), len);
    }

    public void init(IndexAndOffset indexAndOffset, ParsedRecord pr) {
        final int nf = indexAndOffset.getNumFields();
        int sOffset = getStringOffset(indexAndOffset);
        int n = sOffset;

        for (int i = 0; i < nf; i++) {
            final int fid = indexAndOffset.getFieldid(i);
            final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
            if (isLongString(field)) {
                final int sLen = pr.getLength(fid);
                n += (sLen + numBytesForLength(sLen));
            }
        }

        this.data = new byte[n];
        this.iao = indexAndOffset;

        if (n != sOffset) {
            for (int i = 0; i < nf; i++) {
                final int fid = indexAndOffset.getFieldid(i);
                final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
                if (isLongString(field)) {
                    putOffset(indexAndOffset.getOffsetByIndex(i), sOffset);
                    final int sLen = pr.getLength(fid);
                    putLength(sOffset, sLen);
                    sOffset += numBytesForLength(sLen);
                    sOffset += sLen;
                }
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public void init(IndexAndOffset indexAndOffset, byte[] data) {
        if (this.iao == null) {
            this.iao = indexAndOffset;
            this.data = data;
        } else {
            mergeWithBase(indexAndOffset, data);
        }
    }

    /**
     * This is what happens in a push situation: after registering the key, some data arrived
     * <em>before<em> the snapshot with the complete data was available. So we take the snapshot
     * in data and copy all fields into it.
     *
     * @param indexAndOffset iao for data
     * @param data           snap
     */
    private void mergeWithBase(IndexAndOffset indexAndOffset, byte[] data) {
        final SnapDataDefault tmp = new SnapDataDefault();
        tmp.init(indexAndOffset, data);

        final int[] fids = this.iao.getIndexArray();
        for (int i = fids.length - 1; i-- > 0; ) {
            final int fid = fids[i];
            final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
            final VwdFieldDescription.Type type = field.type();
            switch (type) {
                case STRING:
                    if (isLongString(field)) {
                        tmp.setString(fid, ByteBuffer.wrap(getString(fid)));
                    } else {
                        tmp.set(fid, ByteBuffer.wrap(this.data, this.iao.getOffset(fid), field.length()));
                    }
                    break;
                case PRICE:
                    tmp.set(fid, ByteBuffer.wrap(this.data, this.iao.getOffset(fid), PRICE_LENGTH));
                    break;
                default:
                    tmp.set(fid, ByteBuffer.wrap(this.data, this.iao.getOffset(fid), 4));
                    break;
            }
        }

        this.data = tmp.data;
        this.iao = tmp.iao;
    }

    public boolean isInitialized() {
        return this.iao != null;
    }

    public SnapRecord toSnapRecord(int nominalDelayInSeconds) {
        if (!isInitialized()) {
            return NullSnapRecord.INSTANCE;
        }
        return new SnapRecordVwd(this, nominalDelayInSeconds);
    }

    public void removeField(int id) {
        if (!isInitialized()) {
            return;
        }

        final int index = this.iao.getIndex(id);
        if (index < 0) {
            return;
        }

        final int[] indexArray = this.iao.getIndexArray();
        final SimpleBitSet toKeep = new SimpleBitSet(indexArray.length);
        toKeep.set(0, indexArray.length - 1);
        toKeep.clear(index);

        shrink(toKeep);
    }

    public void set(int fieldId, ByteBuffer buffer) {
        int index = this.iao.getIndex(fieldId);
        if (index < 0) {
            index = expand(fieldId, 0);
        }
        final int offset = this.iao.getOffsetByIndex(index);

        if (buffer != null) {
            int length = this.iao.getOffsetArray()[index + 1] - offset;
            buffer.get(this.data, offset, Math.min(length, buffer.remaining()));
        }

        setUpdatedAt(index);
        if (buffer == null) {
            setZeroAt(index);
        } else {
            clearZeroAt(index);
        }
    }

    public void set(int fieldId, byte[] store) {
        int n = this.iao.getIndex(fieldId);
        if (n < 0) {
            n = expand(fieldId, store.length);
        }
        final int offset = this.iao.getOffsetByIndex(n);
        System.arraycopy(store, 0, this.data, offset, store.length);
        setUpdatedAt(n);
    }

    public void setString(int fieldId, ByteBuffer buffer) {
        int index = this.iao.getIndex(fieldId);
        if (index < 0) {
            index = expand(fieldId, buffer.remaining());
        }
        final int offset = this.iao.getOffsetByIndex(index);

        int sPos = getStringOffset(offset);
        final int curLen = getLength(sPos);
        final int len = buffer.remaining();
        if (len <= curLen) {
            sPos += numBytesForLength(curLen);
            buffer.get(this.data, sPos, buffer.remaining());
            if (len != curLen) {
                this.data[sPos + len] = 0;
            }
        } else {
            // new length is larger, need to expand our data array
            final int curTotalLen = curLen + numBytesForLength(curLen);
            final int newTotalLen = len + numBytesForLength(len);
            final byte[] tmp = new byte[this.data.length - curTotalLen + newTotalLen];
            System.arraycopy(this.data, 0, tmp, 0, sPos);
            System.arraycopy(this.data, sPos + curTotalLen, tmp, sPos + newTotalLen, this.data.length - sPos - curTotalLen);
            this.data = tmp;

            // write new string at same position as previous
            putLength(sPos, len);
            buffer.get(this.data, sPos + numBytesForLength(len), buffer.remaining());

            // adjust pointers to all strings stored after the new one
            final int nf = this.iao.getNumFields();
            for (int i = 0; i < nf; i++) {
                final int fid = this.iao.getFieldid(i);
                final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
                if (isLongString(field)) {
                    final int curPos = getStringOffset(this.iao.getOffsetByIndex(i));
                    if (curPos > sPos) {
                        putOffset(this.iao.getOffsetByIndex(i), curPos + newTotalLen - curTotalLen);
                    }
                }
            }
        }

        setUpdatedAt(index);
    }

    public String toString() {
        if (!this.isInitialized()) {
            return "SnapDataDefault[]";
        }
        return new StringBuilder(200).append("SnapDataDefault")
                .append(new SnapRecordVwd(this, 0).getSnapFields())
                .toString();
    }

    boolean isNulled(int fieldId) {
        final int index = this.iao.getIndex(fieldId);
        return (index >= 0) && isZeroAt(index);
    }

    boolean isUpdated(int fieldId) {
        final int index = this.iao.getIndex(fieldId);
        return (index >= 0) && isUpdatedAt(index);
    }

    private int expand(int fieldId, int length) {
        final SnapDataDefault tmp = new SnapDataDefault();
        tmp.init(this.iao, this.data);
        final int oldStringOffset = tmp.getStringOffset();

        final VwdFieldDescription.Field field = VwdFieldDescription.getField(fieldId);
        final int flen;
        final int slen;
        if (isLongString(field)) {
            flen = 2;
            slen = length + numBytesForLength(length);
        } else {
            flen = IndexAndOffsetFactoryVwd.getLength(field);
            slen = 0;
        }

        this.iao = this.iao.expand(fieldId);
        final int newStringOffset = getStringOffset();
        this.data = new byte[newStringOffset + (this.data.length - oldStringOffset) + slen];

        final int newIndex = iao.getIndex(fieldId);
        final int newOffset = iao.getOffsetByIndex(newIndex);
        // copy data fields
        System.arraycopy(tmp.data, 0, this.data, 0, newOffset);
        System.arraycopy(tmp.data, newOffset, this.data, newOffset + flen, oldStringOffset - newOffset);

        // set flags
        final int[] indexArray = tmp.getIndexAndOffset().getIndexArray();
        for (int i = 0, j = 0, n = indexArray.length - 1; i < n; i++, j++) {
            if (i == newIndex) {
                j++;
            }

            if (tmp.isZeroAt(i)) {
                setZeroAt(j);
            }
            else {
                clearZeroAt(j);
            }

            if (tmp.isUpdatedAt(i)) {
                setUpdatedAt(j);
            }
            else {
                clearUpdatedAt(j);
            }
        }


        if (newStringOffset == this.data.length) {
            return newIndex;
        }

        // copy strings so that if field is also a string field it can be appended
        System.arraycopy(tmp.data, oldStringOffset, this.data, newStringOffset, tmp.data.length - oldStringOffset);

        // adjust string offsets
        final int nf = this.iao.getNumFields();
        for (int i = 0; i < nf; i++) {
            final VwdFieldDescription.Field f = VwdFieldDescription.getField(this.iao.getFieldid(i));
            if (isLongString(f)) {
                if (f != field) {
                    final int oldPos = getStringOffset(this.iao.getOffsetByIndex(i));
                    putOffset(this.iao.getOffsetByIndex(i), oldPos + newStringOffset - oldStringOffset);
                } else {
                    // our new field is a string and needs to be prepared
                    final int fieldStringOffset = this.data.length - slen;
                    putOffset(this.iao.getOffsetByIndex(i), fieldStringOffset);
                    putLength(fieldStringOffset, length);
                }
            }
        }
        return newIndex;
    }

    private int getLength(int pos) {
        int n = this.data[pos++];
        int x = 0;
        while (n >= 0) {
            x <<= 7;
            x |= n;
            n = this.data[pos++];
        }
        x <<= 7;
        x |= (n & 0x7f);

        return x;
    }

    private int getStringOffset(int n) {
        // for strings that are not inlined, the "normal" offset n contains a 2 byte pointer
        // to the real offset
        return this.data[n] << 8 | (this.data[++n] & 0xFF);
    }

    private byte[] getString(int offset, int length) {
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (this.data[i] == 0) {
                return Arrays.copyOfRange(this.data, offset, i);
            }
        }
        return Arrays.copyOfRange(this.data, offset, offset + length);
    }

    private int getStringOffset() {
        return getStringOffset(this.iao);
    }

    private boolean isLongString(VwdFieldDescription.Field field) {
        return field.type() == VwdFieldDescription.Type.STRING && field.length() > INLINE_STRING_LENGTH;
    }

    private boolean isUpdatedAt(int index) {
        return (this.data[this.iao.getSize() + (index >> 2)] & UPDATED_FLAGS[index & 0x3]) != 0;
    }

    private void setUpdatedAt(int index) {
        this.data[this.iao.getSize() + (index >> 2)] |= UPDATED_FLAGS[index & 0x3];
    }

    private void clearUpdatedAt(int index) {
        this.data[this.iao.getSize() + (index >> 2)] &= NOT_UPDATED_MASKS[index & 0x3];
    }

    private boolean isZeroAt(int index) {
        return (this.data[this.iao.getSize() + (index >> 2)] & ZERO_FLAGS[index & 0x3]) != 0;
    }

    private void setZeroAt(int index) {
        this.data[this.iao.getSize() + (index >> 2)] |= ZERO_FLAGS[index & 0x3];
    }

    private void clearZeroAt(int index) {
        this.data[this.iao.getSize() + (index >> 2)] &= NOT_ZERO_MASKS[index & 0x3];
    }

    /**
     * Returns the number of bytes needed to encode a length
     *
     * @param n to be encoded
     * @return number of bytes needed
     */
    private int numBytesForLength(int n) {
        if (n < 0x80) {
            return 1;
        }
        return (n < 0x4000) ? 2 : 3;
    }

    private void putLength(int offset, int i) {
        final int numBytes = numBytesForLength(i);
        switch (numBytes) {
            // intentional fallthrough!
            case 3:
                this.data[offset++] = (byte) ((i >> 14) & 0x7f);
            case 2:
                this.data[offset++] = (byte) ((i >> 7) & 0x7f);
            case 1:
                this.data[offset] = (byte) ((i & 0x7f) | 0x80);
                break;
            default:
                // cannot happen
                break;
        }
    }

    private void putOffset(int n, int value) {
        this.data[n] = (byte) (value >> 8);
        this.data[++n] = (byte) (value & 0xFF);
    }

    private void shrink(SimpleBitSet fieldsToKeep) {
        if (fieldsToKeep.isEmpty()) {
            this.iao = null;
            this.data = null;
            return;
        }

        int strLen = 0;
        for (int i = 0, nf = this.iao.getNumFields(); i < nf; i++) {
            final int fid = this.iao.getFieldid(i);
            final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
            if (fieldsToKeep.get(i) && isLongString(field)) {
                final int curPos = getStringOffset(this.iao.getOffsetByIndex(i));
                final int len = getLength(curPos);
                strLen += (len + numBytesForLength(len));
            }
        }

        final SnapDataDefault old = new SnapDataDefault();
        old.init(this.iao, this.data);

        this.iao = this.iao.shrink(fieldsToKeep);
        this.data = new byte[getStringOffset() + strLen];

        int sOff = getStringOffset();
        final int nf = this.iao.getNumFields();
        for (int i = 0; i < nf; i++) {
            final int fid = this.iao.getFieldid(i);
            final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
            final int oldIndex = old.iao.getIndex(fid);
            if (isLongString(field)) {
                putOffset(this.iao.getOffsetByIndex(i), sOff);
                final int oldPos = old.getStringOffset(old.iao.getOffsetByIndex(oldIndex));
                final int oldLen = old.getLength(oldPos);
                final int totalLen = oldLen + numBytesForLength(oldLen);
                System.arraycopy(old.data, oldPos, this.data, sOff, totalLen);
                sOff += totalLen;
                if (old.isUpdatedAt(oldIndex)) {
                    setUpdatedAt(i);
                }
            } else {
                final int oldOffset = old.iao.getOffsetByIndex(oldIndex);
                System.arraycopy(old.data, oldOffset, this.data, this.iao.getOffsetByIndex(i), this.iao.getLengthByIndex(i));
                if (old.isZeroAt(oldIndex)) {
                    setZeroAt(i);
                }
                if (old.isUpdatedAt(oldIndex)) {
                    setUpdatedAt(i);
                }
            }
        }
    }

    public void copy(SnapDataDefault rt) {
        this.iao = rt.iao;
        if (this.data.length >= rt.data.length) {
            System.arraycopy(rt.data, 0, this.data, 0, rt.data.length);
        } else {
            this.data = Arrays.copyOf(rt.data, rt.data.length);
        }
    }

    public static void main(String[] args) {
        final SnapDataDefault dataDefault = new SnapDataDefault();
        IndexAndOffsetFactoryVwd iaof = new IndexAndOffsetFactoryVwd();
        final IndexAndOffset iao = iaof.getIndexAndOffset(new int[]{26, 30, 31, 82, 109, 112, 212, 213, 753, 2147483647});
        dataDefault.init(iao, (ParsedRecord) null);
    }
}
