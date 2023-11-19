/*
 * SnapRecordTranscoder.java
 *
 * Created on 13.02.13 13:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Sometimes, it may be necessary to change the order of a snap field. Keeping the old order around
 * for a while is not a problem for data consumers, as that can simply map both old and new order
 * value to the same snap field. The data provider, however, has to change the data layout and move
 * fields to a position that reflects the current, i.e. main order. This class does that.
 * <p>
 * In addition to reordering fields, they can also be removed. To do that, use
 * <pre>-Dsnap.transcode=&lt;oid>=0(,&lt;oid>=0)*</pre>
 * Or, as a special option to remove all non-dynamic fields:
 * <pre>-Dsnap.transcode=remove_static</pre>
 * </p>
 *
 * @author oflege
 */
class SnapRecordTranscoder extends FieldDataBuilder {

    static class Builder {
        private final List<Item> items = new ArrayList<>();

        Builder() {
            final String transcodes = System.getProperty("snap.transcode");
            if ("remove_static".equals(transcodes)) {
                final BitSet bs = VwdFieldOrder.getNonDynamicOrders();
                for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                    this.items.add(new Item(i, 0));
                }
                return;
            }
            if (transcodes != null) {
                for (String transcode : transcodes.split(",")) {
                    final String[] fromTo = transcode.split("=");
                    this.items.add(new Item(Integer.parseInt(fromTo[0]), Integer.parseInt(fromTo[1])));
                }
            }
        }

        void add(int oldOrder, int newOrder) {
            this.items.add(new Item(oldOrder, newOrder));
        }

        SnapRecordTranscoder build() {
            return new SnapRecordTranscoder(this.items);
        }
    }

    private static class Item {
        private final int oldOrder;

        private final int newOrder;

        private final int type;

        private long data;

        private byte[] string;

        private Item(int oldOrder, int newOrder) {
            this.oldOrder = oldOrder;
            this.newOrder = newOrder;
            // newOrder = 0: we'll remove the field, don't need type for that
            this.type = newOrder > 0 ? FieldDataUtil.getType(VwdFieldOrder.getField(newOrder)) : 0;
        }

        boolean isDelete() {
            return this.newOrder == 0;
        }

        @Override
        public String toString() {
            return this.oldOrder + "->" + this.newOrder;
        }
    }

    private static final Comparator<Item> BY_OLD_ORDER = (o1, o2) -> o1.oldOrder - o2.oldOrder;

    private static final Comparator<Item> BY_NEW_ORDER = (o1, o2) -> o1.newOrder - o2.newOrder;

    // ordered by old order id
    private final Item[] inItems;

    // ordered by new order id, only contains fields present in the source data by old order id
    private final Item[] outItems;

    private SnapRecordTranscoder(List<Item> items) {
        super(items.isEmpty() ? 0 : 4096);
        if (!items.isEmpty()) {
            this.inItems = items.toArray(new Item[items.size()]);
            if (this.inItems.length > 1) {
                Arrays.sort(this.inItems, BY_OLD_ORDER);
            }
            this.outItems = new Item[items.size()];
        }
        else {
            this.inItems = null;
            this.outItems = null;
        }
    }

    @Override
    public String toString() {
        if (this.inItems == null) {
            return getClass().getSimpleName() + "[]";
        }
        return getClass().getSimpleName() + Arrays.toString(this.inItems);
    }

    byte[] transcode(byte[] data) {
        return (this.inItems != null) ? doTranscode(data) : data;
    }

    private byte[] doTranscode(byte[] data) {
        final BufferFieldData fd = new BufferFieldData(data);

        final int m = saveOutFields(fd);

        if (m == 0) {
            return data;
        }

        fd.rewind();

        reset();

        int i = 0;
        int n = 0;
        while (n < m && this.outItems[n].isDelete()) {
            n++;
        }
        for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
            while (i < inItems.length && inItems[i].oldOrder < id) {
                i++;
            }
            if (i < inItems.length && inItems[i].oldOrder == id) {
                i++;
                fd.skipCurrent();
                continue;
            }
            while (n < m && this.outItems[n].newOrder <= id) {
                put(this.outItems[n++]);
            }
            if (id != this.lastOrder) {
                addFieldToBuffer(fd);
            }
            else {
                fd.skipCurrent();
            }
        }
        while (n < m) {
            put(this.outItems[n++]);
        }

        return asArray();
    }

    private void put(Item outItem) {
        switch (outItem.type) {
            case FieldData.TYPE_INT:
                putIntFid(outItem.newOrder);
                putInt((int) outItem.data);
                break;
            case FieldData.TYPE_PRICE:
                putPriceFid(outItem.newOrder);
                putPrice(outItem.data);
                break;
            case FieldData.TYPE_STRING:
                putStringFid(outItem.newOrder);
                putString(outItem.string);
                break;
            case FieldData.TYPE_TIME:
                putTimeFid(outItem.newOrder);
                putInt((int) outItem.data);
                break;
        }
    }

    private int saveOutFields(BufferFieldData fd) {
        int i = 0;
        int n = 0;
        NEXT:
        for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
            if (id < this.inItems[i].oldOrder) {
                fd.skipCurrent();
                continue;
            }
            if (id == this.inItems[i].oldOrder) {
                switch (fd.getType()) {
                    case FieldData.TYPE_INT:
                        if (VwdFieldOrder.getField(fd.getId()).type() == VwdFieldDescription.Type.UINT) {
                            inItems[i].data = fd.getUnsignedInt();
                        } else {
                            inItems[i].data = fd.getInt();
                        }
                        break;
                    case FieldData.TYPE_TIME:
                        inItems[i].data = fd.getInt();
                        break;
                    case FieldData.TYPE_PRICE:
                        inItems[i].data = FieldDataUtil.getPrice(fd);
                        break;
                    case FieldData.TYPE_STRING:
                        inItems[i].string = fd.getBytes();
                        break;
                    default:
                }
                outItems[n++] = inItems[i];
            }
            else {
                fd.skipCurrent();
            }
            do {
                i++;
                if (i == inItems.length) {
                    break NEXT;
                }
            } while (id > this.inItems[i].oldOrder);
        }

        if (n > 1) {
            Arrays.sort(this.outItems, 0, n, BY_NEW_ORDER);
        }
        return n;
    }
}
