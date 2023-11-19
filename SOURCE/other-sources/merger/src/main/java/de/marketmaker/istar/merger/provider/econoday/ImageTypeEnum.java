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
public enum ImageTypeEnum {
    Chart_1(0),
    Chart_2(1),
    Grid_1(2);

    private final int value;

    private ImageTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ImageTypeEnum fromValue(int val) {
        switch (val) {
            case 0:
                return Chart_1;
            case 1:
                return Chart_2;
            case 2:
                return Grid_1;
            default:
                throw new UnsupportedOperationException("unknown image type value: " + val);
        }
    }
}
