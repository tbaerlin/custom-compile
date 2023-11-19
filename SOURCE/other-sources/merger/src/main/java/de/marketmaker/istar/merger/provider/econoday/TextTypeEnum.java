/*
 * FrequencyEnum.java
 *
 * Created on 15.03.12 10:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

/**
 * @author zzhao
 */
public enum TextTypeEnum {
    Highlights(0),
    ConsensusNotes(1);

    private final int value;

    private TextTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TextTypeEnum fromValue(int val) {
        switch (val) {
            case 0:
                return Highlights;
            case 1:
                return ConsensusNotes;
            default:
                throw new UnsupportedOperationException("unknown text type value: " + val);
        }
    }
}
