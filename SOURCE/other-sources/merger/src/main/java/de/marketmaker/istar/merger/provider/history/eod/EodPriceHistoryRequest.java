/*
 * TickHistoryRequest.java
 *
 * Created on 23.08.12 15:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.Interval;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.feed.history.HistoryUtil;

/**
 * @author zzhao
 */
public class EodPriceHistoryRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = -7086945224166542176L;

    private final long quote;

    private final Set<Integer> fields;

    private final Interval interval;

    public EodPriceHistoryRequest(long quote, Interval interval, int... fields) {
        if (quote <= 0) {
            throw new IllegalArgumentException("invalid quote: " + quote);
        }
        if (null == fields || fields.length == 0) {
            throw new IllegalArgumentException("data might exist, but no VWD field mapping rules found");
        }
        this.quote = quote;
        this.interval = interval;
        final TreeSet<Integer> set = new TreeSet<>();
        for (int field : fields) {
            set.add(field);
        }
        this.fields = Collections.unmodifiableSet(set);
    }

    public long getQuote() {
        return quote;
    }

    public Set<Integer> getFields() {
        return fields;
    }

    public Interval getInterval() {
        return interval;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100).append("{").append(getClientInfo());
        sb.append(",").append(this.quote).append(",").append(this.fields).append(",")
                .append(HistoryUtil.DTF_DAY.print(this.interval.getStart()))
                .append("~").append(HistoryUtil.DTF_DAY.print(this.interval.getEnd()));
        return sb.append("}").toString();
    }
}
