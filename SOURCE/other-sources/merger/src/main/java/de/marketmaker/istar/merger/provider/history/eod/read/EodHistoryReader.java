/*
 * EodHistoryReader.java
 *
 * Created on 16.01.13 14:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import org.joda.time.Interval;

import de.marketmaker.istar.feed.history.HistoryUnit;

/**
 * @author zzhao
 */
interface EodHistoryReader extends Closeable {
    HistoryUnit getUnit();

    Interval getInterval();

    boolean contains(Interval another);

    void setFile(File historyFile) throws IOException;

    ByteBuffer loadData(long quote) throws IOException;

    ByteBuffer loadData(long quote, Interval interval) throws IOException;

    Map<Integer, ByteBuffer> loadData(long quote, Collection<Integer> fields) throws IOException;

    Map<Integer, ByteBuffer> loadData(long quote, Interval interval,
            Collection<Integer> fields) throws IOException;
}
