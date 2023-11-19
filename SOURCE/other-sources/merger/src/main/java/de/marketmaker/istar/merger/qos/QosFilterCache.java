/*
 * QosFilterCache.java
 *
 * Created on 27.11.2009 12:50:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.qos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oflege
 */
@ManagedResource
public class QosFilterCache implements InitializingBean, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private QosDao qosDao = null;

    private ExecutorService es;

    private final AtomicInteger numRejected = new AtomicInteger();

    private final AtomicInteger numFailed = new AtomicInteger();

    private final AtomicInteger numStored = new AtomicInteger();

    private final AtomicInteger numRetrieved = new AtomicInteger();

    private volatile boolean enabled = true;

    @ManagedAttribute
    public boolean isEnabled() {
        return enabled;
    }

    @ManagedAttribute
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void afterPropertiesSet() throws Exception {
        this.es = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue(1000),
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "QosFilterCache-Store");
                    }
                }, new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        numRejected.incrementAndGet();
                    }
                });
    }

    public void destroy() throws Exception {
        if (this.es != null) {
            this.es.shutdown();
        }
        this.logger.info("<destroy> "
                + "numStored=" + this.numStored.get()
                + ", numRetrieved=" + this.numRetrieved.get()
                + ", numFailed=" + this.numFailed.get()
                + ", numRejected=" + this.numRejected.get()
        );
    }

    @ManagedAttribute
    public int getNumRejected() {
        return this.numRejected.get();
    }

    @ManagedAttribute
    public int getNumFailed() {
        return this.numFailed.get();
    }

    @ManagedAttribute
    public int getNumStored() {
        return this.numStored.get();
    }

    @ManagedAttribute
    public int getNumRetrieved() {
        return this.numRetrieved.get();
    }

    public void setQosDao(QosDao qosDao) {
        this.qosDao = qosDao;
    }

    protected void store(final String key, final Serializable value) {
        if (!this.enabled) {
            return;
        }

        if (this.es == null) {
            doStore(key, value);
            return;
        }

        this.es.submit(new Runnable() {
            public void run() {
                doStore(key, value);
            }
        });
    }

    private void doStore(String key, Serializable value) {
        try {
            qosDao.store(key, value);
            numStored.incrementAndGet();
        } catch (Exception e) {
            logger.warn("<store> failed", e);
            numFailed.incrementAndGet();
        }
    }

    protected Serializable retrieve(String key) {
        if (!this.enabled) {
            return null;
        }
        
        try {
            final Serializable result = this.qosDao.retrieve(key);
            this.numRetrieved.incrementAndGet();
            return result;
        } catch (Exception e) {
            this.logger.warn("<retrieve> failed", e);
        }
        return null;
    }
}
