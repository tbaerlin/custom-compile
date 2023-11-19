package de.marketmaker.istar.merger.web.easytrade.misc;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.springframework.beans.factory.DisposableBean;

/**
 * Copy of {@link io.micrometer.core.instrument.binder.tomcat.TomcatMetrics} with customizations for
 * standalone tomcat without access to tomcat manager instance.
 */
@NonNullApi
@NonNullFields
public class TomcatMetricsJMX implements MeterBinder, AutoCloseable, DisposableBean {

  private static final String JMX_DOMAIN_EMBEDDED = "Tomcat";
  private static final String JMX_DOMAIN_STANDALONE = "Catalina";
  private static final String OBJECT_NAME_SERVER_SUFFIX = ":type=Server";
  private static final String OBJECT_NAME_SERVER_EMBEDDED =
      JMX_DOMAIN_EMBEDDED + OBJECT_NAME_SERVER_SUFFIX;
  private static final String OBJECT_NAME_SERVER_STANDALONE =
      JMX_DOMAIN_STANDALONE + OBJECT_NAME_SERVER_SUFFIX;

  private final MBeanServer mBeanServer;
  private final Iterable<Tag> tags;
  private final Set<NotificationListener> notificationListeners = ConcurrentHashMap.newKeySet();

  private volatile String jmxDomain;

  public TomcatMetricsJMX() {
    this(Tags.empty());
  }

  public TomcatMetricsJMX(Iterable<Tag> tags) {
    this.tags = tags;
    this.mBeanServer = getMBeanServer();
    this.jmxDomain = getJmxDomain();
  }

  public static MBeanServer getMBeanServer() {
    List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
    if (!mBeanServers.isEmpty()) {
      return mBeanServers.get(0);
    }
    return ManagementFactory.getPlatformMBeanServer();
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    registerGlobalRequestMetrics(registry);
    registerServletMetrics(registry);
    registerCacheMetrics(registry);
    registerThreadPoolMetrics(registry);
    registerSessionMetrics(registry);
  }

  private void registerSessionMetrics(MeterRegistry registry) {
    registerMetricsEventually(":type=Manager,context=*,*", (name, allTags) -> {
      Gauge.builder("tomcat.sessions.active.max", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "maxActive")))
          .tags(allTags)
          .baseUnit(BaseUnits.SESSIONS)
          .register(registry);

      Gauge.builder("tomcat.sessions.active.current", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "activeSessions")))
          .tags(allTags)
          .baseUnit(BaseUnits.SESSIONS)
          .register(registry);

      FunctionCounter.builder("tomcat.sessions.expired", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "expiredSessions")))
          .tags(allTags)
          .baseUnit(BaseUnits.SESSIONS)
          .register(registry);

      FunctionCounter.builder("tomcat.sessions.rejected", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "rejectedSessions")))
          .tags(allTags)
          .baseUnit(BaseUnits.SESSIONS)
          .register(registry);

      TimeGauge.builder("tomcat.sessions.alive.max", mBeanServer, TimeUnit.SECONDS,
          s -> safeDouble(() -> s.getAttribute(name, "sessionMaxAliveTime")))
          .tags(allTags)
          .register(registry);
    });
  }

  private void registerThreadPoolMetrics(MeterRegistry registry) {
    registerMetricsEventually(":type=ThreadPool,name=*", (name, allTags) -> {
      Gauge.builder("tomcat.threads.config.max", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "maxThreads")))
          .tags(allTags)
          .baseUnit(BaseUnits.THREADS)
          .register(registry);

      Gauge.builder("tomcat.threads.busy", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "currentThreadsBusy")))
          .tags(allTags)
          .baseUnit(BaseUnits.THREADS)
          .register(registry);

      Gauge.builder("tomcat.threads.current", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "currentThreadCount")))
          .tags(allTags)
          .baseUnit(BaseUnits.THREADS)
          .register(registry);

      Gauge.builder("tomcat.connections.current", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "connectionCount")))
          .tags(allTags)
          .baseUnit(BaseUnits.CONNECTIONS)
          .register(registry);

      Gauge.builder("tomcat.connections.keepalive.current", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "keepAliveCount")))
          .tags(allTags)
          .baseUnit(BaseUnits.CONNECTIONS)
          .register(registry);

      Gauge.builder("tomcat.connections.config.max", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "maxConnections")))
          .tags(allTags)
          .baseUnit(BaseUnits.CONNECTIONS)
          .register(registry);
    });
  }

  private void registerCacheMetrics(MeterRegistry registry) {
    registerMetricsEventually(":type=StringCache", (name, allTags) -> {
      FunctionCounter.builder("tomcat.cache.access", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "accessCount")))
          .tags(allTags)
          .register(registry);

      FunctionCounter.builder("tomcat.cache.hit", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "hitCount")))
          .tags(allTags)
          .register(registry);
    });
  }

  private void registerServletMetrics(MeterRegistry registry) {
    registerMetricsEventually(":j2eeType=Servlet,name=*,*", (name, allTags) -> {
      FunctionCounter.builder("tomcat.servlet.error", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "errorCount")))
          .tags(allTags)
          .register(registry);

      FunctionTimer.builder("tomcat.servlet.request", mBeanServer,
          s -> safeLong(() -> s.getAttribute(name, "requestCount")),
          s -> safeDouble(() -> s.getAttribute(name, "processingTime")), TimeUnit.MILLISECONDS)
          .tags(allTags)
          .register(registry);

      TimeGauge.builder("tomcat.servlet.request.max", mBeanServer, TimeUnit.MILLISECONDS,
          s -> safeDouble(() -> s.getAttribute(name, "maxTime")))
          .tags(allTags)
          .register(registry);
    });
  }

  private void registerGlobalRequestMetrics(MeterRegistry registry) {
    registerMetricsEventually(":type=GlobalRequestProcessor,name=*", (name, allTags) -> {
      FunctionCounter.builder("tomcat.global.sent", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "bytesSent")))
          .tags(allTags)
          .baseUnit(BaseUnits.BYTES)
          .register(registry);

      FunctionCounter.builder("tomcat.global.received", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "bytesReceived")))
          .tags(allTags)
          .baseUnit(BaseUnits.BYTES)
          .register(registry);

      FunctionCounter.builder("tomcat.global.error", mBeanServer,
          s -> safeDouble(() -> s.getAttribute(name, "errorCount")))
          .tags(allTags)
          .register(registry);

      FunctionTimer.builder("tomcat.global.request", mBeanServer,
          s -> safeLong(() -> s.getAttribute(name, "requestCount")),
          s -> safeDouble(() -> s.getAttribute(name, "processingTime")), TimeUnit.MILLISECONDS)
          .tags(allTags)
          .register(registry);

      TimeGauge.builder("tomcat.global.request.max", mBeanServer, TimeUnit.MILLISECONDS,
          s -> safeDouble(() -> s.getAttribute(name, "maxTime")))
          .tags(allTags)
          .register(registry);
    });
  }

  /**
   * If the Tomcat MBeans already exist, register metrics immediately. Otherwise register an MBean
   * registration listener with the MBeanServer and register metrics when/if the MBeans becomes
   * available.
   */
  private void registerMetricsEventually(String namePatternSuffix,
      BiConsumer<ObjectName, Iterable<Tag>> perObject) {
    if (getJmxDomain() != null) {
      Set<ObjectName> objectNames =
          this.mBeanServer.queryNames(getNamePattern(namePatternSuffix), null);
      if (!objectNames.isEmpty()) {
        // MBeans are present, so we can register metrics now.
        objectNames.forEach(
            objectName -> perObject.accept(objectName, Tags.concat(tags, nameTag(objectName))));
        return;
      }
    }

    // MBean isn't yet registered, so we'll set up a notification to wait for them to be present and register
    // metrics later.
    NotificationListener notificationListener = new NotificationListener() {
      @Override
      public void handleNotification(Notification notification, Object handback) {
        MBeanServerNotification mBeanServerNotification = (MBeanServerNotification) notification;
        ObjectName objectName = mBeanServerNotification.getMBeanName();
        perObject.accept(objectName, Tags.concat(tags, nameTag(objectName)));
        if (getNamePattern(namePatternSuffix).isPattern()) {
          // patterns can match multiple MBeans so don't remove listener
          return;
        }
        try {
          mBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this);
          notificationListeners.remove(this);
        } catch (InstanceNotFoundException | ListenerNotFoundException ex) {
          throw new RuntimeException(ex);
        }
      }
    };
    notificationListeners.add(notificationListener);

    NotificationFilter notificationFilter = (NotificationFilter) notification -> {
      if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
        return false;
      }

      // we can safely downcast now
      ObjectName objectName = ((MBeanServerNotification) notification).getMBeanName();
      return getNamePattern(namePatternSuffix).apply(objectName);
    };

    try {
      mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener,
          notificationFilter, null);
    } catch (InstanceNotFoundException e) {
      // should never happen
      throw new RuntimeException("Error registering MBean listener", e);
    }
  }

  private ObjectName getNamePattern(String namePatternSuffix) {
    try {
      return new ObjectName(getJmxDomain() + namePatternSuffix);
    } catch (MalformedObjectNameException e) {
      // should never happen
      throw new RuntimeException("Error registering Tomcat JMX based metrics", e);
    }
  }

  private String getJmxDomain() {
    if (this.jmxDomain == null) {
      if (hasObjectName(OBJECT_NAME_SERVER_EMBEDDED)) {
        this.jmxDomain = JMX_DOMAIN_EMBEDDED;
      } else if (hasObjectName(OBJECT_NAME_SERVER_STANDALONE)) {
        this.jmxDomain = JMX_DOMAIN_STANDALONE;
      }
    }
    return this.jmxDomain;
  }

  /**
   * Set JMX domain. If unset, default values will be used as follows:
   *
   * <ul>
   *     <li>Embedded Tomcat: "Tomcat"</li>
   *     <li>Standalone Tomcat: "Catalina"</li>
   * </ul>
   *
   * @param jmxDomain JMX domain to be used
   * @since 1.0.11
   */
  public void setJmxDomain(String jmxDomain) {
    this.jmxDomain = jmxDomain;
  }

  private boolean hasObjectName(String name) {
    try {
      return this.mBeanServer.queryNames(new ObjectName(name), null).size() == 1;
    } catch (MalformedObjectNameException ex) {
      throw new RuntimeException(ex);
    }
  }

  private double safeDouble(Callable<Object> callable) {
    try {
      return Double.parseDouble(callable.call().toString());
    } catch (Exception e) {
      return Double.NaN;
    }
  }

  private long safeLong(Callable<Object> callable) {
    try {
      return Long.parseLong(callable.call().toString());
    } catch (Exception e) {
      return 0;
    }
  }

  private Iterable<Tag> nameTag(ObjectName name) {
    return Tags.empty()
        .and(fromKeyProperty(name, "name")) // tag name for thread pool etc.
        .and(fromKeyProperty(name, "context")) // tag context for Manager
        ;
  }

  private Tags fromKeyProperty(ObjectName objectName, String propName) {
    final String nameTagValue = objectName.getKeyProperty(propName);
    if (nameTagValue != null) {
      return Tags.of(propName, nameTagValue.replaceAll("\"", ""));
    }

    return Tags.empty();
  }

  @Override
  public void close() {
    for (NotificationListener notificationListener : this.notificationListeners) {
      try {
        this.mBeanServer
            .removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener);
      } catch (InstanceNotFoundException | ListenerNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  @Override
  public void destroy() throws Exception {
    close();
  }
}

