/*
 * DpScheduler.java
 *
 * Created on 26.02.2004 09:24:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import de.marketmaker.istar.common.util.concurrent.PriorityExecutorService;

/**
 * Scheduler that keeps track of when dp files need to be written. Write triggers are
 * forwarded to a DpWriter instance. In order to ensure sequential processing, this
 * object runs its own thread of control in which it dispatches write requests to the
 * DpWriter; the requests are added by means of a TimeScheduler.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class DpScheduler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DPFILE_KEY = "dpfile";

    /**
     * scheduler for the DpFiles's time triggers
     */
    private TaskScheduler scheduler;

    /**
     * pause for this many milliseconds after writing a file
     */
    private volatile long sleepTime = 1000;

    /**
     * Contains names of all files that have been triggered to be written but have not yet
     * been written. Use a ConcurrentMap because there is no ConcurrentSet...
     */
    private final ConcurrentMap<String, String> pendingJobs = new ConcurrentHashMap<>();

    /**
     * performs the actual writing of output files.
     */
    private PriorityExecutorService executorService;

    /**
     * object that can process write requests.
     */
    private DpManager manager;

    public void setManager(DpManager manager) {
        this.manager = manager;
    }

    public void setExecutorService(PriorityExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setScheduler(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    void addJobFor(DpFile dpf) throws Exception {
        for (String cronExpression : dpf.getCronExpressions()) {
            final ScheduledFuture<?> f = this.scheduler.schedule(() -> jobTriggered(dpf),
                    new CronTrigger(cronExpression));
            dpf.add(f);
        }
    }

    void jobTriggered(final DpFile dpf) {
        final String spec = dpf.getFile().getAbsolutePath();

        if (this.pendingJobs.putIfAbsent(spec, spec) != null) {
            this.logger.warn("<jobTriggered> already pending " + spec);
            return;
        }

        try {
            this.executorService.submit(dpf.getPriority(), () -> {
                try {
                    runWriteJob(spec);
                } finally {
                    pendingJobs.remove(spec);
                }
            });
        } catch (RejectedExecutionException e) {
            // might happen if executor is in shutdown.
            this.logger.warn("<jobTriggered> executor rejected " + spec);
        }
    }

    private void runWriteJob(String spec) {
        final long took = this.manager.doWrite(spec);

        try {
            TimeUnit.MILLISECONDS.sleep(Math.min(took * 2, this.sleepTime));
        } catch (InterruptedException e) {
            this.logger.warn("<runWriteJob> interrupted while sleeping?!");
            Thread.currentThread().interrupt();
        }
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}
