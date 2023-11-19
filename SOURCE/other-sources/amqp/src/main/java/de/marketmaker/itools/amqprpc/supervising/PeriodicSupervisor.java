/*
 * PeriodicSupervisor.java
 *
 * Created on 04.03.2011 15:26:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.supervising;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Checks a set of {@link SupervisableAndRepairable} objects periodically.
 * <p/>
 * This class will check every {@code checkInterval} seconds, whether
 * {@link SupervisableAndRepairable#everythingOk()}, and if not, call
 * {@link SupervisableAndRepairable#tryToRecover()}.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class PeriodicSupervisor implements Supervisor, Runnable, ApplicationListener<ApplicationEvent>,
        InitializingBean, DisposableBean {

    private static AtomicInteger THREAD_ID = new AtomicInteger();

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * the "normal" check interval without any pending problem.
     * If a problem is detected, a service check will be scheduled after 1s and that interval
     * doubles as long as the problem is not solved and it is less than this value. If the
     * problem persists, this interval will finally be used again.
     */
    private int checkInterval = DateTimeConstants.SECONDS_PER_MINUTE;

    private volatile int checkIntervalIncrement;

    /**
     * the millisecond after which the next check is to be executed.
     */
    private AtomicLong nextCheckAt = new AtomicLong();

    private AtomicBoolean closed = new AtomicBoolean(false);

    private AtomicBoolean allChecksPassed = new AtomicBoolean(true);

    private volatile boolean checkInProgress = false;

    private boolean usingDaemonThread = false;

    private CopyOnWriteArrayList<SupervisableAndRepairable> supervisedObjects
            = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1, r -> {
                Thread result = new Thread(r);
                result.setDaemon(usingDaemonThread);
                result.setName("PeriodicSupervisor-" + THREAD_ID.incrementAndGet());
                return result;
            });

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextClosedEvent) {
            if (this.closed.compareAndSet(false, true)) {
                logEvent(event);
                this.scheduler.shutdown();
            }
        } else if (event instanceof AmqpProblemEvent) {
            if (!this.checkInProgress) {
                logEvent(event);
                onProblem(false);
            }
        }
    }

    private void logEvent(ApplicationEvent event) {
        this.logger.info("received =======" + event.getClass().getName() + "=======");
    }

    private void onProblem(boolean doubleInterval) {
        if (this.allChecksPassed.compareAndSet(true, false)) {
            this.checkIntervalIncrement = 1;
            this.logger.info("<onProblem> checkIntervalIncrement = 1");
            setNextCheckAt();
        } else if (doubleInterval) {
            this.checkIntervalIncrement
                    = Math.min(this.checkInterval, this.checkIntervalIncrement * 2);
            this.logger.info("<onProblem> checkIntervalIncrement = " + this.checkIntervalIncrement);
            setNextCheckAt();
        }
    }

    private void setNextCheckAt() {
        this.nextCheckAt.set(System.currentTimeMillis()
                + this.checkIntervalIncrement * DateTimeConstants.MILLIS_PER_SECOND);
    }

    private void onOk() {
        this.allChecksPassed.set(true);
        this.checkIntervalIncrement = this.checkInterval;
        this.logger.debug("<onOk> checkIntervalIncrement = " + this.checkIntervalIncrement);
        setNextCheckAt();
    }

    /**
     * Set time in seconds between status checks.
     *
     * @param checkInterval time in seconds
     */
    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    public void setUsingDaemonThread(boolean usingDaemonThread) {
        this.usingDaemonThread = usingDaemonThread;
    }

    public void addSupervisedObject(SupervisableAndRepairable supervisedObject) {
        if (this.supervisedObjects.addIfAbsent(supervisedObject)) {
            this.logger.info("<addSupervisedObject> added " + supervisedObject);
        }
    }

    public void removeSupervisedObject(SupervisableAndRepairable supervisedObject) {
        if (this.supervisedObjects.remove(supervisedObject)) {
            this.logger.info("<removeSupervisedObject> removed " + supervisedObject);
        }
    }

    public void afterPropertiesSet() {
        this.checkIntervalIncrement = this.checkInterval;
        this.scheduler.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
    }

    public void run() {
        if (System.currentTimeMillis() < this.nextCheckAt.get()) {
            return;
        }

        try {
            if (checkAll()) {
                onOk();
            } else {
                onProblem(true);
            }
        } catch (Throwable e) {
            logger.error("<run> failed", e);
            onProblem(true);
        }
    }

    private boolean checkAll() {
        this.checkInProgress = true;
        try {
            boolean result = true;
            for (SupervisableAndRepairable sar : this.supervisedObjects) {
                result &= check(sar);
            }
            return result;
        } finally {
            this.checkInProgress = false;
        }
    }

    private boolean check(SupervisableAndRepairable supervisedObject) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<check> checking " + supervisedObject);
        }

        if (supervisedObject.everythingOk()) {
            return true;
        }

        this.logger.warn("<check> supervised object not OK: [" +
                supervisedObject.logMessageInCaseOfError() +
                "], Trying recover");
        try {
            supervisedObject.tryToRecover();
            this.logger.info("<check> successfully recovered");
            return true;
        } catch (Throwable e) {
            this.logger.error("<check> failed for " + supervisedObject, e);
            return false;
        }
    }

    public void destroy() throws Exception {
        if (this.scheduler.isTerminated()) {
            return;
        }
        this.scheduler.shutdown();
        if (!this.scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
            this.logger.error("<destroy> scheduler did not terminate");
            this.logger.info("<destroy> Trying hard shutdown.");
            this.scheduler.shutdownNow();
        }
    }
}
