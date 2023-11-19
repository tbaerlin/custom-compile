/*
 * PrototypeManager.java
 *
 * Created on 12.03.2008 17:39:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * A bean that can be used to wrap a protoype (non-singleton) bean (or multiple of those),
 * so that this prototype can be re-instantiated per jmx while the application is running.
 * Useful is the managed instance stopped working and has to be restarted without having to
 * restart the whole application.<p>
 * The problem with this class is that it is very difficult to deal with prototyped objects that cannot
 * be destroyed properly.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class PrototypeManager extends ApplicationObjectSupport
        implements InitializingBean, SmartLifecycle {

    private static final Timer TIMER = new Timer("PrototypeManager-Timer", true);

    private String beanName;

    private class InstanceInfo {
        private final ZonedDateTime created;

        private final Object instance;

        private InstanceInfo(Object instance) {
            this.instance = instance;
            this.created = ZonedDateTime.now();
        }

        public String toString() {
            return PrototypeManager.this.beanName
                + "[created=" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.created) + "]";
        }
    }

    private final List<InstanceInfo> instances = new ArrayList<>();

    private int numInstances = 1;

    private boolean started = false;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Change the number of instances to be created on {@link #reloadInstances()}.
     * @param numInstances number of instances
     */
    @ManagedAttribute
    public synchronized void setNumInstances(int numInstances) {
        this.numInstances = numInstances;
    }

    /**
     * Change the number of instances and reload at once.
     * Saves one operation/click on jconsole.
     * @param numInstances number of instances
     */
    @ManagedAttribute
    public synchronized void setAndReloadInstances(int numInstances) {
        this.setNumInstances(numInstances);
        this.reloadInstances();
    }

    /**
     * Returns number of instances that will be created when {@link #reloadInstances()} is invoked
     * @return number of instances
     */
    @ManagedAttribute
    public synchronized int getNumInstances() {
        return this.numInstances;
    }

    /**
     * Number of currently managed instances. Not necessarily the same as {@link #getNumInstances()},
     * as instance creation might have failed or {@link #addInstance()} might have been called.
     * @return number of live instances
     */
    @ManagedAttribute
    public synchronized int getLiveInstances() {
        return this.instances.size();
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void start() {
        this.started = true;
        if (Boolean.getBoolean("noProtos")) {
            this.logger.warn("<start> -DnoProtos=true was used - coming up w/o instances - use reloadInstances via JMX to start them");
        }
        else {
            createInstances();
        }
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        }
        finally {
            this.started = false;
            callback.run();
        }
    }

    @Override
    public void stop() {
        destroyInstances();
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 10;
    }

    public void afterPropertiesSet() throws Exception {
        if (!getApplicationContext().isPrototype(this.beanName)) {
            throw new IllegalStateException("not a prototype bean: " + this.beanName);
        }
    }

    private synchronized void createInstances() {
        for (int i = 0; i < this.numInstances; i++) {
            addInstance();
        }
        this.logger.info("<createInstances> created " + this.instances.size()
                + " instances of bean prototype '" + this.beanName + "'");
    }

    /**
     * Adds another prototype instance. Does not change the numInstances property and thus the
     * effect of calling {@link #reloadInstances()}.
     */
    @ManagedOperation
    public synchronized void addInstance() {
        final Object instance = createInstance();
        if (instance != null) {
            final InstanceInfo info = new InstanceInfo(instance);
            this.instances.add(info);
            this.logger.info("<addInstance> added " + info);
        }
    }

    private void destroyInstances() {
        final List<InstanceInfo> toDestroy = getInstancesToDestroy();

        this.logger.info("<destroyInstances> " + toDestroy.size() + "...");

        for (InstanceInfo info : toDestroy) {
            destroyInstance(info);
        }

        this.logger.info("<destroyInstances> destroyed " + toDestroy.size()
                + " instances of bean prototype '" + this.beanName + "'");
    }

    private synchronized List<InstanceInfo> getInstancesToDestroy() {
        final List<InstanceInfo> toDestroy = new ArrayList<>(this.instances);
        this.instances.clear();
        return toDestroy;
    }


    private void destroyInstance(InstanceInfo info) {
        final Thread destroyThread = Thread.currentThread();
        final TimerTask task = new TimerTask() {
            public void run() {
                destroyThread.interrupt();
            }
        };
        TIMER.schedule(task, 10000L);
        if (info.instance instanceof DisposableBean) {
            try {
                ((DisposableBean) info.instance).destroy();
                task.cancel();
                this.logger.info("<destroyInstance> destroyed " + info);
            } catch (InterruptedException e) {
                this.logger.error("<destroyInstance> interrupted for " + info, e);
            } catch (Exception e) {
                this.logger.error("<destroyInstance> failed for " + info, e);
            } catch (Throwable t) {
                this.logger.error("<destroyInstance> failed for " + info, t);
            }
        }
    }

    private Object createInstance() {
        try {
            return getApplicationContext().getBean(this.beanName);
        } catch (Exception e) {
            this.logger.error("<createInstance> failed", e);
        } catch (Throwable t) {
            this.logger.error("<createInstance> failed", t);
        }
        return null;
    }

    /**
     * Reload the managed prototype(s), that is: dispose the current instance(s) (if any) and
     * create {@link #getNumInstances()} new instances.
     */
    @ManagedOperation(description = "destroys current instances and creates numInstances new instances")
    public void reloadInstances() {
        destroyInstances();
        createInstances();
    }
}
