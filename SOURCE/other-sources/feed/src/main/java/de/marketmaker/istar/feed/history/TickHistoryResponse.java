/*
 * TickHistoryResponse.java
 *
 * Created on 23.08.12 15:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;

/**
 * @author zzhao
 */
public class TickHistoryResponse extends AbstractIstarResponse {

    public static final TickHistoryResponse INVALID = new TickHistoryResponse();

    private static final long serialVersionUID = 6591065768011766748L;

    private final AggregatedTickRecord record;

    private final DateTime historyEnd;

    public TickHistoryResponse() {
        this(null, TickHistoryContextImpl.GENESIS.toDateTimeAtStartOfDay());
        setInvalid();
    }

    public TickHistoryResponse(AggregatedTickRecord record, DateTime historyEnd) {
        this.record = record;
        this.historyEnd = historyEnd;
    }

    public DateTime getHistoryEnd() {
        return historyEnd;
    }

    public AggregatedTickRecord getRecord() {
        return record;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(this.record);
    }
}
