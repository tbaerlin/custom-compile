/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public class TickHistoryGarnerImpl extends HistoryProviderBase<HistoryGathererTick> implements
        TickHistoryGarner {

    @Override
    public TickType getTickType() {
        return this.gatherer.getTickType();
    }

    @Override
    public DateTime gatherTicks(AggregatedHistoryTickRecord record, List<String> symbols,
            TickHistoryRequest req) throws IOException {
        return this.gatherer.gatherTicks(record, symbols, req);
    }
}
