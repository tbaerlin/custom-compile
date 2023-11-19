/*
 * StopWatchHolder.java
 *
 * Created on 21.08.2006 10:09:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import org.springframework.util.StopWatch;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StopWatchHolder {
    private static ThreadLocal<StopWatch> stopWatchHolder = new ThreadLocal<>();

    public static void setStopWatch(StopWatch stopWatch) {
        if (stopWatch != null) {
            stopWatchHolder.set(stopWatch);
        }
        else {
            stopWatchHolder.remove();
        }
    }

    public static StopWatch getStopWatch() {
        return stopWatchHolder.get();
    }
}
