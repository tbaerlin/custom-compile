/*
 * TickWorker.java
 *
 * Created on 19.04.13 14:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.util.EnumSet;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickRecordImpl;

/**
 * @author zzhao
 */
class TickWorker implements WorkHandler<RequestItem>, WorkReporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int id;

    private final int date;

    private final EnumSet<TickType> tickTypes;

    private final RingBuffer<ResultItem> resultBuffer;

    private int tickAggregated = 0;

    private int tickDamaged = 0;

    TickWorker(int date, EnumSet<TickType> tickTypes, RingBuffer<ResultItem> resultBuffer, int id) {
        this.date = date;
        this.tickTypes = tickTypes;
        this.resultBuffer = resultBuffer;
        this.id = id;
    }

    @Override
    public void onEvent(RequestItem requestItem) throws Exception {
        final ResultItem resultItem = resultBuffer.get(requestItem.getSequence());
        try {
            resultItem.reset();
            resultItem.setSymbol(requestItem.getSymbol());
            resultItem.setDamaged(requestItem.isDamaged());
            if (requestItem.isDamaged()) {
                this.tickDamaged++;
            }
            else {
                final TickRecordImpl record = new TickRecordImpl();
                record.add(this.date, requestItem.getTickData(), requestItem.getEncoding());

                final AbstractTickRecord.TickItem item = record.getItem(this.date);
                final HistoryTickExchanger processor = new HistoryTickExchanger(
                        item, this.tickTypes, !requestItem.isNegativeTicksPossible());
                item.accept(processor);
                final byte[] result = processor.getResult();

                resultItem.setAggregatedTickData(result);
                this.tickAggregated++;
            }
        } catch (Exception e) {
            this.logger.error("<onEvent> failed aggregating {}", requestItem.getSymbol(), e);
            this.tickDamaged++;
            resultItem.setDamaged(true);
        } finally {
            resultBuffer.publish(requestItem.getSequence());
        }
    }

    @Override
    public void report() {
        this.logger.info("<report> {} aggregated, {} damaged on worker {}",
                this.tickAggregated, this.tickDamaged, this.id);
    }
}
