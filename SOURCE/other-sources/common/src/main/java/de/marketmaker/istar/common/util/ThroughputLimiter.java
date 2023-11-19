/*
 * ThroughputLimiter.java
 *
 * Created on 08.08.2006 08:44:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.concurrent.TimeUnit;

/**
 * Used to limit the number of times a certain action will be performed per second.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ThroughputLimiter {
    public static final ThroughputLimiter UNLIMITED = new ThroughputLimiter(0, TimeUnit.SECONDS, 0);

    private final long duration;

    private final int limit;

    private int count = 0;

    private long nextTime;

    public ThroughputLimiter(int maxNumPerSecond) {
        this(maxNumPerSecond, 10);
    }

    public ThroughputLimiter(int maxNumPerSecond, int batchesPerSec) {
        this(1000 / batchesPerSec,
                TimeUnit.MILLISECONDS, Math.max(1, maxNumPerSecond / batchesPerSec));
        if (maxNumPerSecond < 1) {
            throw new IllegalArgumentException(maxNumPerSecond + " < 1");
        }
        if (batchesPerSec > 1000) {
            throw new IllegalArgumentException(batchesPerSec + " > 1000");
        }
    }

    /**
     * Allows <code>limit</code> actions per <code>time</code>, which is given in <code>unit</code>.
     * @param time duration in which no more than limit actions should occur
     * @param unit unit of time
     * @param limit number of actions, 0 = unlimited.
     */
    public ThroughputLimiter(int time, TimeUnit unit, int limit) {
        this.limit = Math.max(0, limit);
        this.count = 0;
        this.duration = TimeUnit.NANOSECONDS.convert(time, unit);
    }

    /**
     * Allows <code>limit</code> actions per <code>millis</code> milliseconds.
     * @param millis interval for limiting
     * @param limit maximum number of actions
     * @return new instance
     */
    public static ThroughputLimiter perMillis(int millis, int limit) {
        return new ThroughputLimiter(millis, TimeUnit.MILLISECONDS, limit);
    }

    /**
     * To be called for each action limited by this object. If this object detects that the
     * desired throughput is exceeded, it will block the calling thread.
     */
    public float ackAction() {
        return ackActions(1);
    }

    /**
     * To be called when a number of actions are submitted as a batch update.
     * If this object detects that the
     * desired throughput is exceeded, it will block the calling thread.
     */
    public float ackActions(int num) {
        if (this.limit == 0) {
            return 0;
        }

        final long startTime;

        synchronized (this) {
            if (count == 0) {
                this.nextTime = System.nanoTime() + this.duration;
            }

            this.count += num;
            if (this.count < this.limit) {
                return 0;
            }

            startTime = System.currentTimeMillis();
            final long now = System.nanoTime();
            if (now < this.nextTime) {
                final int periods = Math.max(1, this.count / this.limit);
                sleep((this.nextTime - now) * periods);
            }
            this.count = 0;
        }

        return (System.currentTimeMillis() - startTime) / 1000;
    }

    private void sleep(long sleepTime) {
        try {
            TimeUnit.NANOSECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }

    public String toString() {
        if (this == UNLIMITED) {
            return "ThroughputLimiter.UNLIMITED";
        }
        return "ThroughputLimiter[limit=" + this.limit + ", duration=" + this.duration / (1000 * 1000) + "ms]";
    }


    public static void main(String[] args) {
        ThroughputLimiter tl = new ThroughputLimiter(100);
        tl = new ThroughputLimiter(1, TimeUnit.MINUTES, Integer.parseInt("8"));
        System.out.println("tl = " + tl);
        int n = 0;
        final long now = System.currentTimeMillis();
        while (n++ < 1500) {
            tl.ackActions(1);
        }
        System.out.println("took = " + (System.currentTimeMillis() - now));
    }

}
