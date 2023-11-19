/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package de.marketmaker.istar.common.monitor;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.springframework.context.Lifecycle;


/**
 * <p>The ActiveMonitor is used to actively check a set of resources to see if they have
 * changed. It will poll the resources with a frequency as specified or if
 * unspecified with the default (60 seconds).</p>
 *
 * <p><emph>This class has no use for a {@link java.nio.file.WatchService} implementation.</emph></p>
 *
 * <p>An implementation of the {@link ActiveMonitor} with the use of {@link java.nio.file.WatchService}
 * has been reverted. {@link java.nio.file.WatchService} makes use of kernel functionality to detect changes of
 * local and remote file systems. Since only changes by the operating system kernel will be detected no changes
 * of remote systems can be detected.</p>
 */
@ThreadSafe
public class ActiveMonitor extends AbstractMonitor implements Lifecycle, Runnable {
    private static final long DEFAULT_FREQUENCY = 1000L * 60L;

    /**
     * The frequency to scan resources for changes measured
     * in milliseconds.
     */
    private volatile long frequency = DEFAULT_FREQUENCY;

    /**
     * The thread that does the monitoring.
     */
    @GuardedBy("this")
    private Thread monitorThread;

    private String threadName = "ActiveMonitor";

    /**
     * Set to false to shutdown the thread.
     */
    private volatile boolean keepRunning = true;

    private final boolean daemon;

    public ActiveMonitor() {
        this(true);
    }

    public ActiveMonitor(boolean daemon) {
        this.daemon = daemon;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Set the frequency with which the monitor
     * checks the resources. This can be changed
     * anytime and will be enabled the next time
     * through the check.
     * @param frequency the frequency to scan resources for changes
     */
    public void setFrequency(final long frequency) {
        this.frequency = frequency;
    }

    public synchronized void start() {
        if (this.monitorThread != null) {
            return;
        }
        this.logger.info("<start> " + this.threadName
                + ", daemon: " + this.daemon
                + ", frequency: " + this.frequency);
        this.keepRunning = true;
        this.monitorThread = new Thread(this, this.threadName);
        this.monitorThread.setDaemon(this.daemon);
        this.monitorThread.start();
    }

    public synchronized void stop() {
        if (this.monitorThread == null) {
            return;
        }
        this.keepRunning = false;
        this.monitorThread.interrupt();
        try {
            this.monitorThread.join();
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
        this.monitorThread = null;
        this.logger.info("<stop> " + this.threadName);
    }

    @Override
    public synchronized boolean isRunning() {
        return this.monitorThread != null;
    }

    public final void run() {
        try {
            while (this.keepRunning) {
                Thread.sleep(this.frequency);
                scanAllResources();
            }
        } catch (InterruptedException e) {
            // empty
        }
    }
}
