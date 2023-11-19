package de.marketmaker.istar.merger.web.easytrade.misc;

import io.micrometer.core.instrument.Tags;

/**
 * @author zzhao
 */
public final class MetricsUtil {

  public static final Tags COMMON_TAGS = Tags.of(
      "app", "dmxml",
      "domain", System.getProperty("domainid", "unknown"),
      "machine", System.getProperty("machineid", "unknown")
  );

  private MetricsUtil() {
    throw new AssertionError("not for instantiation or inheritance");
  }
}
