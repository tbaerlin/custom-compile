/*
 * EconodayMetaDataKey.java
 *
 * Created on 30.03.12 13:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * @author zzhao
 */
public class EconodayMetaDataKey implements Comparable<EconodayMetaDataKey>, Serializable {

    private static final EconodayMetaDataKey COUNTRY = new EconodayMetaDataKey(EconodayMetaDataEnum.Country);

    private static final EconodayMetaDataKey FREQUENCY = new EconodayMetaDataKey(EconodayMetaDataEnum.Frequency);

    private static final EconodayMetaDataKey EVENT = new EconodayMetaDataKey(EconodayMetaDataEnum.Event);

    private final EconodayMetaDataEnum type;

    public EconodayMetaDataKey(EconodayMetaDataEnum type) {
        this.type = type;
    }

    public static EconodayMetaDataKey fromEnum(EconodayMetaDataEnum type) {
        switch (type) {
            case Country:
                return COUNTRY;
            case Frequency:
                return FREQUENCY;
            case Event:
                return EVENT;
            default:
                throw new UnsupportedOperationException("no support for: " + type);
        }
    }

    public String getType() {
        return this.type.name().toLowerCase();
    }

    public String getName() {
        return this.type.name().toLowerCase();
    }

    public boolean isEnum() {
        return false;
    }

    @Override
    public int compareTo(EconodayMetaDataKey o) {
        return this.type.compareTo(o.type);
    }

    protected Object readResolve() throws ObjectStreamException {
        return fromEnum(this.type);
    }
}
