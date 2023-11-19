/*
 * IntradayRequest.java
 *
 * Created on 17.02.2005 13:45:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.encodeTimestamp;
import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.toDateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IntradayRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final List<Item> items = new ArrayList<>();

    /**
     * only snap records that have received an update at or after this timestamp
     * will be returned.
     */
    private int updatedSince;

    /**
     * If this value is &gt; 0, the returned snap records will only contain fields up to (and
     * including) this field.
     */
    private int maxFieldOrderId;

    /**
     * If true, this lifts the {@link de.marketmaker.istar.feed.tick.TickServer} 100 days limit of history data.
     */
    private boolean tickDataFullAccess;

    public IntradayRequest() {
    }

    public void add(Item item) {
        this.items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public int size() {
        return this.items.size();
    }

    public int getUpdatedSince() {
        return updatedSince;
    }

    public void setUpdatedSince(DateTime dt) {
        setUpdatedSince(((dt.getYear() >= 2000) ? encodeTimestamp(dt.getMillis()) : 0));
    }

    /**
     * Only request data if feed data object has received an update at or after <code>timestamp</code>
     * @param timestamp as encoded by {@link de.marketmaker.istar.feed.DateTimeProvider.Timestamp#encodeTimestamp(long)},
     * which is the same format as used by {@link OrderedUpdate#getTimestamp()}.
     */
    public void setUpdatedSince(int timestamp) {
        this.updatedSince = timestamp;
    }

    public int getMaxFieldOrderId() {
        return maxFieldOrderId;
    }

    public void setMaxFieldOrderId(int maxFieldOrderId) {
        this.maxFieldOrderId = maxFieldOrderId;
    }

    public boolean isTickDataFullAccess() {
        return tickDataFullAccess;
    }

    public void setTickDataFullAccess(boolean tickDataFullAccess) {
        this.tickDataFullAccess = tickDataFullAccess;
    }

    public boolean isWithTicks() {
        for (final IntradayRequest.Item item : this.items) {
            if (item.isWithTicks()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(80 + items.size() * 40);
        sb.append("IntradayRequest[").append(getClientInfo());
        if (this.maxFieldOrderId > 0) {
            sb.append(", maxOid=").append(this.maxFieldOrderId);
        }
        if (this.updatedSince > 0) {
            sb.append(", updatedSince=").append(toDateTime(this.updatedSince));
        }
        if (this.items.isEmpty()) {
            return sb.append("]").toString();
        }
        if (isUniform()) {
            sb.append(", ");
            this.items.get(0).appendFlags(sb);
            return sb.append(", ").append(keys()).append("]").toString();
        }
        return sb.append(", items=").append(this.items).append("]").toString();
    }

    /**
     * @return true iff all items share the same parameters
     */
    private boolean isUniform() {
        if (items.size() < 2) {
            return true;
        }
        final Item item = this.items.get(0);
        for (int i = 1; i < this.items.size(); i++) {
            final Item other = items.get(i);
            if (other.realtime != item.realtime
                    || other.withTicks != item.withTicks
                    || other.ticksFrom != item.ticksFrom
                    || other.ticksTo != item.ticksTo
                    ) {
                return false;
            }
        }
        return true;
    }

    private List<String> keys() {
        final List<String> result = new ArrayList<>(this.items.size());
        for (Item item : items) {
            result.add(item.vendorkey);
        }
        return result;
    }

    public static class Item implements Serializable {
        static final long serialVersionUID = 2L;

        private final String vendorkey;

        private final boolean realtime;

        private final boolean realtimeAndDelay;

        private boolean withTicks;

        private int ticksFrom;

        private int ticksTo;

        private int[] tickStorageInfo;

        private String serverId;


        public Item(String vendorkey) {
            this.vendorkey = vendorkey;
            this.realtime = true;
            this.realtimeAndDelay = true;
        }

        public Item(String vendorkey, boolean realtime) {
            this.vendorkey = vendorkey;
            this.realtime = realtime;
            this.realtimeAndDelay = false;
        }

        public void setRetrieveTicks(int day) {
            setRetrieveTicks(day, day);
        }

        public void setRetrieveTicks(int from, int to) {
            if (this.tickStorageInfo != null && from != to) {
                throw new IllegalStateException("tickStorageInfo and multiple days");
            }
            this.withTicks = true;
            this.ticksFrom = from;
            this.ticksTo = to;
        }

        public void setTickStorageInfo(String serverId, int[] tickStorageInfo) {
            if (this.withTicks && this.ticksFrom != this.ticksTo) {
                throw new IllegalStateException("tickStorageInfo and multiple days");
            }
            this.serverId = serverId;
            this.tickStorageInfo = tickStorageInfo;
        }

        public int[] getTickStorageInfo() {
            return CLIENT_ID.equals(this.serverId) ? this.tickStorageInfo : null;
        }

        public String getVendorkey() {
            return vendorkey;
        }

        public boolean isRealtime() {
            return realtime;
        }

        public boolean isRealtimeAndDelay() {
            return realtimeAndDelay;
        }

        public boolean isWithSnap() {
            return true;
        }

        public boolean isWithTicks() {
            return withTicks;
        }

        public int getTicksFrom() {
            return ticksFrom;
        }

        public int getTicksTo() {
            return ticksTo;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(20).append(this.vendorkey).append("(");
            appendFlags(sb);
            return sb.append(")").toString();
        }

        private void appendFlags(StringBuilder sb) {
            sb.append(this.realtimeAndDelay ? '*' : (this.realtime ? 'R' : 'D'));
            if (this.withTicks) {
                sb.append("T ").append(this.ticksFrom);
                if (this.ticksFrom != this.ticksTo) {
                    sb.append("-").append(this.ticksTo);
                }
                if (this.tickStorageInfo != null) {
                    sb.append(" ").append(this.serverId).append(Arrays.toString(this.tickStorageInfo));
                }
            }
        }
    }
}
