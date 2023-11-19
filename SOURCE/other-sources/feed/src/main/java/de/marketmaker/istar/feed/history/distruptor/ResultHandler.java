/*
 * ResultHandler.java
 *
 * Created on 19.04.13 14:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.nio.ByteBuffer;
import java.util.Map;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.TickHistoryContext;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.HistoryWriter;

/**
 * @author zzhao
 */
class ResultHandler implements EventHandler<ResultItem>, WorkReporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int days;

    private final Map<TickType, HistoryWriter<ByteString>> writers;

    private final TickHistoryContext ctx;

    private int ticksWritten = 0;

    private int ticksNotWritten = 0;

    private int damagedTicks = 0;

    ResultHandler(int days, Map<TickType, HistoryWriter<ByteString>> writers, TickHistoryContext ctx) {
        this.days = days;
        this.writers = writers;
        this.ctx = ctx;
    }

    @Override
    public void onEvent(ResultItem resultItem, long sequence, boolean endOfBatch) throws Exception {
        if (resultItem.isDamaged()) {
            this.damagedTicks++;
            return;
        }
        final ByteBuffer bb = ByteBuffer.wrap(resultItem.getAggregatedTickData());
        while (bb.hasRemaining()) {
            final TickType tickType = HistoryTickExchanger.fromOrdinal(bb.get());
            final short numTicks = bb.getShort();
            final byte[] bytes = new byte[bb.getInt()];
            bb.get(bytes);
            try {
                final byte[] data = ctx.postProcessTickData(tickType, bytes);
                if (!HistoryUtil.isUnsignedShort(data.length)) {
                    this.logger.error("<writeAggregation> compressed tick data overflow {} {}",
                            resultItem.getSymbol(), data.length);
                    continue;
                }

                final ByteBuffer buf = ByteBuffer.allocate(6 + data.length);
                buf.putShort((short) this.days);
                buf.putShort(numTicks);
                buf.putShort((short) data.length);
                buf.put(data);
                this.writers.get(tickType).withEntry(resultItem.getSymbol(), buf.array());
                this.ticksWritten++;
            } catch (Exception e) {
                this.logger.error("<writeAggregation> failed writing data for {}",
                        resultItem.getSymbol(), e);
                this.ticksNotWritten++;
            }
        }
    }

    @Override
    public void report() {
        this.logger.info("<report> {} written, {} not written, {} damaged",
                this.ticksWritten, this.ticksNotWritten, this.damagedTicks);
    }
}
