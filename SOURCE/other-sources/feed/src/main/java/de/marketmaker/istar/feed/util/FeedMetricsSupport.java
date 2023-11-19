package de.marketmaker.istar.feed.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;

/**
 * @author zzhao
 */
public class FeedMetricsSupport {

  private FeedMetricsSupport() {
    throw new AssertionError("not for instantiation or inheritance");
  }

  public static Sample mayStartSample(MeterRegistry meterRegistry) {
    return meterRegistry != null ? Timer.start() : null;
  }

  public static void mayStopSample(Class<?> subject, MeterRegistry meterRegistry,
      Sample sample, Class<?> reqClass) {
    if (meterRegistry != null && sample != null) {
      sample.stop(meterRegistry.timer("chicago.resp",
          "src", subject.getSimpleName(),
          "req", reqClass.getSimpleName()
      ));
    }
  }
}
