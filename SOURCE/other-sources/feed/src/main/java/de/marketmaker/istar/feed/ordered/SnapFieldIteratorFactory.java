/*
 * SnapFieldIteratorFactory.java
 *
 * Created on 17.10.12 11:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Some data queries specify a set of fields that need to be extracted from a FieldData object.
 * This factory allows to specify those fields once and then create Iterators for FieldData
 * objects that will return only the specified fields.
 * @author oflege
 */
public class SnapFieldIteratorFactory {

    /**
     * Create a factory that, when {@link #iterator(BufferFieldData)} is called, returns an Iterator
     * that will iterate over the specified vwdFieldIds
     *
     * @param fieldsWithNullEscape vwd field ids of those price fields for which the latest non-zero
     * value should be returned if the current value is zero; may be null
     * @param vwdFieldIds fields supported by iterators
     * @return a factory
     */
    public static SnapFieldIteratorFactory create(BitSet fieldsWithNullEscape, int... vwdFieldIds) {
        final int[] tmp = new int[vwdFieldIds.length];
        int n = 0;
        NEXT: for (int i = 0; i < vwdFieldIds.length; i++) {
            for (int j = 0; j < i; j++) {
                if (vwdFieldIds[i] == vwdFieldIds[j]) {
                    continue NEXT;
                }
            }
            tmp[n] = vwdFieldIds[i] + (VwdFieldOrder.getOrder(vwdFieldIds[i]) << 16);
            n++;
        }

        Arrays.sort(tmp, 0, n);
        return new SnapFieldIteratorFactory(fieldsWithNullEscape, Arrays.copyOf(tmp, n));
    }

    /**
     * Index of all fields that are supposed to be returned; each int contains the vwd-FieldId
     * in the lower 16 bits and the field's order in the upper 16 bits
     */
    private final int[] index;

    private final boolean[] escapeNulls;

    private SnapFieldIteratorFactory(BitSet fieldsWithNullEscape, int[] index) {
        this.index = index;
        this.escapeNulls = new boolean[this.index.length];
        if (fieldsWithNullEscape != null) {
            for (int i = 0; i < index.length; i++) {
                this.escapeNulls[i] = fieldsWithNullEscape.get(index[i] & 0xFFFF);
            }
        }
    }

    public Iterator<SnapField> iterator(OrderedUpdate update) {
        return iterator(update.getFieldData());
    }

    /**
     * @param fd ordered field data
     * @return Iterator over fd's fields that will only return those fields whose ids are
     * contained in the set of fields used to initialize this factory. The internal state of
     * fd will be modified while iterating over the fields. Clients must not ensure that fd has
     * been reset before it is passed to this method and must rewind it again if they want to
     * reuse it after the iterator finished
     */
    public Iterator<SnapField> iterator(final BufferFieldData fd) {
        if (index.length == 0) {
            return Collections.emptyIterator();
        }
        return new Iterator<SnapField>() {
            private int n = 0;

            private SnapField next = advance();

            private SnapField advance() {
                int order = getOrder();
                for (int oid = fd.readNext(); oid != 0; oid = fd.readNext()) {
                    while (oid > order) {
                        if (++this.n == index.length) {
                            return null;
                        }
                        order = getOrder();
                    }
                    if (oid == order) {
                        final int fid = getVwdId();
                        return new OrderedSnapRecord.Field(fid,
                                OrderedSnapRecord.getFieldValue(fd, escapeNulls[n]));
                    }
                    fd.skipCurrent();
                }
                return null;
            }

            private int getOrder() {
                return index[n] >> 16;
            }

            private int getVwdId() {
                return index[n] & 0xFFFF;
            }

            @Override
            public boolean hasNext() {
                return this.next != null;
            }

            @Override
            public SnapField next() {
                if (this.next == null) {
                    throw new NoSuchElementException();
                }
                final SnapField result = this.next;
                this.next = advance();
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
