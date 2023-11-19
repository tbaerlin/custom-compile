/*
 * SharedIntradayContext.java
 *
 * Created on 21.10.14 10:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of all intraday data accessed while processing a single molecule request. The molecule's
 * atoms might be processed in different threads; in order to ensure a consistent view of the
 * intraday data items, an instance of this class is stored in the
 * {@link de.marketmaker.istar.merger.context.RequestContext} and helps to ensure that the
 * intraday data for each symbol is only requested once.
 *
 * @author oflege
 */
public class SharedIntradayContext {
    private static final int MAP_SIZE_LOG_THRESHOLD = 5001;

    private static final int TICK_SIZE_THRESHOLD
            = Integer.getInteger("maxSharedTickSize", 134_217_728 /* 128mb */);

    public static SharedIntradayContext NULL = new SharedIntradayContext(null);

    private volatile Logger logger;

    private ConcurrentHashMap<String, SharedIntradayData> map
            = new ConcurrentHashMap<>();

    private final AtomicInteger tickSize = new AtomicInteger();

    public SharedIntradayContext() {
        this(new ConcurrentHashMap<String, SharedIntradayData>());
    }

    private SharedIntradayContext(ConcurrentHashMap<String, SharedIntradayData> map) {
        this.map = map;
    }

    public void clear() {
        if (this == NULL) {
            return;
        }
        this.map.clear();
        this.tickSize.set(0);
    }

    SharedIntradayData putIfAbsent(String key, SharedIntradayData value) {
        if (this == NULL) {
            return null;
        }
        final SharedIntradayData result = map.putIfAbsent(key, value);
        if (result == null) {
            logIfMapExceedsSizeLimit();
        }
        return result;
    }

    SharedIntradayData get(String key) {
        if (this == NULL) {
            return null;
        }
        return map.get(key);
    }

    public int size() {
        if (this == NULL) {
            return 0;
        }
        return map.size();
    }

    void incTickSize(int delta) {
        if (this == NULL) {
            return;
        }
        checkTickSize(this.tickSize.addAndGet(delta));
    }

    void checkTickSize() {
         checkTickSize(this.tickSize.get());
    }

    private void checkTickSize(int num) {
        if (num > TICK_SIZE_THRESHOLD) {
            throw new TickSizeException(TICK_SIZE_THRESHOLD, num);
        }
    }

    public List<String> keySet() {
        if (this == NULL || this.map.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(this.map.keySet());
    }

    private void logIfMapExceedsSizeLimit() {
        if (this.map.size() > MAP_SIZE_LOG_THRESHOLD && this.logger == null) {
            this.logger = LoggerFactory.getLogger(getClass());
            this.logger.error(getClass().getSimpleName() + " with " + this.map.size() + "entries!",
                    new Exception());
        }
    }
}
