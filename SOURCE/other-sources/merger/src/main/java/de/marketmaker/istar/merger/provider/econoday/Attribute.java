/*
 * Attribute.java
 *
 * Created on 15.03.12 16:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author zzhao
 */
public enum Attribute {
    Actual(1, "Actual Available"),
    Consensus(2, "Consensus Available"),
    MarketMover(4, "Market Mover"),
    Highlight(8, "Highlight Available"),
    None(16, "Non Timed / Non Commentary Item"),
    Commentary(32, "Commentary Item"),
    Attention(64, "Merit extra Attention"),
    Revised(128, "Revised Data Available");

    private final int bit;

    private final String info;

    private Attribute(int bit, String info) {
        this.bit = bit;
        this.info = info;
    }

    public int getBit() {
        return bit;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return this.info;
    }

    public static Set<Attribute> fromValue(int val) {
        final EnumSet<Attribute> ret = EnumSet.noneOf(Attribute.class);
        for (Attribute attr : values()) {
            if ((attr.bit & val) == attr.bit) {
                ret.add(attr);
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        final Set<Attribute> set = fromValue(5);
        System.out.println(set);
    }
}
