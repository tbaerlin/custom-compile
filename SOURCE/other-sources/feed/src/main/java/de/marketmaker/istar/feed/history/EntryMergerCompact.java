/*
 * TickDataMerger.java
 *
 * Created on 28.09.12 14:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author zzhao
 */
public class EntryMergerCompact<T extends MutableEntry> extends EntryMergerJoin<T> {

    public EntryMergerCompact(int pivotDays, Class<T> clazz) {
        super(pivotDays, clazz);
    }

    @Override
    public byte[] merge(byte[] base, byte[] delta) {
        if (null == base && null == delta) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteBuffer bb = ByteBuffer.allocate(
                (null == base ? 0 : base.length) + (null == delta ? 0 : delta.length));

        if (null != delta) {
            bb.put(delta);
        }
        if (null != base) {
            fromBuffer(ByteBuffer.wrap(base), bb);
        }

        return Arrays.copyOfRange(bb.array(), 0, bb.position());
    }
}
