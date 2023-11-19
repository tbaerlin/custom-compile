/*
 * EodTick.java
 *
 * Created on 06.12.12 15:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import de.marketmaker.istar.merger.provider.history.eod.BCD;

/**
 * @author zzhao
 */
class EodTick {

    private long quote;

    private int date;

    private final Int2ObjectSortedMap<byte[]> values = new Int2ObjectAVLTreeMap<>();

    void reset(long quote, int date) {
        this.quote = quote;
        this.date = date;
        this.values.clear();
    }

    long getQuote() {
        return quote;
    }

    void withField(byte fieldNum, byte[] fieldVal) {
        this.values.put(fieldNum, fieldVal);
    }

    boolean isEmpty() {
        return this.values.isEmpty();
    }

    int getDate() {
        return date;
    }

    public Int2ObjectSortedMap<byte[]> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "EodTick{" + quote + ":" + date + ":" + valuesString() + "}";
    }

    private String valuesString() {
        if (this.values.isEmpty()) {
            return "{}";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Int2ObjectMap.Entry<byte[]> entry : values.int2ObjectEntrySet()) {
            sb.append(entry.getIntKey()).append("->").append(BCD.toBCDString(entry.getValue()))
                    .append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), "}");
        return sb.toString();
    }
}
