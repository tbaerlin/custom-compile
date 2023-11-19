/*
 * Volatilities.java
 *
 * Created on 15.12.11 11:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.risk;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.ImpliedVolatilities;
import de.marketmaker.istar.merger.provider.protobuf.ProviderProtos;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author oflege
 */
class Volatilities {

    private static class Item implements Comparable<Item> {
        private final long key;

        private long[] values = new long[10];

        private int num = 0;

        private Item(long key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return getStrike() + "/" + getMaturity() + "/" + getType() + "/#" + num;
        }

        @Override
        public int compareTo(Item o) {
            return (this.key < o.key) ? -1 : ((this.key == o.key) ? 0 : 1);
        }

        public void add(final long encodedVolatility) {
            for (int i = 0; i < num; i++) {
                // sometimes different generations of an option have the same value, only store them once
                if (values[i] == encodedVolatility) {
                    return;
                }
            }
            if (num == this.values.length) {
                this.values = Arrays.copyOf(this.values, this.values.length + 5);
            }
            this.values[this.num++] = encodedVolatility;
        }

        public float getStrike() {
            return Volatilities.getStrike(this.key);
        }

        public int getMaturity() {
            return Volatilities.getMaturity(this.key);
        }

        public String getType() {
            return Volatilities.getType(this.key);
        }

        public int countValuesFrom(long day) {
            int n = 0;
            for (int i = 0; i < this.num; i++) {
                if (this.values[i] > day) {
                    n++;
                }
            }
            return n;
        }
    }

    private static long encodeVolaAndDate(ProviderProtos.ImpliedVolatility iv) {
        return (((long) iv.getDatum()) << 32) + impliedVolaToInt(iv);
    }

    static float getVolatility(long encodedVolatility) {
        return Float.intBitsToFloat((int)encodedVolatility);
    }

    static int getDate(long encodedVolatility) {
        return (int)(encodedVolatility >> 32);
    }

    static String getType(long encodedOption) {
        return ((encodedOption & 0x1) == 0) ? "C" : "P";
    }

    static int getMaturity(long encodedOption) {
        return (int) ((encodedOption >> 36));
    }

    static float getStrike(long encodedOption) {
        return Float.intBitsToFloat((int) (encodedOption >> 4));
    }

    private static int strikeToInt(ProviderProtos.ImpliedVolatility iv) {
        return stringFloatAsInt(iv.getStrike());
    }

    private static int impliedVolaToInt(ProviderProtos.ImpliedVolatility iv) {
        return stringFloatAsInt(iv.getAdf333());
    }

    private static int stringFloatAsInt(final String value) {
        return Float.floatToIntBits(Float.parseFloat(value));
    }

    final long iid;

    private List<Item> items = new ArrayList<>();

    Volatilities(Long iid) {
        this.iid = iid;
    }

    @Override
    public String toString() {
        return this.iid + ".iid";
    }

    public void add(ProviderProtos.ImpliedVolatility iv) {
        long key = toKey(iv);
        final int i = binarySearch(key);
        Item item;
        if (i < 0) {
            item = new Item(key);
            this.items.add(-i - 1, item);
        } else {
            item = this.items.get(i);
        }
        item.add(encodeVolaAndDate(iv));
    }

    private int binarySearch(long key) {
        int low = 0;
        int high = this.items.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = this.items.get(mid).key;
            int cmp = midVal < key ? -1 : (midVal == key ? 0 : 1);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }


    private long toKey(ProviderProtos.ImpliedVolatility iv) {
        return (((long) iv.getMaturity()) << 36) + (((long) strikeToInt(iv)) << 4) +
                +("P".equals(iv.getOstype()) ? 1 : 0);
    }

    public ImpliedVolatilities getVolatilities(LocalDate from) {
        final long day = (from != null ? DateUtil.toYyyyMmDd(from) : 0L) << 32;
        int numResults = 0;
        for (Item item : this.items) {
            numResults += item.countValuesFrom(day);
        }
        final long[][] data = new long[2][numResults];
        int n = 0;
        for (Item item : items) {
            for (int i = 0; i < item.num; i++) {
                if (item.values[i] > day) {
                    data[0][n] = item.values[i];
                    data[1][n] = item.key;
                    n++;
                }
            }
        }
        if (data[0].length > 0) {
            sortResult(data);
        }
        return new ImpliedVolatilitiesImpl(this.iid, data);
    }

    private void sortResult(long[][] data) {
        ArraysUtil.sort(data[0], data[1]); // sort by date of vola (in high bits of data[0][i])
        int fromIndex = 0;
        for (int i = 1; i < data[0].length; i++) {
            if (getDate(data[0][fromIndex]) != getDate(data[0][i])) {
                ArraysUtil.sort(data[1], fromIndex, i, data[0]);
                fromIndex = i;
            }
        }
        ArraysUtil.sort(data[1], fromIndex, data[1].length, data[0]);
    }

}
