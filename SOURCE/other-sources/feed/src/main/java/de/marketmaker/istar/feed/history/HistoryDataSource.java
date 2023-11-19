/*
 * HistoryDataSource.java
 *
 * Created on 28.09.12 10:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;

/**
 * @author zzhao
 */
public interface HistoryDataSource<T extends Comparable<T>> {

    void transfer(HistoryWriter<T> writer) throws IOException;
}
