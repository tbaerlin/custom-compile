/*
 * FieldDataConverter.java
 *
 * Created on 23.04.13 15:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.domain.data.LiteralSnapField.*;

/**
 * @author oflege
 */
public interface FieldDataConverter {
    FieldDataConverter DEFAULT = fd -> {
        int fid = VwdFieldOrder.getFieldId(fd.getId());
        if (fid == 0) {
            fd.skipCurrent();
            return null;
        }
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                return createNumber(fid, fd.getInt());
            case FieldData.TYPE_TIME:
                return createTime(fid, MdpsFeedUtils.decodeTime(fd.getInt()));
            case FieldData.TYPE_PRICE:
                return createPrice(fid, BigDecimal.valueOf(fd.getInt(), -fd.getByte()));
            case FieldData.TYPE_STRING:
                return fd.getLength() == 0
                        ? createString(fid, "")
                        : createString(fid, OrderedSnapRecord.toString(fd.getBytes()));
            default:
                throw new IllegalStateException("Unknown type: " + fd.getType());
        }
    };

    /**
     * Keeps numbers in the same format as they are stored in chicago, i.e., dates as yyyyMMdd,
     * times mdps-encoded.
     */
    FieldDataConverter RAW = fd -> {
        int fid = VwdFieldOrder.getFieldId(fd.getId());
        if (fid == 0) {
            fd.skipCurrent();
            return null;
        }
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                return createNumber(fid, fd.getInt());
            case FieldData.TYPE_TIME:
                return createTime(fid, fd.getInt());
            case FieldData.TYPE_PRICE:
                return createPrice(fid, BigDecimal.valueOf(fd.getInt(), -fd.getByte()));
            case FieldData.TYPE_STRING:
                return fd.getLength() == 0
                        ? createString(fid, "")
                        : createString(fid, OrderedSnapRecord.toString(fd.getBytes()));
            default:
                throw new IllegalStateException("Unknown type: " + fd.getType());
        }
    };

    SnapField convert(FieldData fd);
}
