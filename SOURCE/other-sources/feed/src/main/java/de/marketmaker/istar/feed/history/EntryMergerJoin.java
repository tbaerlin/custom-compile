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
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author zzhao
 */
public class EntryMergerJoin<T extends MutableEntry> implements EntryMerger {

    private final int pivotDays;

    private final Class<T> clazz;

    public EntryMergerJoin(int pivotDays, Class<T> clazz) {
        this.pivotDays = pivotDays;
        this.clazz = clazz;
    }

    @Override
    public byte[] merge(byte[] base, byte[] delta) {
        if (null == base && null == delta) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteBuffer bb = ByteBuffer.allocate(
                (null == base ? 0 : base.length) + (null == delta ? 0 : delta.length));

        if (null == base) {
            fromBuffer(ByteBuffer.wrap(delta), bb);
        }
        else if (null == delta) {
            fromBuffer(ByteBuffer.wrap(base), bb);
        }
        else {
            // merge
            final MutableEntryIterator<T> baseIt =
                    new MutableEntryIterator<>(ByteBuffer.wrap(base), this.clazz);
            final MutableEntryIterator<T> deltaIt =
                    new MutableEntryIterator<>(ByteBuffer.wrap(delta), this.clazz);

            T baseEntry = nextEntry(baseIt);
            T deltaEntry = nextEntry(deltaIt);

            while (null != baseEntry || null != deltaEntry) {
                final int diff = compare(baseEntry, deltaEntry);
                if (diff == 0) {
                    EntryFactory.toBuffer(bb, deltaEntry);
                    baseEntry = nextEntry(baseIt);
                    deltaEntry = nextEntry(deltaIt);
                }
                else if (diff > 0) {
                    if (null != deltaEntry) {
                        EntryFactory.toBuffer(bb, deltaEntry);
                        deltaEntry = nextEntry(deltaIt);
                    }
                    else {
                        EntryFactory.toBuffer(bb, baseEntry);
                        baseEntry = nextEntry(baseIt);
                    }
                }
                else {
                    if (null != baseEntry) {
                        EntryFactory.toBuffer(bb, baseEntry);
                        baseEntry = nextEntry(baseIt);
                    }
                    else {
                        EntryFactory.toBuffer(bb, deltaEntry);
                        deltaEntry = nextEntry(deltaIt);
                    }
                }
            }
        }

        return Arrays.copyOfRange(bb.array(), 0, bb.position());
    }

    private int compare(T baseEntry, T deltaEntry) {
        // based on the order of days, which is descending
        if (null != baseEntry && null != deltaEntry) {
            return deltaEntry.getDays() - baseEntry.getDays();
        }
        else if (null == baseEntry) {
            return 1;
        }
        else {
            return -1;
        }
    }

    private T nextEntry(Iterator<T> it) {
        if (!it.hasNext()) {
            return null;
        }

        final T entry = it.next();
        return entry.getDays() < this.pivotDays ? null : entry;
    }

    protected void fromBuffer(ByteBuffer from, ByteBuffer to) {
        final MutableEntryIterator<T> it = new MutableEntryIterator<>(from, this.clazz);
        while (it.hasNext()) {
            final T entry = it.next();
            if (entry.getDays() < this.pivotDays) {
                break;
            }
            else {
                EntryFactory.toBuffer(to, entry);
            }
        }
    }
}
