/*
 * OrderedSnapData.java
 *
 * Created on 13.11.12 11:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.snap.SnapData;

/**
 * @author oflege
 */
public interface OrderedSnapData extends SnapData {
    /**
     * @return timestamp of last update, encoded as
     * {@link de.marketmaker.istar.feed.DateTimeProvider.Timestamp#feedTimestamp}
     */
    int getLastUpdateTimestamp();

    void setLastUpdateTimestamp(int lastUpdateTimestamp);

    void init(OrderedSnapRecord src);
}

