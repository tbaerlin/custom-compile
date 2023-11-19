/*
 * TimeTaker.java
 *
 * Created on 18.11.2004 13:57:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

/**
 * Allows to measure elapsed milliseconds while permitting that the measurement
 * can be suspended and resumed. <p>
 * This class is thread-compatible (requires external synchronization).
 *
 * @author Oliver Flege
 * @version $Id: TimeTaker.java,v 1.4 2004/11/30 16:28:47 tkiesgen Exp $
 */
public class TimeTaker {
    private long totalTime;
    private long startedAt;

    private static final long ONE_SECOND = 1000;
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;

    /**
     * Standard default constructor, starts the timer immediately.
     */
    public TimeTaker() {
        this(true);
    }

    /**
     * Constructor.
     * @param start whether the timer should be started upon creation.
     */
    public TimeTaker(boolean start) {
        if (start) {
            start();
        }
    }

    /**
     * Returns formatted {@link #getElapsedMs}.
     * @return elapsed time as string.
     */
    public String toString() {
        final long n = getElapsedMs();
        final long h = n / ONE_HOUR;
        final long m = (n % ONE_HOUR) / ONE_MINUTE;
        final long s = (n % ONE_MINUTE) / ONE_SECOND;
        final long ms = (n % ONE_SECOND);
        return h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s + "." +
                (ms<10?"00":(ms<100?"0":""))+ms;
    }

    /**
     * initLogging measuring elapsed time (again).
     */
    public void start() {
        this.startedAt = System.currentTimeMillis();
    }

    /**
     * stop measuring elapsed time (and wait for initLogging measuring again).
     */
    public void stop() {
        this.totalTime += getElapsedMs();
        this.startedAt = 0;
    }

    /**
     * set everything to 0; equal to new TimeTaker().
     */
    public TimeTaker reset() {
        this.totalTime = 0;
        this.startedAt = 0;
        return this;
    }

    /**
     * Returns the total elapsed time w/o the pauses between the {@link #start} and
     * {@link #stop} calls.
     * @return total elapsed time
     */
    public long getTotalElapsedMs() {
        return (this.startedAt == 0) ? this.totalTime :
            this.totalTime + (System.currentTimeMillis() - startedAt);
    }

    /**
     * Returns the elapsed time since the last {@link #start} (as opposed to the
     * the total elapsed time via {@link #getTotalElapsedMs}). If the timer is not currently
     * active (never started or currently stopped), returns total elapsed time
     * @return time since last start (if running) or total elapsed time
     */
    public long getElapsedMs() {
        return (this.startedAt == 0) ? this.totalTime :
                System.currentTimeMillis() - this.startedAt;
    }

    /**
     * Returns the time when this time taker was started
     * @return time of last start
     */
    public long getStartedAt() {
        return this.startedAt;
    }
}
