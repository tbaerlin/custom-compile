/*
 * TickBuilder.java
 *
 * Created on 14.11.12 16:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_WITH_OLD_HANDELSDATUM;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_WITH_SPECIAL_TICK_FIELDS;
import static de.marketmaker.istar.feed.vwd.VwdFieldOrder.*;

/**
 * @author oflege
 */
@NotThreadSafe
public class TickBuilder extends FieldDataBuilder implements OrderedUpdateBuilder {

    private static final int REGULAR_TICK_FLAGS = FeedUpdateFlags.FLAG_WITH_TRADE
            | FeedUpdateFlags.FLAG_WITH_BID
            | FeedUpdateFlags.FLAG_WITH_ASK
            | FeedUpdateFlags.FLAG_WITH_TICK_FIELD;

    private static final int TICK_FLAGS = REGULAR_TICK_FLAGS
            | FeedUpdateFlags.FLAG_PROFESSIONAL_TRADE;

    private MemoryTickStore store;

    private TickWriter writer;

    private int today = 0;

    private int yesterday = 0;

    private int tickTime;

    private final ExecutorService es = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(200),
            r -> new Thread(r, "TickBuilder-Correcions"));

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public TickBuilder() {
        // buffer starts with unsigned byte indicating length, using 255 ensures
        // longer messages will raise an exception
        super(255);
    }

    public void setStore(MemoryTickStore store) {
        this.store = store;
    }

    public void setWriter(TickWriter writer) {
        this.writer = writer;
    }

    /**
     * Called by parser thread, we are synchronized on data
     */
    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        assert Thread.holdsLock(data);

        if (!update.hasFlag(TICK_FLAGS) || update.hasFlag(FLAG_WITH_OLD_HANDELSDATUM)) {
            if (update.getMsgType() == VwdFeedConstants.MESSAGE_TYPE_CORRECTION) {
                processCorrection(data, update);
            }
            return;
        }

        if (!data.getOrderedTickData().setDate(getDate(update))) {
            return;
        }

        data.getMarket().setTickFeedTimestamp(update.getTimestamp());

        this.bb.clear().position(1);
        // will always be 0 for professional trades, but TickDecompressor expects it
        this.bb.put((byte) (update.getFlags() & REGULAR_TICK_FLAGS));

        if (update.hasFlag(FeedUpdateFlags.FLAG_PROFESSIONAL_TRADE)) {
            processProfessionalTrade(data, update);
        }
        else {
            processTick(data, update);
        }
    }

    private void processCorrection(FeedData data, OrderedUpdate update) {
        try {
            final LiveTickCorrections tcs = LiveTickCorrections.create((OrderedFeedData) data,
                    update, this.writer, this.store);
            if (tcs != null) {
                this.es.submit(tcs);
            }
        } catch (Exception e) {
            this.logger.error("<processCorrection> failed", e);
        }
    }

    private void processProfessionalTrade(FeedData data, OrderedUpdate update) {
        this.bb.put(update.getFieldData().asBuffer());
        addBufferTo(data);
    }

    private void processTick(FeedData data, OrderedUpdate update) {
        final BufferFieldData fd = update.getFieldData();

        this.tickTime = getTime(fd);
        // the first field is always time, its id/type is implicit, so we do NOT put it first
        bb.putInt(this.tickTime);

        this.lastOrder = 0;
        addTickFields(update, fd);

        if (fd.getId() > 0 && update.hasFlag(FLAG_WITH_SPECIAL_TICK_FIELDS)) {
            addAdditionalFields(fd, data.getMarket().getTickOrderIds());
        }

        addBufferTo(data);
    }

    private void addTickFields(OrderedUpdate update, BufferFieldData fd) {
        for (int oid = fd.getId(); oid != 0 && oid < VwdFieldOrder.FIRST_NON_TICK; oid = fd.readNext()) {
            switch (oid) {
                case ORDER_MMF_BEZAHLT_DATUM:
                case ORDER_MMF_BOERSENZEIT:
                case ORDER_ADF_PROZENTUALE_VERAENDERUNG:
                case ORDER_ADF_VERAENDERUNG:
                case ORDER_ADF_BRIEF_QUELLE:
                case ORDER_ADF_GELD_QUELLE:
                case ORDER_ADF_BEZAHLT_DATUM:
                case ORDER_ADF_DATUM_QUOTIERUNG:
                case ORDER_ADF_DATUM:
                case ORDER_ADF_HANDELSDATUM:
                    fd.skipCurrent();
                    break;
                case ORDER_ADF_ZEIT_QUOTIERUNG:
                case ORDER_ADF_GELD_ZEIT:
                case ORDER_ADF_BRIEF_ZEIT:
                case ORDER_ADF_BEZAHLT_ZEIT:
                    addTime(oid, fd.getInt());
                    break;
                case ORDER_ADF_QUELLE:
                    addQuelle(update, fd);
                    break;
                default:
                    addFieldToBuffer(fd);
                    break;
            }
        }
    }

    private void addAdditionalFields(BufferFieldData fd, BitSet additionalOids) {
        if (additionalOids == null) {
            return;
        }
        final int max = additionalOids.size();
        for (int oid = fd.getId(); oid != 0 && oid < max; oid = fd.readNext()) {
            if (additionalOids.get(oid)) {
                addFieldToBuffer(fd);
            }
            else {
                fd.skipCurrent();
            }
        }
    }

    private void addQuelle(OrderedUpdate update, BufferFieldData fd) {
        if (update.hasFlag(FeedUpdateFlags.FLAG_WITH_QUELLE)) {
            addFieldToBuffer(fd);
        }
        else {
            fd.skipCurrent();
        }
    }

    private void addTime(int id, final int time) {
        if (time != this.tickTime) {
            putTimeFid(id);
            this.bb.putInt(time);
        }
    }

    private void addBufferTo(FeedData data) {
        this.bb.flip();
        final int rem = this.bb.remaining();
        if (rem > 8) { // time field (4 bytes) + at least one time/price tick field (4 or 5 bytes)
            this.bb.put(0, (byte) rem);
            this.store.add(data, this.bb);
        }
    }

    private int getDate(OrderedUpdate update) {
        final int date = update.getDate();
        if (!update.hasFlag(FeedUpdateFlags.FLAG_YESTERDAY)) {
            return date;
        }
        if (date != this.today) {
            this.today = date;
            this.yesterday = DateUtil.toYyyyMmDd(DateUtil.yyyyMmDdToLocalDate(date).minusDays(1));
        }
        return this.yesterday;
    }

    /**
     * Returns the time for the current tick; since it scans the id of the next field immediately
     * after the time field, the code calling this must continue field processing
     * with <tt>fd.getId()</tt> rather than <tt>fd.readNext()</tt>.
     * @param fd fields
     * @return tick time;
     */
    public static int getTime(BufferFieldData fd) {
        int result = 0;
        for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
            switch (id) {
                case ORDER_ADF_ZEIT:
                    result = fd.getInt();
                    break;
                case ORDER_ADF_BOERSENZEIT:
                    // intentional fall-through, return whatever comes first
                case ORDER_ADF_ZEIT_QUOTIERUNG:
                    result = fd.getInt();
                    fd.readNext(); // forward fd's internal state as if we would get to the default branch
                    return result;
                default:
                    return result;
            }
        }
        return result;
    }
}
