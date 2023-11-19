/*
 * SerializationUtil.java
 *
 * Created on 23.04.13 15:34
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.data.SnapField;

/**
 * @author oflege
 */
class SerializationUtil {
    private SerializationUtil() {
    }

    static TimeseriesProtos.Field serialize(SnapField sf, boolean useMillis) {
        final TimeseriesProtos.Field.Builder fb = TimeseriesProtos.Field.newBuilder();
        fb.setFieldId(sf.getId());
        switch (sf.getType()) {
            case PRICE:
                final BigDecimal bd = sf.getPrice();
                fb.setPriceValue(bd.unscaledValue().intValue());
                fb.setPriceExponent((byte) bd.scale());
                break;
            case TIME:
                final int mdpsTime = ((Number) sf.getValue()).intValue();
                if (useMillis) {
                    fb.setIntValue(decodeTime(mdpsTime) * 1000 + decodeTimeMillis(mdpsTime));
                }
                else {
                    fb.setIntValue(decodeTime(mdpsTime));
                }
                break;
            case DATE:
                // intentional fallthrough
            case NUMBER:
                fb.setIntValue(((Number) sf.getValue()).intValue());
                break;
            case STRING:
                fb.setStringValue(sf.getValue().toString());
                break;
        }
        return fb.build();
    }

    private static int decodeTimeMillis(int i) {
        return i & 0x7FFF;
    }

    private static int decodeTime(int i) {
        int hh = i >>> 27;
        int mm = (i >>> 21) & 0x3F;
        int ss = (i >>> 15) & 0x3F;
        return hh * 3600 + mm * 60 + ss;
    }

}
