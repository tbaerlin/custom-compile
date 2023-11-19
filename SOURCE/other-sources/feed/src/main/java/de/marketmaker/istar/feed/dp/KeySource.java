/*
 * KeySource.java
 *
 * Created on 17.12.2009 17:22:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.IOException;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;

/**
 * @author oflege
 */
public interface KeySource {
    boolean hasNext() throws IOException;

    FeedData nextFeedData();

    ByteString getAlias();

    void close() throws IOException;
}
