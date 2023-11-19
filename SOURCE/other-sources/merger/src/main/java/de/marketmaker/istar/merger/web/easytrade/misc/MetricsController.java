package de.marketmaker.istar.merger.web.easytrade.misc;

import com.rabbitmq.client.ConnectionFactory;
import de.marketmaker.itools.amqprpc.impl.AmqpPostProcessor;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.EhCache2Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author zzhao
 */
public class MetricsController extends AbstractController implements InitializingBean {

  private PrometheusMeterRegistry registry;

  // only ehcache manager see also MmEhCacheManagerFactoryBean
  private CacheManager cacheManager;

  // amqp connection factory is dynamically registered within AmqpPostProcessor
  // bean name is fixed amqpConnectionFactory
  private AmqpPostProcessor amqpPostProcessor;

  private TomcatMetricsJMX tomcatMetrics;

  public void setRegistry(PrometheusMeterRegistry registry) {
    this.registry = registry;
  }

  public void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public void setAmqpPostProcessor(AmqpPostProcessor amqpPostProcessor) {
    this.amqpPostProcessor = amqpPostProcessor;
  }

  public void setTomcatMetrics(TomcatMetricsJMX tomcatMetrics) {
    this.tomcatMetrics = tomcatMetrics;
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp)
      throws Exception {
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType(TextFormat.CONTENT_TYPE_004);

    try (Writer writer = resp.getWriter()) {
      this.registry.scrape(writer);
      writer.flush();
    }

    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // bind JVM related metrics
    new JvmThreadMetrics().bindTo(this.registry);
    new JvmGcMetrics().bindTo(this.registry);
    new JvmMemoryMetrics().bindTo(this.registry);
    new JvmHeapPressureMetrics().bindTo(this.registry);
    new ClassLoaderMetrics().bindTo(this.registry);
    new ProcessorMetrics().bindTo(this.registry);
    new UptimeMetrics().bindTo(this.registry);

    this.tomcatMetrics.bindTo(this.registry);

    // bind ehcache related metrics
    for (String cacheName : this.cacheManager.getCacheNames()) {
      new EhCache2Metrics(this.cacheManager.getCache(cacheName), Tags.empty())
          .bindTo(this.registry);
    }

    // bind rabbit MQ metrics TODO need amqp-client update
    final ConnectionFactory connectionFactory =
        getApplicationContext().getBean("amqpConnectionFactory", ConnectionFactory.class);
  }
}
