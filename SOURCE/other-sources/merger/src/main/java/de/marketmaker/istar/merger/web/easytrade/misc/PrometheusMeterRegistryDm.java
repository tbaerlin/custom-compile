package de.marketmaker.istar.merger.web.easytrade.misc;

import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.InitializingBean;

/**
 * Not like springboot, have to customize meter registry on our own. This has to be done before
 * registry is used, otherwise common tags and filter won't be applied.
 *
 * @author zzhao
 */
public class PrometheusMeterRegistryDm extends PrometheusMeterRegistry implements InitializingBean {

  private MeterFilter meterFilter;

  public PrometheusMeterRegistryDm() {
    super(PrometheusConfig.DEFAULT);
  }

  public void setMeterFilter(MeterFilter meterFilter) {
    this.meterFilter = meterFilter;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // set common tags
    config().commonTags(MetricsUtil.COMMON_TAGS);
    if (this.meterFilter != null) {
      config().meterFilter(this.meterFilter);
    }
  }
}
