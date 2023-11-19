/*
 * EodPriceMpc.java
 *
 * Created on 16.01.13 13:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author zzhao
 */
public class EodPrice {

    private int date;

    private LocalDate localDate;

    private Int2ObjectMap<String> values = new Int2ObjectArrayMap<>(32);

    EodPrice() {
    }

    void reset(int date) {
        this.values.clear();
        this.date = date;
        this.localDate = DateUtil.yyyyMmDdToLocalDate(date);
    }

    void put(int field, String val) {
        this.values.put(field, val);
    }

    public String getPrice(int field) {
        return this.values.get(field);
    }

    public int getDate() {
        return date;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public int size() {
        return this.values.size();
    }

    @Override
    public String toString() {
        return this.date + ": " + values;
    }

    public String toString(DecimalFormat nf) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.date).append(": {");
        boolean first = true;
        for (Map.Entry<Integer, String> entry : values.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry).append("=").append(nf.format(new BigDecimal(entry.getValue())));
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }
}
