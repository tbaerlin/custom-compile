/*
 * FieldDataUtil.java
 *
 * Created on 30.08.12 16:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
public final class FieldDataUtil {
    private FieldDataUtil() {
    }

    public static long getPrice(FieldData data) {
        return MdpsFeedUtils.encodePrice(data.getInt(), data.getByte());
    }

    public static int getType(VwdFieldDescription.Field f) {
        switch (f.type()) {
            case TIME:
                return FieldData.TYPE_TIME;
            case UINT:
                if (f == VwdFieldDescription.BIG_SOURCE_ID) {
                    return FieldData.TYPE_STRING;
                }
                // intentional fall-through
            case USHORT:
                // intentional fall-through
            case DATE:
                return FieldData.TYPE_INT;
            case PRICE:
                return FieldData.TYPE_PRICE;
            case STRING:
                return FieldData.TYPE_STRING;
            default:
                throw new IllegalArgumentException(String.valueOf(f.type()));
        }
    }

    public static int getType(int fid) {
        return getType(VwdFieldDescription.getField(fid));
    }

    public static byte[] getFieldsUpTo(FieldData fd, int maxOid) {
        int oid;
        while ((oid = fd.readNext()) > 0 && oid <= maxOid) {
            fd.skipCurrent();
        }
        return (oid > 0) ? fd.getAsByteArrayBeforeCurrent() : fd.getAsByteArray();
    }

    public static Map<Integer, Object> getFieldsById(FieldData fd) {
        return getFieldsById(fd, false);
    }

    public static Map<Integer, Object> getFieldsById(FieldData fd, boolean escapeNulls) {
        Map<Integer, Object> result = new LinkedHashMap<>();
        int id;
        while ((id = fd.readNext()) > 0) {
            result.put(VwdFieldOrder.getField(id).id(), readObject(fd, escapeNulls));
        }
        return result;
    }

    public static Map<VwdFieldDescription.Field, Object> getTickFields(FieldData fd) {
        return getFields(fd, true, false);
    }

    public static Map<VwdFieldDescription.Field, Object> getFields(FieldData fd) {
        return getFields(fd, false);
    }

    public static Map<VwdFieldDescription.Field, Object> getFields(FieldData fd, boolean escapeNulls) {
        return getFields(fd, false, escapeNulls);
    }

    private static Map<VwdFieldDescription.Field, Object> getFields(FieldData fd, boolean tick, boolean escapeNulls) {
        Map<VwdFieldDescription.Field, Object> result = new TreeMap<>(VwdFieldOrder.BY_ORDER);
        if (tick) {
            result.put(VwdFieldDescription.ADF_Zeit, MdpsFeedUtils.decodeTime(fd.getInt()));
        }
        int id;
        while ((id = fd.readNext()) > 0) {
            result.put(VwdFieldOrder.getField(id), readObject(fd, escapeNulls));
        }
        return result;
    }

    private static Object readObject(FieldData fd, boolean escapeNulls) {
        return OrderedSnapRecord.getFieldValue(fd, escapeNulls);
    }
}
