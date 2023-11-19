/*
 * SnapData.java
 *
 * Created on 13.01.2005 14:32:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import de.marketmaker.istar.common.lifecycle.Disposable;
import de.marketmaker.istar.domain.data.SnapRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SnapData {
    /**
     * Returns a byte array with snap data
     * @param copy whether the data has to be copied; if the data is not copied, the client is
     * expected to ensure that the data cannot be modified by any other thread during the time
     * the data is used.
     * @return byte array with snap data
     */
    byte[] getData(boolean copy);

    /**
     * Index and offset information for the data returned by {@link #getData(boolean)}
     * @return
     */
    IndexAndOffset getIndexAndOffset();

    /**
     * Returns whether this object has been initialized
     * @return
     */
    boolean isInitialized();

    /**
     * Initialize this object. If this object already contains data, it can be assumed that
     * data represents an older but more complete set of fields, which has to be merged with
     * the data that is already present. Typical for push:
     * <ol>
     * <li>register for updates
     * <li>request complete snapshot 
     * <li>update arrives: init with update
     * <li>snapshot arrives: init with snapshot, but keep data from updates as more current
     * </ol>
     * @param indexAndOffset index and offset information for data
     * @param data initial data
     */
    void init(IndexAndOffset indexAndOffset, byte[] data);

    /**
     * Creates a SnapRecord based on this object
     * @return a SnapRecord
     * @param nominalDelayInSeconds
     */
    SnapRecord toSnapRecord(int nominalDelayInSeconds);

    default void dispose() {
        // empty
    }
}
