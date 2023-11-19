/*
 * IndexAndOffsetFactory.java
 *
 * Created on 28.10.2004 08:55:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import de.marketmaker.istar.common.util.SimpleBitSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IndexAndOffsetFactory {
    IndexAndOffset getExpandedIndexAndOffset(IndexAndOffset indexAndOffset, int fieldId);

    IndexAndOffset getShrunkIndexAndOffset(IndexAndOffset indexAndOffset, SimpleBitSet fieldIdsToKeep);

    IndexAndOffset getIndexAndOffset(int[] fieldIds);

    IndexAndOffset getEmptyIndexAndOffset();

    /**
     * Number of unique IndexAndOffset objects maintained in this factory
     * @return size
     */
    int size();
}
