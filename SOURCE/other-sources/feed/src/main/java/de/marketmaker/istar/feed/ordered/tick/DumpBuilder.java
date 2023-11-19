/*
 * TickBuilder.java
 *
 * Created on 14.11.12 16:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;

/**
 * @author oflege
 */
@NotThreadSafe
public class DumpBuilder extends FieldDataBuilder implements OrderedUpdateBuilder {

    private static final int TICK_FLAGS
            = FeedUpdateFlags.FLAG_WITH_TRADE
            | FeedUpdateFlags.FLAG_WITH_BID
            | FeedUpdateFlags.FLAG_WITH_ASK
            | FeedUpdateFlags.FLAG_WITH_TICK_FIELD
            | FeedUpdateFlags.FLAG_PROFESSIONAL_TRADE;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private MemoryTickStore store;

    public DumpBuilder() {
        super(8192);
    }

    DumpBuilder(int bufferSize) {
        super(bufferSize);
    }

    public void setStore(MemoryTickStore store) {
        this.store = store;
    }

    /**
     * Called by parser thread, we are synchronized on data
     */
    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        assert Thread.holdsLock(data);

        DumpFeedData dfd = (DumpFeedData) data;

        if (!dfd.getOrderedTickData().setDate(update.getDate())) {
            return;
        }


        final int ts = update.getTimestamp();
        dfd.setLastUpdateTimestamp(ts);

        if (update.hasFlag(TICK_FLAGS)) {
            // this helps for idle data eviction: data for markets without a recent tick timestamp
            // will be evicted more frequently
            dfd.getMarket().setTickFeedTimestamp(update.getTimestamp());
        }

        this.bb.clear().position(2);
        this.bb.putInt(ts);

        byte mdpsMsgType = update.getMdpsMsgType();
        this.bb.put(mdpsMsgType > 0 ? mdpsMsgType : (byte) -update.getMsgType());

        this.bb.put((byte) update.getMdpsKeyType());
        this.bb.putShort((short) update.getFlags());
        update.getFieldData().putFields(this.bb);

        prependLength();

        this.store.add(data, this.bb);
    }

    private void prependLength() {
        this.bb.flip();
        final int i = this.bb.remaining() - 2;
        bb.put(1, (byte) ((i & 0x7f) | 0x80));
        if (i < 0x80) {
            bb.position(1);
        }
        else {
            bb.put(0, (byte) ((i >> 7) & 0x7f));
        }
    }

    // for test purposes only
    byte[] testPrependLength(int i) {
        this.bb.clear().position(2 + i);
        prependLength();
        return Arrays.copyOfRange(this.bb.array(), this.bb.position(), this.bb.limit());
    }
}
