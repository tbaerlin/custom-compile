/*
 * PriorityExecutorServiceImpl.java
 *
 * Created on 10.08.2007 12:58:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Executor service that allows to submit prioritized tasks.
 * It supports graceful and immediate shutdown modes. Code
 * executed in submitted tasks should respect interruption in
 * order for immediate shutdown to function correctly.
 *
 * @author Oliver Flege
 */
public class PriorityExecutorServiceImpl implements PriorityExecutorService,
        InitializingBean, DisposableBean, BeanNameAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService es;

    private String name;

    private int corePoolSize = 1;

    private int maxPoolSize = 1;

    private long keepAliveTimeSeconds = 0;

    private AtomicInteger threadNum = new AtomicInteger();

    private volatile boolean waitForSubmittedTasks = true;

    public void setBeanName(String s) {
        if (this.name == null) {
            this.name = s;
        }
    }

    /**
     * Set name for the threads in the executor service, default is the name of this bean
     * @param name thread name
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setWaitForSubmittedTasks(boolean waitForSubmittedTasks) {
        this.waitForSubmittedTasks = waitForSubmittedTasks;
    }

    /**
     * Set core thread pool size, default is 1
     * @param corePoolSize size
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * If more than corePoolSize threads have been created, those threads will be destroyed
     * after they have been idle for this many seconds
     * @param keepAliveTimeSeconds keep alive time
     */
    public void setKeepAliveTimeSeconds(long keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    /**
     * Max number of threads in the thread pool, default is 1
     * @param maxPoolSize max number of threads
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void destroy() throws Exception {
        if (this.waitForSubmittedTasks) {
            gracefulShutdown();
        }
        else {
            abruptShutdown();
        }
    }

    private void gracefulShutdown() throws Exception {
        this.logger.info("<gracefulShutdown> Performing graceful shutdown, waiting 300s for submitted tasks to complete");
        this.es.shutdown();
        if (!this.es.awaitTermination(300, TimeUnit.SECONDS)) {
            this.logger.error("<gracefulShutdown> failed to terminate within 300s, trying to force shutdown");
            this.es.shutdownNow();
            if (!es.awaitTermination(60, TimeUnit.SECONDS)) {
                this.logger.error("<gracefulShutdown> failed to shutdown executor");
            }
        }
    }

    private void abruptShutdown() throws Exception {
        this.logger.info("<abruptShutdown> Performing immediate shutdown, all submitted tasks will be stopped");
        this.es.shutdownNow();
        if (!this.es.awaitTermination(1, TimeUnit.SECONDS)) {
            this.logger.error("<abruptShutdown> After waiting 1s, some tasks are still running");
        }
    }

    @Override
    public void afterPropertiesSet() {
        this.es = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, this.keepAliveTimeSeconds,
                TimeUnit.SECONDS, new PriorityBlockingQueue<>(), r -> new Thread(r,
            this.name + "-" + this.threadNum.incrementAndGet()));
    }

    public <T> Future<T> submit(int priority, Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final RunnableFuture<T> ftask = newTaskFor(priority, task);
        this.es.execute(ftask);
        return ftask;
    }

    public <T> Future<T> submit(int priority, Runnable task, T result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final RunnableFuture<T> ftask = newTaskFor(priority, task, result);
        this.es.execute(ftask);
        return ftask;
    }

    public Future<?> submit(int priority, Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final RunnableFuture<Object> ftask = newTaskFor(priority, task, null);
        this.es.execute(ftask);
        return ftask;
    }


    private <T> RunnableFuture<T> newTaskFor(int priority, Callable<T> callable) {
        return new ComparableTask<>(priority, callable);
    }

    private <T> RunnableFuture<T> newTaskFor(int priority, Runnable runnable, T value) {
        return new ComparableTask<>(priority, runnable, value);
    }

    private static class ComparableTask<T> extends FutureTask<T> implements
            Comparable<ComparableTask> {
        private final int priority;

        public ComparableTask(int priority, Callable<T> callable) {
            super(callable);
            this.priority = priority;
        }

        public ComparableTask(int priority, Runnable runnable, T result) {
            super(runnable, result);
            this.priority = priority;
        }

        public int compareTo(ComparableTask o) {
            return o.priority - this.priority;
        }


        public String toString() {
            return new StringBuilder(200).append("ComparableTask[priority=").append(this.priority)
                    .append(super.toString()).append("]").toString();
        }
    }


    public static void main(String[] args) throws Exception {
        PriorityExecutorServiceImpl es = new PriorityExecutorServiceImpl();
        es.setName("foo");
        es.afterPropertiesSet();

        Random r = new Random();
        List<Future> fs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int prio = r.nextInt(30);
            final Future<?> f = es.submit(prio, new Runnable() {
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ": " + prio);
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            fs.add(f);
        }

        for (Future f : fs) {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        es.destroy();
    }
}
