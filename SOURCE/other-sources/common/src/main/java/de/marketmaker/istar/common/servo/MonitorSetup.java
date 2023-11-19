/*
 * MonitorSetup.java
 *
 * Created on 28.08.15 21:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.servo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceLevel;
import com.netflix.servo.monitor.CompositeMonitor;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.Pollers;
import com.netflix.servo.publish.AsyncMetricObserver;
import com.netflix.servo.publish.BaseMetricObserver;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.JvmMetricPoller;
import com.netflix.servo.publish.MetricFilter;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.RegexMetricFilter;
import com.netflix.servo.tag.Tags;
import com.netflix.servo.util.ExpiringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.Constants;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Sets up monitoring of servo monitors. An instance of this class can be obtained via
 * {@link ServoMonitoring#getSetup()} and is expected to be added to a spring application context.
 * When it receives a {@link ContextRefreshedEvent}, the beans in that context are introspected
 * for monitors and, if any are found, these are registered in the {@link DefaultMonitorRegistry#getInstance()}.
 * <p>
 * Metric polling is configured when this component is {@link #start()}ed. Polling occurs according
 * to the intervals defined per {@link Pollers#getPollingIntervals()}, the default being every
 * 60s for all non-critical metrics and every 10s for all critical metrics. A Monitor is considered
 * as a supplier of critical metrics if its configuration contains the {@link DataSourceLevel#CRITICAL} tag.
 */
public final class MonitorSetup implements SmartLifecycle, ApplicationListener<ContextRefreshedEvent> {

    private static MonitorSetup createInstance() {
        Tags.intern(DataSourceLevel.KEY); // throws NoClassDefFoundError if guava is not on classpath
        return new MonitorSetup(InfluxdbMetricObserver.create()
                , "true".equals(Constants.getProperty("logging.observer", "false"))
                , "true".equals(Constants.getProperty("monitor.jvm", "false")));
    }

    static final MonitorSetup INSTANCE = createInstance();

    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.hasObserver();
    }

    public static long getPollInterval(int i) {
        return Pollers.getPollingIntervals().get(i);
    }

    private static MetricObserver rateTransform(MetricObserver observer, int i) {
        final long heartbeat = 2 * getPollInterval(i);
        return new CounterToRateMetricTransform(observer, heartbeat, MILLISECONDS);
    }

    private static MetricObserver async(String name, MetricObserver observer, int i) {
        final long expireTime = 2 * getPollInterval(i);
        final int queueSize = 10;
        return new AsyncMetricObserver(name, observer, queueSize, expireTime);
    }

    private static void schedule(MetricPoller poller, List<MetricObserver> observers, int i) {
        final PollRunnable task = new PollRunnable(poller, BasicMetricFilter.MATCH_ALL,
                true, observers);
        PollScheduler.getInstance().addPoller(task, getPollInterval(i), MILLISECONDS);
    }

    private static MetricObserver createLoggingObserver(int i) {
        BaseMetricObserver observer = new BaseMetricObserver("logging-" + i) {
            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void updateImpl(List<Metric> metrics) {
                for (Metric m : metrics) {
                    logger.info("{}", m);
                }
            }
        };
        return rateTransform(observer, i);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MetricFilter criticalFilter = new RegexMetricFilter(DataSourceLevel.KEY,
            Pattern.compile(DataSourceLevel.CRITICAL.name()), false, false);

    private final MetricFilter notCriticalFilter = cfg -> !this.criticalFilter.matches(cfg);

    private final AtomicBoolean running = new AtomicBoolean();

    private final InfluxdbMetricObserver influxdbObserver;

    private final boolean addLoggingObserver;

    private final boolean addJvmMetricPoller;

    private MonitorSetup(InfluxdbMetricObserver imo, boolean addLoggingObserver,
            boolean addJvmMetricPoller) {
        this.influxdbObserver = imo;
        if (this.influxdbObserver != null) {
            final CompositeMonitor<?> cm
                    = Monitors.newObjectMonitor("influxdbObserver", this.influxdbObserver);
            DefaultMonitorRegistry.getInstance().register(cm);
        }
        this.addLoggingObserver = addLoggingObserver;
        this.addJvmMetricPoller = addJvmMetricPoller;
    }

    private boolean hasObserver() {
        return this.influxdbObserver != null || this.addLoggingObserver;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerMonitors((ApplicationContext) event.getSource());
    }

    protected void registerMonitors(ApplicationContext ctx) {
        final TreeSet<String> beansWithMonitors = new TreeSet<>();
        final Map<String, Object> beans = ctx.getBeansOfType(Object.class, false, false);
        for (Map.Entry<String, Object> e : beans.entrySet()) {
            String name = e.getKey();
            Object bean = e.getValue();
            if (bean.getClass().getPackage() == null
                    || !StringUtils.hasText(bean.getClass().getSimpleName())) {
                continue;
            }
            final String pkg = bean.getClass().getPackage().getName();
            if (!pkg.startsWith("de.marketmaker") && !pkg.startsWith("com.vwd")) {
                continue;
            }
            CompositeMonitor<?> cm = Monitors.newObjectMonitor(name, bean);
            if (!cm.getMonitors().isEmpty()) {
                DefaultMonitorRegistry.getInstance().register(cm);
                beansWithMonitors.add(name);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<registerMonitors> for " + name);
                }
            }
        }
        if (!beansWithMonitors.isEmpty()) {
            this.logger.info("<registerMonitors> for " + beansWithMonitors);
        }
    }


    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }


    @Override
    public void start() {
        if (!isActive()) {
            return;
        }
        if (!this.running.compareAndSet(false, true)) {
            this.logger.info("<start> again?!");
            return;
        }

        PollScheduler.getInstance().start();

        final List<MetricObserver> observers = addPoller(0);
        if (this.addJvmMetricPoller) {
            schedule(new JvmMetricPoller(), observers, 0);
        }
        addPoller(1);
        this.logger.info("<start> done");
    }

    protected List<MetricObserver> addPoller(int i) {
        final List<MetricObserver> observers = createObservers(i);
        MetricFilter f = (i == 1) ? criticalFilter : notCriticalFilter;
        final MonitorRegistryMetricPoller poller
                = new MonitorRegistryMetricPoller(DefaultMonitorRegistry.getInstance(), 0L, MILLISECONDS, false);
        final PollRunnable r = new PollRunnable(poller, f, observers);
        PollScheduler.getInstance().addPoller(r, getPollInterval(i), MILLISECONDS);
        return observers;
    }

    protected List<MetricObserver> createObservers(int i) {
        final List<MetricObserver> observers = new ArrayList<>();
        if (this.influxdbObserver != null) {
            final MetricObserver async = async("influxdb-" + i, this.influxdbObserver, i);
            observers.add(rateTransform(async, i));
        }
        if (this.addLoggingObserver) {
            observers.add(createLoggingObserver(i));
        }
        return observers;
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(true, false)) {
            return;
        }
        PollScheduler.getInstance().stop();

        try {
            final Field f = ExpiringCache.class.getDeclaredField("SERVICE");
            f.setAccessible(true);
            ((ExecutorService) f.get(null)).shutdownNow();
        } catch (Exception e) {
            this.logger.warn("<stop> failed for ExpiringCache.SERVICE", e);
        }

        this.logger.info("<stop> done");
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }
}

