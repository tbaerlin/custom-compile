/*
 * EodPrice.java
 *
 * Created on 11.01.13 12:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.nio.ByteBuffer;
import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.merger.provider.history.eod.BCD;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * Contains eod price data in following format:
 * <pre>
 *     yyyyMMdd(4)|fields(1)|field1(1)|price1|field2(1)|price2 ...
 * </pre>
 *
 * sorted on date descend. This also is the format for eod patch data.
 * <p>
 * A conversion is performed if asked for eod format of price data.
 * </p>
 *
 * @author zzhao
 */
class EodPrices implements EodItem {

    private byte[] data;

    EodPrices() {
        this.data = EodUtil.EMPTY_BA;
    }

    EodPrices(byte[] data) {
        this.data = data;
    }

    public void withData(byte[] data) {
        this.data = data;
    }

    static byte[] withTick(byte[] base, int date, Int2ObjectSortedMap<byte[]> data) {
        final int len = calcLength(data) + 4; // 4 bytes for date
        final ByteBuffer bb = ByteBuffer.allocate(base.length + len + 1); // 1 additional byte for fields count

        if (base.length == 0) {
            addPrice(date, data, bb);
        }
        else {
            final ByteBuffer dataBuf = ByteBuffer.wrap(base);
            boolean inserted = false;
            while (dataBuf.hasRemaining()) {
                final int ymd = dataBuf.getInt();
                if (ymd > date) {
                    // copy price for ymd into bb
                    bb.putInt(ymd);
                    transferPrice(dataBuf, bb);
                }
                else {
                    addPrice(date, data, bb);
                    if (ymd < date) {
                        bb.putInt(ymd);
                    }
                    else {
                        bypassPrice(dataBuf);
                    }
                    bb.put(dataBuf);
                    inserted = true;
                    break;
                }
            }

            if (!inserted) {
                addPrice(date, data, bb);
            }
        }
        return EodUtil.fromBuffer(bb);
    }

    private static void addPrice(int date, Int2ObjectSortedMap<byte[]> data, ByteBuffer bb) {
        bb.putInt(date);
        setPrice(bb, data);
    }

    private static void bypassPrice(ByteBuffer dataBuf) {
        final int fields = HistoryUtil.fromUnsignedByte(dataBuf.get());
        for (int i = 0; i < fields; i++) {
            dataBuf.get(); // field id
            byte b;
            do {
                b = dataBuf.get();
            } while (!BCD.isBoundary(b));
        }
    }

    private static void setPrice(ByteBuffer bb, Int2ObjectSortedMap<byte[]> data) {
        HistoryUtil.ensureUnsignedByte(data.size());
        bb.put((byte) data.size());

        for (Int2ObjectMap.Entry<byte[]> entry : data.int2ObjectEntrySet()) {
            bb.put((byte) entry.getIntKey());
            bb.put(entry.getValue());
        }
    }

    private static void transferPrice(ByteBuffer dataBuf, ByteBuffer bb) {
        final int fields = HistoryUtil.fromUnsignedByte(dataBuf.get());
        bb.put((byte) fields);

        for (int i = 0; i < fields; i++) {
            bb.put(dataBuf.get()); // field id
            do {
                bb.put(dataBuf.get());
            } while (!BCD.isBoundary(bb));
        }
    }

    private static int calcLength(Int2ObjectSortedMap<byte[]> data) {
        int len = 0;
        for (byte[] val : data.values()) {
            len += (1 + val.length); // field id 1 byte plus price bytes
        }
        return len;
    }

    @Override
    public byte[] getBytes(boolean isPatch, int pivot) {
        if (isPatch) {
            return pivot == 0 ? this.data : pivotPrices(pivot);
        }
        else {
            return toEodBytes();
        }
    }

    private byte[] pivotPrices(int pivot) {
        final ByteBuffer bb = ByteBuffer.allocate(this.data.length);
        try {
            BASE.reset(ByteBuffer.wrap(this.data));
            BASE.next();
            while (BASE.date >= pivot) {
                BASE.transferPrice(bb);
                BASE.next();
            }

            return Arrays.copyOfRange(bb.array(), 0, bb.position());
        } finally {
            BASE.reset(null);
        }
    }

    private static final EodFields EOD_FIELDS = new EodFields();

    private byte[] toEodBytes() {
        try {
            EOD_FIELDS.merge(this, false);
            return EOD_FIELDS.getBytes(false, 0);
        } finally {
            EOD_FIELDS.withData(null);
        }
    }

    /**
     * @param another
     * @param extension denotes if it's about EoD-Field extension
     * @param <T>
     */
    @Override
    public <T extends EodItem> void merge(T another, boolean extension) {
        if (!(another instanceof EodPrices)) {
            throw new IllegalArgumentException("EodPrice can only be merged with EodPrice");
        }
        final EodPrices price = (EodPrices) another;

        final ByteBuffer bb = ByteBuffer.allocate(this.data.length + price.data.length);

        try {
            BASE.reset(ByteBuffer.wrap(this.data));
            DELTA.reset(ByteBuffer.wrap(price.data));

            BASE.next();
            DELTA.next();

            while (BASE.date != 0 || DELTA.date != 0) {
                if (BASE.date == 0) {
                    if (!extension) {
                        DELTA.transferRest(bb);
                    }
                    // delta will only contain quote, date, and price for that extended field
                    // in case of eod field extension, don't transfer those delta
                    // if do transfer those data, it will mean it only contains that field's price
                    // since an absence of other fields means those fields' prices are removed
                    // in eod patch format
                    break;
                }
                else if (DELTA.date == 0) {
                    BASE.transferRest(bb);
                    break;
                }
                else {
                    if (BASE.date > DELTA.date) {
                        BASE.transferPrice(bb);
                        BASE.next();
                    }
                    else if (BASE.date < DELTA.date) {
                        if (!extension) {
                            DELTA.transferPrice(bb);
                        }
                        DELTA.next();
                    }
                    else {
                        if (extension) {
                            PRICES.clear();
                            BASE.transferPrice(PRICES);
                            DELTA.transferPrice(PRICES); // extension produced after patch, has priority
                            addPrice(BASE.date, PRICES, bb);
                        }
                        else {
                            DELTA.transferPrice(bb);
                        }
                        BASE.next();
                        DELTA.next();
                    }
                }
            }

            this.data = Arrays.copyOfRange(bb.array(), 0, bb.position());
        } finally {
            BASE.reset(null);
            DELTA.reset(null);
            PRICES.clear();
        }
    }

    public <T extends EodItem> void merge(T another, int pivot) {
        if (!(another instanceof EodPrices)) {
            throw new IllegalArgumentException("EodPrice can only be merged with EodPrice");
        }
        final EodPrices price = (EodPrices) another;
        final ByteBuffer bb = ByteBuffer.allocate(this.data.length + price.data.length);

        try {
            BASE.reset(ByteBuffer.wrap(this.data));
            DELTA.reset(ByteBuffer.wrap(price.data));

            BASE.next();
            DELTA.next();

            while (BASE.date != 0 || DELTA.date != 0) {
                if (BASE.date == 0) {
                    DELTA.transferRest(bb);
                    break;
                }
                else if (DELTA.date == 0) {
                    if (BASE.date >= pivot) {
                        BASE.transferPrice(bb);
                        BASE.next();
                    }
                    else {
                        break;
                    }
                }
                else {
                    if (BASE.date > DELTA.date) {
                        if (BASE.date >= pivot) {
                            BASE.transferPrice(bb);
                        }
                        BASE.next();
                    }
                    else if (BASE.date < DELTA.date) {
                        DELTA.transferPrice(bb);
                        DELTA.next();
                    }
                    else {
                        DELTA.transferPrice(bb);
                        BASE.next();
                        DELTA.next();
                    }
                }
            }

            this.data = Arrays.copyOfRange(bb.array(), 0, bb.position());
        } finally {
            BASE.reset(null);
            DELTA.reset(null);
            PRICES.clear();
        }
    }

    public void reduce(EodPrices price, EodHistoryArchiveISM.DateContext dc) {
        final ByteBuffer bb = ByteBuffer.allocate(this.data.length);

        try {
            BASE.reset(ByteBuffer.wrap(this.data));
            DELTA.reset(ByteBuffer.wrap(price.data));

            BASE.next();
            DELTA.next();

            while (BASE.date != 0) {
                if (DELTA.date == 0) {
                    BASE.transferRest(bb);
                    dc.update(BASE.date);
                    break;
                }
                else {
                    if (BASE.date > DELTA.date) {
                        BASE.transferPrice(bb);
                        dc.update(BASE.date);
                        BASE.next();
                    }
                    else if (BASE.date < DELTA.date) {
                        DELTA.next();
                    }
                    else {
                        // reduce
                        BASE.next();
                        DELTA.next();
                    }
                }
            }

            this.data = Arrays.copyOfRange(bb.array(), 0, bb.position());
        } finally {
            BASE.reset(null);
            DELTA.reset(null);
        }
    }

    public void updateDateContext(EodHistoryArchiveISM.DateContext dc) {
        final ByteBuffer bb = ByteBuffer.wrap(this.data);
        while (bb.hasRemaining()) {
            final int yyyyMMdd = bb.getInt();
            dc.update(yyyyMMdd);
            bypassPrice(bb);
        }
    }

    private static final Item BASE = new Item();

    private static final Item DELTA = new Item();

    private static final class Item {
        private int date;

        private ByteBuffer buf;

        private int from;

        private int length;

        private Item() {
        }

        void reset(ByteBuffer buf) {
            this.date = 0;
            this.from = -1;
            this.length = 0;
            this.buf = buf;
        }

        public void next() {
            if (!this.buf.hasRemaining()) {
                this.date = 0;
            }
            else {
                this.from = this.buf.position();
                this.date = this.buf.getInt();
                final int fields = HistoryUtil.fromUnsignedByte(this.buf.get());
                for (int i = 0; i < fields; i++) {
                    this.buf.get(); // field id
                    byte b;
                    do {
                        b = this.buf.get();
                    } while (!BCD.isBoundary(b)); // field price
                }
                this.length = this.buf.position() - this.from;
            }
        }

        public void transferPrice(ByteBuffer bb) {
            bb.put(this.buf.array(), this.from, this.length);
        }

        public void transferRest(ByteBuffer bb) {
            this.buf.position(this.from);
            bb.put(this.buf);
        }

        public void transferPrice(Int2ObjectSortedMap<byte[]> prices) {
            final ByteBuffer bb = this.buf.asReadOnlyBuffer();
            bb.position(this.from);
            bb.limit(this.from + this.length);
            bb.getInt(); // bypass date

            final int fields = HistoryUtil.fromUnsignedByte(bb.get());
            for (int i = 0; i < fields; i++) {
                final int fieldId = HistoryUtil.fromUnsignedByte(bb.get());
                final int from = bb.position();
                BCD.bypassPrices(bb, 1);
                prices.put(fieldId, Arrays.copyOfRange(this.buf.array(), from, bb.position()));
            }
        }
    }

    private static final Int2ObjectSortedMap<byte[]> PRICES = new Int2ObjectAVLTreeMap<>();

    void update(EodFields efs, boolean extension) {
        final ByteBuffer bb = ByteBuffer.wrap(this.data);
        while (bb.hasRemaining()) {
            final int date = bb.getInt();
            final int fields = HistoryUtil.fromUnsignedByte(bb.get());
            if (fields == 0) {
                if (!extension) {
                    efs.removePrice(date);
                }
            }
            else {
                try {
                    for (int i = 0; i < fields; i++) {
                        final int fieldId = HistoryUtil.fromUnsignedByte(bb.get());
                        final int from = bb.position();
                        BCD.bypassPrices(bb, 1);
                        PRICES.put(fieldId, Arrays.copyOfRange(this.data, from, bb.position()));
                    }
                    efs.updatePrices(date, PRICES, extension);
                } finally {
                    PRICES.clear();
                }
            }
        }
    }
}
