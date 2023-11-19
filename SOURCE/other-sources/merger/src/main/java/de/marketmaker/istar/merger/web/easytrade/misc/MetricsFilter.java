package de.marketmaker.istar.merger.web.easytrade.misc;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * Adopted from org.springframework.boot.actuate.autoconfigure.metrics.PropertiesMeterFilter with
 * dynamic configuration via JMX operations.
 * <p>
 * Not working as expected yet. Currently meter filter is only applied when meters are initially
 * registered. See also https://github.com/micrometer-metrics/micrometer/issues/1197. If this filter
 * can work dynamically depends on how that ticket is resolved.
 * </p>
 *
 * @author zzhao
 */
@ManagedResource(description = "filters metrics collection")
public class MetricsFilter implements MeterFilter {

  private static final Pattern P_SEP_SWITCH = Pattern.compile(",");

  // to use copied methods
  private final ConcurrentHashMap<String, Boolean> switchByName = new ConcurrentHashMap<>();

  public MetricsFilter(String disabledMetrics) {
    if (StringUtils.hasText(disabledMetrics)) {
      final String[] switches = P_SEP_SWITCH.split(disabledMetrics);
      if (switches != null && switches.length > 0) {
        Arrays.stream(switches).forEach(s -> this.switchByName.put(s, Boolean.FALSE));
      }
    }
  }

  @ManagedOperation(description = "disabled metrics")
  public String getDisabledMetrics() {
    return String.valueOf(this.switchByName);
  }

  @ManagedOperation(description = "disable metrics by name")
  @ManagedOperationParameters(
      @ManagedOperationParameter(
          name = "metricsName",
          description = "a metrics name")
  )
  public void disableMetrics(String metricsName) {
    this.switchByName.put(metricsName, Boolean.FALSE);
  }

  @ManagedOperation(description = "enable metrics by name")
  @ManagedOperationParameters(
      @ManagedOperationParameter(
          name = "metricsName",
          description = "a metrics name")
  )
  public void enableMetrics(String metricsName) {
    this.switchByName.remove(metricsName);
  }

  @Override
  public MeterFilterReply accept(Meter.Id id) {
    boolean enabled = lookupWithFallbackToAll(this.switchByName, id);
    return enabled ? MeterFilterReply.NEUTRAL : MeterFilterReply.DENY;
  }

  private Boolean lookupWithFallbackToAll(Map<String, Boolean> values, Id id) {
    if (values.isEmpty()) {
      return Boolean.TRUE;
    }
    return doLookup(values, id);
  }

  private Boolean doLookup(Map<String, Boolean> values, Id id) {
    String name = id.getName();
    while (StringUtils.hasLength(name)) {
      Boolean result = values.get(name);
      if (result != null) {
        return result;
      }
      int lastDot = name.lastIndexOf('.');
      name = (lastDot != -1) ? name.substring(0, lastDot) : "";
    }

    return Boolean.TRUE;
  }
}
