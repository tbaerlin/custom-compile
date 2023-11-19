/*
 * MuxOutput.java
 *
 * Created on 12.03.14 07:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Implement this interface to receive feed data records from multicast
 *
 * @author oflege
 */
public interface MuxOutput {

    default void onInClosed() {
        // empty
    }

    default void reset() {
        // empty
    }

    boolean isAppendOnlyCompleteRecords();

    void append(ByteBuffer in) throws IOException;

}
