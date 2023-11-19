/*
 * PriorityExecutorService.java
 *
 * Created on 10.08.2007 09:21:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;

/**
 * Like an {@link java.util.concurrent.ExecutorService}, but supports different task priorities.
 * Tasks with a higher priority will be scheduled earlier than those with a lower priority, even
 * if the former have been submitted after the latter.<p>
 * Useful if actions would otherwise occur in parallel that have a negative impact on each other
 * (e.g., writing lots of data to files). Usually, some action has a higher priority than
 * another action. If both actions are performed using this service, they can be scheduled
 * according to that priority without having to rely on priorities assigned to threads.<p>
 *
 * @author Oliver Flege
 */
public interface PriorityExecutorService {
    /**
     * Submit task for execution so that it will be executed before any other sumitted (but not
     * yet scheduled) task with a lower priority.
     * @param priority taks's priority
     * @param task to be executed
     * @return Future for task
     */
    <T> Future<T> submit(int priority, Callable<T> task);

    /**
     * Submit task for execution so that it will be executed before any other sumitted (but not
     * yet scheduled) task with a lower priority.
     * @param priority taks's priority
     * @param task to be executed
     * @param result to be returned by the future's get() method when the task is done
     * @return Future for task
     */
    <T> Future<T> submit(int priority, Runnable task, T result);

    /**
     * Submit task for execution so that it will be executed before any other sumitted (but not
     * yet scheduled) task with a lower priority.
     * @param priority taks's priority
     * @param task to be executed
     * @return Future for task
     */
    Future<?> submit(int priority, Runnable task);
}
