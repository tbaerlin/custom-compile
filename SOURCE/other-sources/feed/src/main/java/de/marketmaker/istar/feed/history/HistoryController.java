/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

/**
 * @author zzhao
 */
public abstract class HistoryController
        implements InitializingBean, Lifecycle, Runnable, TickerMBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String tokenStop;

    private BlockingQueue<String> scheduleQueue;

    private AtomicBoolean stopped = new AtomicBoolean(false);

    private Thread worker;

    public HistoryController() {
        this.tokenStop = Long.toHexString(System.currentTimeMillis());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.scheduleQueue = new LinkedBlockingQueue<>();
        this.worker = new Thread(this, "Worker");
    }

    @Override
    public void start() {
        this.worker.setDaemon(false);
        this.worker.start();
        this.logger.info("<start> tick history controller started");
    }

    @Override
    public void stop() {
        this.logger.info("<stop> stopping tick history controller ...");
        this.stopped.set(true);
        this.scheduleQueue.offer(this.tokenStop);
        this.logger.info("<stop> waiting for worker thread to end");
        try {
            this.worker.join();
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
        this.logger.info("<stop> tick history controller stopped");
        if (!this.scheduleQueue.isEmpty()) {
            this.logger.warn("<stop> not executed jobs: " + this.scheduleQueue);
        }
    }

    @Override
    public boolean isRunning() {
        return this.worker != null && this.worker.isAlive();
    }

    @ManagedOperation(description = "invoke to create day tick file ")
    @ManagedOperationParameters(
            @ManagedOperationParameter(
                    name = "path",
                    description = "the folder contains tick data for one day")
    )
    public boolean tick(String path) {
        boolean succ = false;
        if (!this.stopped.get()) {
            succ = this.scheduleQueue.offer(path);
        }

        return succ;
    }

    protected String getWorkingStatus() {
        final StringBuilder sb = new StringBuilder();
        sb.append("stopped: ").append(this.stopped.get());
        final String[] jobs = this.scheduleQueue.toArray(new String[0]);
        if (jobs.length > 0) {
            sb.append("\nremaining jobs:");
            for (String job : jobs) {
                sb.append("\n").append(job);
            }
        }
        else {
            sb.append("\nno remaining jobs");
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            String token;
            do {
                this.logger.info("<run> waiting for invocation");
                token = this.scheduleQueue.take();
                try {
                    if (!this.tokenStop.equals(token)) {
                        final File file = new File(token);
                        if (!file.exists()) {
                            this.logger.warn("<run> no such file found: " + file.getAbsolutePath());
                        }
                        else {
                            tickIntern(file);
                        }
                    }
                } catch (Exception e) {
                    this.logger.error("<run> cannot perform job: " + token, e);
                }
            } while (!this.tokenStop.equals(token) && !this.stopped.get());
        } catch (InterruptedException e) {
            this.logger.error("<run> tick history worker interrupted", e);
            Thread.currentThread().interrupt();
        }
        this.logger.info("<run> tick history worker stopped");
    }

    protected abstract void tickIntern(File file) throws IOException;
}
