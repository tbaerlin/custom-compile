/*
 * ImpliedVolatilitiesImpl.java
 *
 * Created on 14.12.11 13:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.risk;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.ImpliedVolatilities;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;

/**
 * @author oflege
 */
public class ImpliedVolatilitiesImpl implements ImpliedVolatilities, Serializable {
    static final long serialVersionUID = 1L;

    private static final long[][] NULL_DATA = new long[2][0];

    private class ItemImpl implements ImpliedVolatilities.Item {
        private final int i;

        private ItemImpl(int i) {
            this.i = i;
        }

        @Override
        public String getType() {
            return ImpliedVolatilitiesImpl.this.getType(this.i);
        }

        @Override
        public BigDecimal getStrike() {
            return BigDecimal.valueOf(ImpliedVolatilitiesImpl.this.getStrike(this.i));
        }

        @Override
        public LocalDate getMaturity() {
            return DateUtil.yyyyMmDdToLocalDate(ImpliedVolatilitiesImpl.this.getMaturity(this.i));
        }

        @Override
        public BigDecimal getImpliedVolatility() {
            return BigDecimal.valueOf(ImpliedVolatilitiesImpl.this.getVolatility(this.i));
        }
    }
    
    private class DailyIterator implements ImpliedVolatilities.Daily {
        private final LocalDate day;
        
        private int n;
        
        private final int to;
        
        private DailyIterator(int n) {
            this.n = n;
            final int yyyymmdd = ImpliedVolatilitiesImpl.this.getDate(n);
            this.day = DateUtil.yyyyMmDdToLocalDate(yyyymmdd);
            this.to = getNextDayPos(this.n);
        }

        @Override
        public LocalDate getDay() {
            return this.day;
        }

        @Override
        public Iterator<Item> getItems() {
            return new Iterator<Item>() {
                @Override
                public boolean hasNext() {
                    return n < to;
                }

                @Override
                public Item next() {
                    if (!hasNext()) {
                        throw new IllegalStateException();
                    }
                    return new ItemImpl(n++);           
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private final long underyingId;

    /**
     * expected to be a [2][n] array, each value in [0][i] corresponds to a value in the values array
     * of an {@link Volatilities.Item}, each value in [1][i] is equivalent to the
     * key field of the respective item.
     */
    private final long[][] encodedData;       

    public static ImpliedVolatilities createEmpty(long id) {
        return new ImpliedVolatilitiesImpl(id, NULL_DATA);
    }

    ImpliedVolatilitiesImpl(long underyingId, long[][] encodedData) {
        this.underyingId = underyingId;
        this.encodedData = encodedData;
    }
    
    private int getNextDayPos(int from) {
        final int yyyymmdd = ImpliedVolatilitiesImpl.this.getDate(from);
        for (int i = from + 1; i < this.encodedData[0].length; i++) {
            if (ImpliedVolatilitiesImpl.this.getDate(i) != yyyymmdd) {
                return i;
            }
        }        
        return this.encodedData[0].length;
    }
    
    private int getDate(int i) {
        return Volatilities.getDate(this.encodedData[0][i]);
    }

    private float getVolatility(int i) {
        return Volatilities.getVolatility(this.encodedData[0][i]);
    }

    private String getType(int i) {
        return Volatilities.getType(this.encodedData[1][i]);
    }

    private int getMaturity(int i) {
        return Volatilities.getMaturity(this.encodedData[1][i]);
    }

    private float getStrike(int i) {
        return Volatilities.getStrike(this.encodedData[1][i]);
    }

    @Override
    public long getUnderlyingId() {
        return this.underyingId;
    }

    @Override
    public Iterator<Daily> getDailies() {
        return new Iterator<Daily>() {
            private int n = 0;
                        
            @Override
            public boolean hasNext() {
                return this.n < encodedData[0].length;
            }

            @Override
            public Daily next() {
                if (!hasNext()) {
                    throw new IllegalStateException();
                }
                final DailyIterator result = new DailyIterator(this.n);
                this.n = result.to;
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
