/*
 * ExecutorServiceUtil.java
 *
 * Created on 12.03.2010 10:55:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from Java API example - recommended way to shutdown executor service.
 *
 * @author zzhao
 */
public final class ExecutorServiceUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceUtil.class);

    private ExecutorServiceUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    /**
     * Shuts down the given executor service as given below:
     * <ol>
     * <li>disable new tasks from being submitted</li>
     * <li>wait for termination with the given timeout in seconds</li>
     * <li>if the given executor service does not terminate within the given timeout,
     * the current task would be cancelled, then wait for termination again</li>
     * </ol>
     * If the given executor service does not terminate, a log message of level warn is emitted. If
     * the thread issuing the shutdown action is interrupted, the shutdown action would be re-issued,
     * afterwards the interrupt status of the issuing thread is restored.
     *
     * @param es an executor service
     * @param timeOutSeconds timeout in seconds
     */
    public static void shutdownAndAwaitTermination(ExecutorService es, int timeOutSeconds) {
        // shutdown execution service
        es.shutdown(); // disable new tasks from being submitted
        try {
            if (!es.awaitTermination(timeOutSeconds, TimeUnit.SECONDS)) {
                // time out
                es.shutdownNow(); // cancel currently executing tasks
                // wait a minute for tasks to respond to being cancelled
                if (!es.awaitTermination(timeOutSeconds, TimeUnit.SECONDS)) {
                    logger.warn("<shutdownAndAwaitTermination> executor service did not terminate");
                }
            }
            logger.info("<shutdownAndAwaitTermination> executor service stopped");
        } catch (InterruptedException e) {
            logger.warn("<shutdownAndAwaitTermination> terminating executor service interrupted", e);
            // (Re-)cancel if current thread also interrupted
            es.shutdownNow();
            // restore interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shuts down the given executor service as given below:
     * <ol>
     * <li>disable new tasks from being submitted</li>
     * <li>wait for termination with the given timeout</li>
     * </ol>
     * If the given executor service does not terminate, a log message of level warn is emitted. If
     * the thread issuing the shutdown action is interrupted, the interrupt status of the issuing
     * thread is restored.
     *
     * @param es an executor service
     * @param timeOut timeout value
     * @param unit time out unit
     */
    public static void waitForTermination(ExecutorService es, int timeOut, TimeUnit unit) {
        // shutdown execution service
        es.shutdown(); // disable new tasks from being submitted
        try {
            if (!es.awaitTermination(timeOut, unit)) {
                // time out
                logger.warn("<shutdownAndAwaitTermination> executor service did not terminate" +
                        "in " + timeOut + " " + unit);
            }
            logger.info("<shutdownAndAwaitTermination> executor service stopped");
        } catch (InterruptedException e) {
            logger.warn("<shutdownAndAwaitTermination> terminating executor service interrupted", e);
            // restore interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
