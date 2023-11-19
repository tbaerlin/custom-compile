/*
 * EconodayMetaEnum.java
 *
 * Created on 30.03.12 13:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

/**
 * @author zzhao
 */
public enum EconodayMetaDataEnum {
    Country("b.country"),
    Frequency("b.frequency"),
    Event("r.event_code");

    private final String columnName;

    private EconodayMetaDataEnum(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
