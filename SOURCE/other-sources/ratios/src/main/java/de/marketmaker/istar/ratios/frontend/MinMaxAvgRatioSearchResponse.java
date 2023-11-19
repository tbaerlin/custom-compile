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
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MinMaxAvgRatioSearchResponse extends AbstractIstarResponse implements
        RatioSearchResponse {
    static final long serialVersionUID = 125438213125L;

    private final Map<Integer, Map<String, Map<String, MinMaxAvg>>> result = new HashMap<>();

    void add(String groupBy0, String groupBy1, int fieldid, double value) {
        Map<String, Map<String, MinMaxAvg>> perFieldMap = this.result.computeIfAbsent(fieldid, (key) -> new HashMap<>());
        Map<String, MinMaxAvg> group0Map = perFieldMap.computeIfAbsent(groupBy0, (key) -> new HashMap<>());
        group0Map.merge(groupBy1, new MinMaxAvg(groupBy0, groupBy1, value), (oldValue, newValue) -> oldValue.add(value));
    }

    public void merge(MinMaxAvgRatioSearchResponse other) {
        other.result.forEach((k, v) -> this.result.merge(k, v, this::merge));
    }

    private Map<String, Map<String, MinMaxAvg>> merge(Map<String, Map<String, MinMaxAvg>> my,
            Map<String, Map<String, MinMaxAvg>> other) {
        other.forEach((k, v) -> my.merge(k, v, this::mergeMinMaxAvg));

        return my;
    }

    private Map<String, MinMaxAvg> mergeMinMaxAvg(Map<String, MinMaxAvg> my,
            Map<String, MinMaxAvg> other) {
        other.forEach((k, v) -> my.merge(k, v, MinMaxAvg::merge));

        return my;
    }

    public Map<Integer, Map<String, Map<String, MinMaxAvg>>> getResult() {
        return Collections.unmodifiableMap(this.result);
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(this.result);
    }

    public static class MinMaxAvg implements Serializable {
        static final long serialVersionUID = 1L;

        private final String groupBy0;

        private final String groupBy1;

        private int num = 1;

        private double sum;

        private double min;

        private double max;

        MinMaxAvg(String groupBy0, String groupBy1, double value) {
            this.groupBy0 = groupBy0;
            this.groupBy1 = groupBy1;
            this.sum = value;
            this.min = value;
            this.max = value;
        }

        public String toString() {
            return "MinMaxAvg[" + this.getGroupBy0() + "/" + getGroupBy1()
                    + ", #=" + this.num
                    + ", min=" + this.min
                    + ", max=" + this.max
                    + ", avg=" + getAvg()
                    + "]";
        }

        MinMaxAvg add(double value) {
            this.num++;
            this.sum += value;
            this.min = Math.min(this.min, value);
            this.max = Math.max(this.max, value);

            return this;
        }

        public String getGroupBy0() {
            return groupBy0;
        }

        public String getGroupBy1() {
            return groupBy1;
        }

        public int getNum() {
            return num;
        }

        public BigDecimal getSum() {
            return BigDecimal.valueOf(this.sum);
        }

        public BigDecimal getAvg() {
            return BigDecimal.valueOf(this.sum / this.num);
        }

        public BigDecimal getMin() {
            return BigDecimal.valueOf(this.min);
        }

        public BigDecimal getMax() {
            return BigDecimal.valueOf(this.max);
        }

        public MinMaxAvg merge(MinMaxAvg other) {
            this.num += other.num;
            this.sum += other.sum;
            this.min = Math.min(this.min, other.min);
            this.max = Math.max(this.max, other.max);

            return this;
        }
    }
}
