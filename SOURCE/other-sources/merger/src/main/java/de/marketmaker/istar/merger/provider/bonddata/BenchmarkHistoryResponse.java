/*
 * BenchmarkHistoryResponse.java
 *
 * Created on 23.04.12 11:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.bonddata;

import java.io.Serializable;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author tkiesgen
 */
public class BenchmarkHistoryResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    public static class BenchmarkHistoryItem implements Serializable {
        static final long serialVersionUID = 1L;

        private final String vwdsymbol;
        private final String benchmarkIsin;
        private final Long benchmarkIid;
        private final LocalDate start;
        private final LocalDate end;

        public BenchmarkHistoryItem(String vwdsymbol, String benchmarkIsin, Long benchmarkIid,
                LocalDate start, LocalDate end) {
            this.vwdsymbol = vwdsymbol;
            this.benchmarkIsin = benchmarkIsin;
            this.benchmarkIid = benchmarkIid;
            this.start = start;
            this.end = end;
        }

        public String getVwdsymbol() {
            return vwdsymbol;
        }

        public String getBenchmarkIsin() {
            return benchmarkIsin;
        }

        public Long getBenchmarkIid() {
            return benchmarkIid;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "BenchmarkHistoryItem{" +
                    "vwdsymbol='" + vwdsymbol + '\'' +
                    ", benchmarkIsin='" + benchmarkIsin + '\'' +
                    ", benchmarkIid='" + benchmarkIid+ '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }


    private final String vwdsymbol;
    private final List<BenchmarkHistoryItem> items;

    public BenchmarkHistoryResponse(String vwdsymbol,
            List<BenchmarkHistoryItem> items) {
        this.vwdsymbol = vwdsymbol;
        this.items = items;
    }

    public String getVwdsymbol() {
        return vwdsymbol;
    }

    public List<BenchmarkHistoryItem> getItems() {
        return items;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", vwdsymbol=").append(this.vwdsymbol);
        sb.append(", items=").append(this.items);
    }
}