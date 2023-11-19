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
public enum FrequencyEnum {
    NotSpecified(0, "None Specified"),
    Weekly(1, "Weekly"),
    Monthly(2, "Monthly"),
    Quarterly(3, "Quarterly"),
    Fortnight(4, "Every Other Week"),
    SixWeeks(5, "About Every 6 weeks");

    private final int value;

    private final String info;

    private FrequencyEnum(int value, String info) {
        this.info = info;
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.info;
    }

    public static FrequencyEnum fromValue(int val) {
        switch (val) {
            case 0:
                return NotSpecified;
            case 1:
                return Weekly;
            case 2:
                return Monthly;
            case 3:
                return Quarterly;
            case 4:
                return Fortnight;
            case 5:
                return SixWeeks;
            default:
                throw new UnsupportedOperationException("unknown frequency value: " + val);
        }
    }
}
