/*
 * FeriFundPerformanceTimeseries.java
 *
 * Created on 23.03.2007 12:06:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.timeseries.Timeseries;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeriPerformanceTimeseries implements Timeseries<BigDecimal>, Serializable {
    protected static final long serialVersionUID = 1L;

    private final List<FeriFundPerformanceItem> items = new ArrayList<>();

    public void add(DateTime date, BigDecimal value) {
        this.items.add(new FeriFundPerformanceItem(date, value));
    }

    public Iterator<DataWithInterval<BigDecimal>> iterator() {
        final List<FeriFundPerformanceItem> tmp = new ArrayList<>(this.items);
        tmp.sort(null);

        final Iterator<FeriFundPerformanceItem> iterator = tmp.iterator();

        return new Iterator<DataWithInterval<BigDecimal>>() {
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public DataWithInterval<BigDecimal> next() {
                final FeriFundPerformanceItem item = iterator.next();
                return new DataWithInterval<BigDecimal>() {
                    public BigDecimal getData() {
                        return item.getValue();
                    }

                    public ReadableInterval getInterval() {
                        return new Interval(item.getDate(), item.getDate());
                    }
                };
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static class FeriFundPerformanceItem implements Serializable, Comparable<FeriFundPerformanceItem> {
        protected static final long serialVersionUID = 1L;

        private final DateTime date;
        private final BigDecimal value;

        public FeriFundPerformanceItem(DateTime date, BigDecimal value) {
            this.date = date;
            this.value = value;
        }

        public DateTime getDate() {
            return date;
        }

        public BigDecimal getValue() {
            return value;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final FeriFundPerformanceItem that = (FeriFundPerformanceItem) o;
            return date.equals(that.date);
        }

        public int hashCode() {
            return date.hashCode();
        }

        public int compareTo(FeriFundPerformanceItem o) {
            return this.date.compareTo(o.date);
        }

        public String toString() {
            return "FeriFundPerformanceItem[" + this.value + "@" + this.date + "]";
        }
    }
}
