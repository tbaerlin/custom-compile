/*
 * EodFields.java
 *
 * Created on 11.01.13 12:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodFields implements EodItem {

    private static final Int2ObjectSortedMap<byte[]> TMP_MAP = new Int2ObjectAVLTreeMap<>();

    private byte[] data;

    private final List<EodField> fields = new ArrayList<>();

    private BitSet bitSet = new BitSet();

    private boolean loaded = false;

    public void withData(byte[] data) {
        clear();
        this.data = data;
    }

    private void clear() {
        this.data = null;
        this.bitSet.clear();
        this.loaded = false;
        for (EodField field : fields) {
            field.clear();
        }
    }

    private void loadIfNecessary() {
        if (this.loaded) {
            return;
        }
        if (null != this.data) {
            final ByteBuffer bb = ByteBuffer.wrap(this.data);
            final int cnt = HistoryUtil.fromUnsignedByte(bb.get());
            for (int i = 0; i < cnt; i++) {
                final int len24Field8 = bb.getInt();
                final int field = HistoryUtil.fromUnsignedByte(EodUtil.decodeField(len24Field8));
                final int len = EodUtil.decodeFieldLength(len24Field8);
                setOrGetField(field).withData(field, this.data, bb.position(), len);
                bb.position(bb.position() + len);
            }
        }
        this.loaded = true;
    }

    EodField setOrGetField(int field) {
        this.bitSet.set(field - 1);
        if (this.fields.size() < field) {
            for (int j = this.fields.size(); j < field; j++) {
                this.fields.add(new EodField(j + 1));
            }
        }
        return this.fields.get(field - 1);
    }

    @Override
    public byte[] getBytes(boolean isPatch, int pivot) {
        if (isPatch) {
            throw new IllegalArgumentException("EodFields does not support serialization as patch");
        }

        if (pivot == 0 && this.bitSet.isEmpty()) {
            return null == this.data ? EodUtil.EMPTY_BA : this.data;
        }

        loadIfNecessary();
        return toBytes(this.bitSet, this.fields, pivot);
    }

    static byte[] toBytes(BitSet bs, List<EodField> fields, int pivot) {
        try {
            int len = 0;
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                final byte[] bytes = fields.get(i).getBytes(pivot);
                if (null != bytes && bytes.length > 0) {
                    len += bytes.length;
                    TMP_MAP.put((i + 1), bytes);
                }
            }

            if (len > 0) {
                final ByteBuffer bb = ByteBuffer.allocate(len + TMP_MAP.size() * 4 + 1);
                HistoryUtil.ensureUnsignedByte(TMP_MAP.size());
                bb.put((byte) TMP_MAP.size());
                for (Int2ObjectMap.Entry<byte[]> entry : TMP_MAP.int2ObjectEntrySet()) {
                    bb.putInt(EodUtil.encodeLengthField(entry.getValue().length, (byte) entry.getIntKey()));
                    bb.put(entry.getValue());
                }
                return bb.array();
            }

            return EodUtil.EMPTY_BA;
        } finally {
            TMP_MAP.clear();
        }
    }

    @Override
    public <T extends EodItem> void merge(T another, boolean extension) {
        if (another instanceof EodPrices) {
            final EodPrices price = (EodPrices) another;
            loadIfNecessary();
            price.update(this, extension); // extension only comes from EodPrices
        }
        else if (another instanceof EodFields) {
            final EodFields efs = (EodFields) another;
            efs.loadIfNecessary();
            loadIfNecessary();

            for (int i = this.bitSet.nextSetBit(0); i >= 0; i = this.bitSet.nextSetBit(i + 1)) {
                final EodField ef = this.fields.get(i);
                if (efs.containsField(ef.getFieldId())) {
                    ef.merge(efs.getField(ef.getFieldId()));
                }
            }

            // add new field if any
            for (int i = efs.bitSet.nextSetBit(0); i >= 0; i = efs.bitSet.nextSetBit(i + 1)) {
                final EodField ef = efs.fields.get(i);
                if (!containsField(ef.getFieldId())) {
                    setOrGetField(ef.getFieldId()).with(ef);
                }
            }
        }
        else {
            throw new UnsupportedOperationException("not implemented for: " + another.getClass());
        }
    }

    private boolean containsField(int fieldId) {
        return this.bitSet.get(fieldId - 1);
    }

    private EodField getField(int fieldId) {
        if (containsField(fieldId)) {
            return this.fields.get(fieldId - 1);
        }
        else {
            return null;
        }
    }

    void removePrice(int date) {
        for (int i = this.bitSet.nextSetBit(0); i >= 0; i = this.bitSet.nextSetBit(i + 1)) {
            this.fields.get(i).setPrice(date, EodUtil.EMPTY_BA);
        }
    }

    void updatePrices(int date, Int2ObjectSortedMap<byte[]> prices, boolean extension) {
        for (int i = this.bitSet.nextSetBit(0); i >= 0; i = this.bitSet.nextSetBit(i + 1)) {
            final int fieldId = i + 1;
            if (prices.containsKey(fieldId)) {
                this.fields.get(i).setPrice(date, prices.get(fieldId));
            }
            else if (!extension) {
                this.fields.get(i).setPrice(date, EodUtil.EMPTY_BA);
            }
        }

        for (Int2ObjectMap.Entry<byte[]> entry : prices.int2ObjectEntrySet()) {
            if (!this.bitSet.get(entry.getIntKey() - 1)) {
                setOrGetField(entry.getIntKey()).setPrice(date, entry.getValue());
            }
        }
    }

    List<EodField> getFields() {
        loadIfNecessary();
        final List<EodField> ret = new ArrayList<>(this.bitSet.cardinality());
        for (int i = this.bitSet.nextSetBit(0); i >= 0; i = this.bitSet.nextSetBit(i + 1)) {
            ret.add(this.fields.get(i));
        }

        return ret;
    }
}
