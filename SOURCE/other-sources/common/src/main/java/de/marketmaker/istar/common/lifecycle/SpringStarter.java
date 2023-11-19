/*
 * Starter.java
 *
 * Created on 25.10.2004 14:16:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lifecycle;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @deprecated
 */
public class SpringStarter extends ApplicationObjectSupport implements InitializingBean,
        ApplicationListener {
    private List<Startable> startables = null;

    private final List<Startable> started = new ArrayList<>();

    private Set<String> excluded = Collections.emptySet();

    private boolean startDone = false;

    private AtomicBoolean handlingEvent = new AtomicBoolean(false);

    public SpringStarter() {
    }

    public void setExcluded(String[] excluded) {
        this.excluded = new HashSet<>(Arrays.asList(excluded));
        this.logger.info("<setExcluded> " + this.excluded);
    }

    public void setStartables(List<Startable> startables) {
        this.startables = startables;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.startables == null) {
            this.startables = detectStartables();
        }
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent.getSource() != getApplicationContext()) {
            return;
        }
        if (!this.handlingEvent.compareAndSet(false, true)) {
            this.logger.warn("<onApplicationEvent> already handling another event");
            return;
        }
        try {
            handleEvent(applicationEvent);
        } finally {
            this.handlingEvent.set(false);
        }
    }

    private synchronized void handleEvent(ApplicationEvent applicationEvent) {
        if (!this.startDone && applicationEvent instanceof ContextRefreshedEvent) {
            this.startDone = true;
            doStart();
        }
        else if (this.startDone && applicationEvent instanceof ContextClosedEvent) {
            doStop();
            this.startDone = false;
        }
        else {
            this.logger.info("<handleEvent> ignoring " + applicationEvent);
        }
    }

    private void doStop() {
        this.logger.info("<doStop> ...");
        for (Startable startable : this.started) {
            try {
                startable.stop();
            } catch (Exception e) {
                this.logger.error("<doStop> failed", e);
                // continue to allow other components to shutdown
            }
        }
        this.logger.info("<doStop> finished");
    }

    private void doStart() {
        this.logger.info("<doStart> ...");
        for (Startable startable : this.startables) {
            try {
                startable.start();
                this.started.add(0, startable);
            } catch (Throwable t) {
                this.logger.error("<doStart> failed", t);
                final ApplicationContext ac = getApplicationContext();
                if (ac instanceof ConfigurableApplicationContext) {
                    ((ConfigurableApplicationContext) ac).close();
                }
                return;
            }
        }
        this.logger.info("<doStart> finished");
    }


    private List<Startable> detectStartables() {
        final List<Startable> result = new ArrayList<>();

        final Map<String, Startable> beansByName
                = getApplicationContext().getBeansOfType(Startable.class, false, false);

        for (String s : this.excluded) {
            beansByName.remove(s);
        }

        result.addAll(beansByName.values());
        result.sort(new OrderComparator());

        this.logger.info("<detectStartables> found " + beansByName.keySet());
        return result;
    }
}
