/*
 * EodHistoryArchive.java
 *
 * Created on 11.12.12 14:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.File;
import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

/**
 * @author zzhao
 */
public interface EodHistoryArchive {

    static final String CONTENT_TYPE = "EOD";

    static final int LENGTH_BITS = 24;

    File getUpdateLockFile();

    void begin(int date) throws IOException;

    void update(long quote, int date, Int2ObjectSortedMap<byte[]> data);

    void finish() throws IOException;
}
