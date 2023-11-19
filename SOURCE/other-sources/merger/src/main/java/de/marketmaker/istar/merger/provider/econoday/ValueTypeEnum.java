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
public enum ValueTypeEnum {
    Consensus(0),
    Actual(1),
    Revised(2),
    Previous(3),
    ConsensusRangeFrom(4),
    ConsensusRangeTo(5),;

    private final int value;

    private ValueTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ValueTypeEnum fromValue(int val) {
        switch (val) {
            case 0:
                return Consensus;
            case 1:
                return Actual;
            case 2:
                return Revised;
            case 3:
                return Previous;
            case 4:
                return ConsensusRangeFrom;
            case 5:
                return ConsensusRangeTo;
            default:
                throw new UnsupportedOperationException("unknown value type value: " + val);
        }
    }
}
