/*
 * TickHistoryReaderI.java
 *
 * Created on 24.10.12 14:49
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
public interface TickHistoryGarner {
    TickType getTickType();

    DateTime gatherTicks(AggregatedHistoryTickRecord record, List<String> symbols,
            TickHistoryRequest req) throws IOException;
}
