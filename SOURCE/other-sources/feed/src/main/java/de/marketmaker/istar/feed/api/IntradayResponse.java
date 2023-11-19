/*
 * IntradayResponse.java
 *
 * Created on 17.02.2005 13:46:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.tick.TickRecordImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IntradayResponse extends AbstractIstarResponse implements
        Iterable<IntradayResponse.Item> {
    static final long serialVersionUID = 1L;

    private final Map<String, Item> items = new HashMap<>();

    public IntradayResponse() {
    }

    public void add(Item item) {
        this.items.put(item.getVendorkey(), item);
    }

    public Item getItem(String key) {
        return this.items.get(key);
    }

    public int size() {
        return this.items.size();
    }

    public Iterator<Item> iterator() {
        return this.items.values().iterator();
    }

    public void mergeTicksFrom(IntradayResponse other) {
        for (Map.Entry<String, Item> entry : other.items.entrySet()) {
            final String vendorkey = entry.getKey();

            final Item item = getItem(vendorkey);
            if (item == null) {
                // we don't have it yet, so add it
                add(entry.getValue());
                continue;
            }

            final TickRecord tickRecord = entry.getValue().getTickRecord();
            if (tickRecord != null) {
                if (item.getTickRecord() == null) {
                    item.setTickRecord(tickRecord);
                }
                else {
                    // merge with other item's tickRecord, but prefer my data
                    item.setTickRecord(tickRecord.merge(item.getTickRecord()));
                }
            }
        }
    }

    protected void appendToString(StringBuilder sb) {
        if (this.items.size() != 1) {
            sb.append(", #items=").append(this.items.size());
            final int tickSize = tickSize();
            if (tickSize > 0) {
                sb.append(", tickSize=").append(tickSize);
            }
        }
        else {
            sb.append(", ").append(this.items.values().iterator().next());
        }
    }

    private int tickSize() {
        int n = 0;
        for (Item item : items.values()) {
            n += item.tickSize();
        }
        return n;
    }

    public static class Item implements Serializable {
        static final long serialVersionUID = 2L;

        private final String vendorkey;

        private final boolean realtime;

        private SnapRecord priceSnapRecord;

        private SnapRecord delaySnapRecord;

        private transient SnapRecord mergedDelaySnapRecord;

        private TickRecord tickRecord = null;

        private int[] tickStorageInfo;

        private int createdTimestamp;

        private int vendorkeyType;

        /**
         * Creates response item for the given key
         * @param vendorkey the key used in the request, <em>not necessarily</em> a "real" vendorkey
         * with a numeric type prefix, may be a vwdcode.
         * @param realtime
         */
        public Item(String vendorkey, boolean realtime) {
            this.vendorkey = vendorkey;
            this.realtime = realtime;
        }

        public int getCreatedTimestamp() {
            return createdTimestamp;
        }

        public void setCreatedTimestamp(int createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(80)
                    .append("Item[").append(this.vendorkey);
            if (!this.realtime) {
                sb.append("/D");
            }
            if (this.priceSnapRecord != null) {
                sb.append(", rt=").append(toString(this.priceSnapRecord));
            }
            if (this.delaySnapRecord != null) {
                sb.append(", nt=").append(toString(this.delaySnapRecord));
            }
            if (this.tickRecord != null) {
                sb.append(", tick=").append(this.tickRecord);
                if (this.tickStorageInfo != null) {
                    sb.append("/").append(Arrays.toString(tickStorageInfo));
                }
            }
            return sb.append("]").toString();
        }

        private String toString(SnapRecord sr) {
            if (sr instanceof OrderedSnapRecord) {
                OrderedSnapRecord orderedSnapRecord = (OrderedSnapRecord) sr;
                return "#" + orderedSnapRecord.length();
            }
            return sr.toString();
        }

        /**
         * HACK: the chicago3 backend sometimes miscalculates the last tick timestamp so
         * that not all ticks are returned for delayed tick records. At least for records
         * with nominal delay time 0 (e.g., EUWAX), we can avoid that.
         * TODO: remove this method as soon as chicago3 backends set correct last time
         */
        private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            if (!this.realtime && this.priceSnapRecord != null
                    && this.priceSnapRecord.getNominalDelayInSeconds() == 0
                    && this.tickRecord instanceof TickRecordImpl) {
                ((TickRecordImpl) this.tickRecord).unsetLast();
            }
        }

        /**
         * @return the same key that was used to request this item,
         * <em>not necessarily</em> a "real" vendorkey
         * with a numeric type prefix, may be a vwdcode. In all cases, the actual vendorkey type
         * can be requested using {@link #getVendorkeyType()}.
         */
        public String getVendorkey() {
            return vendorkey;
        }

        public boolean isRealtime() {
            return realtime;
        }

        public void setPriceSnapRecord(SnapRecord priceSnapRecord) {
            this.priceSnapRecord = priceSnapRecord;
        }

        public void setDelaySnapRecord(SnapRecord delaySnapRecord) {
            this.delaySnapRecord = delaySnapRecord;
        }

        public void setTickRecord(TickRecord tickRecord) {
            this.tickRecord = tickRecord;
        }

        public SnapRecord getPriceSnapRecord() {
            if (!this.realtime && this.delaySnapRecord != null) {
                return getDelaySnapRecord();
            }
            return (priceSnapRecord != null) ? this.priceSnapRecord : NullSnapRecord.INSTANCE;
        }

        /**
         * the delay snap record (with non-dynamic fields merged from the realtime price snap record)
         * @return delay snap record, may be null
         */
        public SnapRecord getDelaySnapRecord() {
            if (this.priceSnapRecord == null || this.delaySnapRecord == null) {
                return getPriceSnapRecord();
            }
            if (this.mergedDelaySnapRecord == null) {
                this.mergedDelaySnapRecord = ((OrderedSnapRecord) this.priceSnapRecord)
                        .merge((OrderedSnapRecord) this.delaySnapRecord);
            }
            return this.mergedDelaySnapRecord;
        }

        /**
         * the delay snap record without merged non-dynamic fields
         * @return delay snap record, may be null
         */
        public SnapRecord getRawDelaySnapRecord() {
            return this.delaySnapRecord;
        }

        public TickRecord getTickRecord() {
            return tickRecord;
        }

        private int tickSize() {
            return this.tickRecord != null ? this.tickRecord.tickSize() : 0;
        }

        public void setTickStorageInfo(int[] tickStorageInfo) {
            this.tickStorageInfo = tickStorageInfo;
        }

        public int[] getTickStorageInfo() {
            return this.tickStorageInfo;
        }

        public int getVendorkeyType() {
            return this.vendorkeyType;
        }

        public void setVendorkeyType(int vendorkeyType) {
            this.vendorkeyType = vendorkeyType;
        }

        public void merge(Item other) {
            if (this.priceSnapRecord == null) {
                this.priceSnapRecord = other.priceSnapRecord;
            }
            if (this.delaySnapRecord == null) {
                this.delaySnapRecord = other.delaySnapRecord;
            }
            if (this.tickRecord == null) {
                this.tickRecord = other.tickRecord;
            }
            else if (other.tickRecord != null) {
                this.tickRecord = this.tickRecord.merge(other.tickRecord);
            }
        }
    }
}
