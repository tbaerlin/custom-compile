/*
 * TickHistoryResponse.java
 *
 * Created on 23.08.12 15:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod;

import java.util.Collections;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;

/**
 * @author zzhao
 */
public class EodPriceHistoryResponse extends AbstractIstarResponse {

    public static final EodPriceHistoryResponse INVALID = new EodPriceHistoryResponse();

    private static final long serialVersionUID = 6591065768011766748L;

    private final long quoteId;

    private final Map<Integer, HistoricTimeseries> histories;

    private EodPriceHistoryResponse() {
        this.quoteId = -1;
        this.histories = Collections.emptyMap();
        setInvalid();
    }

    public EodPriceHistoryResponse(long quoteId, Map<Integer, HistoricTimeseries> histories) {
        this.quoteId = quoteId;
        this.histories = histories;
    }

    public long getQuoteId() {
        return quoteId;
    }

    public HistoricTimeseries getHistory(int field) {
        return this.histories.get(field);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(this.quoteId);
        for (Map.Entry<Integer, HistoricTimeseries> entry : histories.entrySet()) {
            sb.append(",").append(entry.getKey()).append("-").
                    append(HistoryUtil.DTF_DAY.print(entry.getValue().getStartDay()))
                    .append(":").append(entry.getValue().size());
        }
    }
}
