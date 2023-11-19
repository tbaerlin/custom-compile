/*
 * MinMaxAvgRatioSearchResponse.java
 *
 * Created on 03.08.2006 19:23:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BestToolRatioSearchResponse extends AbstractIstarResponse implements RatioSearchResponse {
    static final long serialVersionUID = 1L;

    private final Map<String, Map<Object, List<BestToolElement>>> result
            = new HashMap<>();

    public BestToolRatioSearchResponse(Map<String, Map<Object, List<BestToolElement>>> result) {
        this.result.putAll(result);
    }

    public Map<String, Map<Object, List<BestToolElement>>> getResult() {
        return Collections.unmodifiableMap(this.result);
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(this.result);
    }

    public static class BestToolElement implements Serializable, Comparable<BestToolElement> {
        static final long serialVersionUID = 1L;

        private final long qid;
        private final long value;
        private Quote quote;

        public BestToolElement(long qid, long value) {
            this.qid = qid;
            this.value = value;
        }

        public long getQid() {
            return qid;
        }

        public long getSourceValue() {
            return value;
        }

        public BigDecimal getValue() {
            return BigDecimal.valueOf(this.value, 5);
        }

        public int compareTo(BestToolElement o) {
            return (int) (o.value - this.value);
        }

        public Quote getQuote() {
            return quote;
        }

        public void setQuote(Quote quote) {
            this.quote = quote;
        }

        @Override
        public String toString() {
            return this.qid + ".qid:" + value;
        }
    }
}