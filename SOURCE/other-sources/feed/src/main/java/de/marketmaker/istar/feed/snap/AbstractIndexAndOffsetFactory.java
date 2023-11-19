/*
 * IndexAndOffsetFactoryVwd.java
 *
 * Created on 28.10.2004 09:09:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

import de.marketmaker.istar.common.util.SimpleBitSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractIndexAndOffsetFactory implements IndexAndOffsetFactory {
    /**
     * Use WeakHashMap so we do not have to track reference counts but can still be sure to
     * garbage collect values that are no longer used.
     */
    private final Map<IndexAndOffset, WeakReference<IndexAndOffset>> map =
                    new WeakHashMap<>();

    private final IndexAndOffset EMPTY_IAO = getIndexAndOffset(new int[0]);

    private IndexAndOffset create(int[] fieldids) {
        Arrays.sort(fieldids);
        return new IndexAndOffset(this, new int[][]{
                fieldids, calculateOffsets(fieldids)
        });
    }

    public IndexAndOffset getEmptyIndexAndOffset() {
        return EMPTY_IAO;
    }

    public IndexAndOffset getIndexAndOffset(int[] fieldids) {
        final int[] tmp;
        if (fieldids.length == 0) {
            tmp = new int[] { Integer.MAX_VALUE };
        }
        else if (fieldids[fieldids.length - 1] != Integer.MAX_VALUE) {
            tmp = new int[fieldids.length + 1];
            System.arraycopy(fieldids, 0, tmp, 0, fieldids.length);
            tmp[fieldids.length] = Integer.MAX_VALUE;
        }
        else {
            tmp = new int[fieldids.length];
            System.arraycopy(fieldids, 0, tmp, 0, fieldids.length);
        }
        final IndexAndOffset iao = create(tmp);
        return getOrPutIfAbsent(iao);
    }

    public IndexAndOffset getExpandedIndexAndOffset(IndexAndOffset indexAndOffset, int fieldid) {
        if (fieldid < 0) {
            throw new IllegalArgumentException();
        }

        final int fieldindex = indexAndOffset.getIndex(fieldid);
        if (fieldindex >= 0) {
            return indexAndOffset;
        }

        return getExpandedIndexAndOffset(indexAndOffset, fieldid, -fieldindex - 1);
    }

    public IndexAndOffset getShrunkIndexAndOffset(IndexAndOffset indexAndOffset,
            SimpleBitSet fieldIdsToKeep) {

        int m = fieldIdsToKeep.cardinality();
        if (m >= (indexAndOffset.getArrayLength() - 1)) {
            throw new IllegalArgumentException("fieldIdsToKeep " + m + ">=" + indexAndOffset.getArrayLength());
        }

        final int[] tmpIndex = new int[m + 1];
        tmpIndex[m] = Integer.MAX_VALUE;

        int i = indexAndOffset.getArrayLength() - 1;
        while (i-- > 0) {
            if (fieldIdsToKeep.get(i)) {
                tmpIndex[--m] = indexAndOffset.getIndexArray()[i];
            }
        }

        return getOrPutIfAbsent(create(tmpIndex));
    }

    private IndexAndOffset getExpandedIndexAndOffset(IndexAndOffset indexAndOffset, int fieldid,
            final int newIndex) {

        final int[] tmpIndex = new int[indexAndOffset.getArrayLength() + 1];

        if (newIndex > 0) {
            System.arraycopy(indexAndOffset.getIndexArray(), 0, tmpIndex, 0, newIndex);
        }

        if (newIndex < indexAndOffset.getArrayLength()) {
            System.arraycopy(indexAndOffset.getIndexArray(), newIndex, tmpIndex, newIndex + 1, indexAndOffset.getArrayLength() - newIndex);
        }

        tmpIndex[newIndex] = fieldid;

        return getOrPutIfAbsent(create(tmpIndex));
    }

    private IndexAndOffset getOrPutIfAbsent(final IndexAndOffset key) {
        synchronized (this.map) {
            final WeakReference<IndexAndOffset> value = this.map.get(key);
            final IndexAndOffset existing = (value != null) ? value.get() : null;

            if (existing != null) {
                return existing;
            }

            this.map.put(key, new WeakReference<>(key));
            return key;
        }
    }

    public int size() {
        synchronized(this.map) {
            return this.map.size();
        }
    }

    private int[] calculateOffsets(final int[] fields) {
        int offset = 0;
        final int[] result = new int[fields.length];
        for (int i = 0; i < fields.length - 1; i++) {
            result[i] = offset;
            offset += getLength(fields[i]);
        }
        result[fields.length - 1] = offset;
        return result;
    }

    protected abstract int getLength(int fieldid);
}

