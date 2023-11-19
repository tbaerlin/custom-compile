/*
 * TimeTracer.java
 *
 * Created on 22.01.2016 11:53
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Allows the measurement of elapsed milliseconds while permitting that the measurement can be suspended and resumed.
 * Records also traces of the elapsed time and time periods. Traces are stored until reset has been called.
 * @author mdick
 */
public class TimeTracer {
    private static final long ONE_SECOND = 1000;

    private static final long ONE_MINUTE = 60 * ONE_SECOND;

    private static final long ONE_HOUR = 60 * ONE_MINUTE;

    private long totalTime;

    private long startedAt;

    private final ArrayList<TimeTrace> traces;

    private HashMap<String, TimeTrace> timeTraces;

    private final Supplier<Long> millisSupplier;

    /**
     * Starts the timer immediately.
     */
    public TimeTracer() {
        this(true);
    }

    /**
     * Starts the timer immediately.
     * @param millisSupplier supplies the system time in milli seconds.
     */
    public TimeTracer(Supplier<Long> millisSupplier) {
        this(true, millisSupplier);
    }

    /**
     * Starts the timer depending on <code>start</code>.
     * Uses the system time as millis supplier.
     * @param start whether the timer should be started upon creation.
     */
    public TimeTracer(boolean start) {
        this(start, System::currentTimeMillis);
    }

    /**
     * Starts the timer depending on <code>start</code>.
     * @param start whether the timer should be started upon creation.
     * @param millisSupplier supplies the system time in milli seconds.
     */
    public TimeTracer(boolean start, Supplier<Long> millisSupplier) {
        this.millisSupplier = millisSupplier;
        this.traces = new ArrayList<>();
        this.timeTraces = new HashMap<>();
        if (start) {
            start();
        }
    }

    /**
     * @return Elapsed time and traces as string.
     */
    @NonNLS
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append("Total: ")
                .append(toTimeString(getElapsedMs()))
                .append('\n');
        for (TimeTrace trace : this.traces) {
            sb.append('\t')
                    .append(trace.getWhat())
                    .append(": ");
            if (trace.isPeriod()) {
                sb.append(toTimeString(trace.getStartedAtMs()))
                        .append(" period: ");
            }
            sb.append(toTimeString(trace.getElapsedMs()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String toTimeString(long n) {
        final long h = n / ONE_HOUR;
        final long m = (n % ONE_HOUR) / ONE_MINUTE;
        final long s = (n % ONE_MINUTE) / ONE_SECOND;
        final long ms = (n % ONE_SECOND);
        return h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s + "." +
                (ms < 10 ? "00" : (ms < 100 ? "0" : "")) + ms;
    }

    public List<TimeTrace> getTraces() {
        return Collections.unmodifiableList(this.traces);
    }

    /**
     * Start measuring elapsed time (again).
     */
    public void start() {
        if (this.startedAt != 0) {
            throw new IllegalStateException("already started"); // $NON-NLS$
        }
        this.startedAt = this.millisSupplier.get();
    }

    /**
     * Stop measuring elapsed time (and wait for start measuring again).
     */
    public void stop() {
        if (this.startedAt == 0) {
            return;
        }
        final long elapsedMs = getElapsedMs();
        trace("stop", elapsedMs);
        this.totalTime += elapsedMs;
        this.startedAt = 0;
    }

    public boolean isRunning() {
        return this.startedAt != 0;
    }

    /**
     * Trace elapsed time.
     * @param what a name or message used to identify the trace within the traces.
     */
    public void trace(String what) {
        if (!isRunning()) {
            throw new IllegalStateException("not running");
        }
        trace(what, getElapsedMs());
    }

    private void trace(String what, long elapsed) {
        this.traces.add(new TimeTrace(what, elapsed));
    }

    /**
     * Start a trace period.
     * @param key identifier of the trace period.
     */
    public void startTrace(String key) {
        if (!isRunning()) {
            throw new IllegalStateException("not running");
        }
        if (this.timeTraces.containsKey(key)) {
            throw new IllegalStateException("trace \"" + key + "\" already started");
        }
        final long elapsed = this.millisSupplier.get();
        this.timeTraces.put(key, new TimeTrace(key, getElapsedMs(elapsed), elapsed));
    }

    /**
     * Finish a trace period.
     * @param key identifier of the trace period.
     */
    public void stopTrace(String key) {
        stopTrace(key, null);
    }

    /**
     * Finish a trace period.
     * @param key identifier of the trace period.
     * @param message use the message instead of the <code>key</code> as {@linkplain TimeTrace#what}.
     */
    public void stopTrace(String key, String message) {
        final TimeTrace tt = this.timeTraces.remove(key);
        if (tt == null) {
            throw new IllegalStateException("no trace \"" + key + "\" started");
        }

        tt.elapsedMs = this.millisSupplier.get() - tt.elapsedMs;
        if (message != null) {
            tt.what = message;
        }

        this.traces.add(tt);
    }

    public boolean isTraceStarted(String key) {
        return this.timeTraces.containsKey(key);
    }

    /**
     * Set everything to 0; same as new TimeTaker(false).
     */
    public TimeTracer reset() {
        this.totalTime = 0;
        this.startedAt = 0;
        this.traces.clear();
        this.timeTraces.clear();
        return this;
    }

    /**
     * Returns the total elapsed time w/o the pauses between the {@link #start} and
     * {@link #stop} calls.
     * @return total elapsed time
     */
    public long getTotalElapsedMs() {
        return (this.startedAt == 0) ? this.totalTime :
                this.totalTime + (this.millisSupplier.get() - startedAt);
    }

    /**
     * Returns the elapsed time since the last {@link #start} (as opposed to the
     * the total elapsed time via {@link #getTotalElapsedMs}). If the timer is not currently
     * active (never started or currently stopped), returns total elapsed time
     * @return time since last start (if running) or total elapsed time
     */
    public long getElapsedMs() {
        return getElapsedMs(this.millisSupplier.get());
    }

    private long getElapsedMs(long millis) {
        return (this.startedAt == 0) ? this.totalTime :
                millis - this.startedAt;
    }

    /**
     * Returns the time when this time taker was started
     * @return time of last start
     */
    public long getStartedAt() {
        return this.startedAt;
    }

    public final static class TimeTrace {
        private final boolean period;

        private final long startedAtMs;

        private long elapsedMs;

        private String what;

        private TimeTrace(String what, long elapsed) {
            this.period = false;
            this.what = what;
            this.startedAtMs = 0L;
            this.elapsedMs = elapsed;
        }

        private TimeTrace(String what, long startedAtMs, long elapsed) {
            this.period = true;
            this.what = what;
            this.startedAtMs = startedAtMs;
            this.elapsedMs = elapsed;
        }

        public boolean isPeriod() {
            return period;
        }

        public long getStartedAtMs() {
            return startedAtMs;
        }

        public long getElapsedMs() {
            return elapsedMs;
        }

        public String getWhat() {
            return what;
        }

        @NonNLS
        @Override
        public String toString() {
            return "TimeTrace{" +
                    "period=" + period +
                    ", startedAtMs=" + startedAtMs +
                    ", elapsedMs=" + elapsedMs +
                    ", what='" + what + '\'' +
                    '}';
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TimeTrace)) return false;

            TimeTrace timeTrace = (TimeTrace) o;

            if (period != timeTrace.period) return false;
            if (startedAtMs != timeTrace.startedAtMs) return false;
            if (elapsedMs != timeTrace.elapsedMs) return false;
            return !(what != null ? !what.equals(timeTrace.what) : timeTrace.what != null);

        }

        @Override
        public int hashCode() {
            int result = (period ? 1 : 0);
            result = 31 * result + (int) (startedAtMs ^ (startedAtMs >>> 32));
            result = 31 * result + (int) (elapsedMs ^ (elapsedMs >>> 32));
            result = 31 * result + (what != null ? what.hashCode() : 0);
            return result;
        }
    }
}
