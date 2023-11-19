package de.marketmaker.istar.common.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.util.function.Supplier;

/**
 * @author zzhao
 */
public final class MeterSupport {

  public static void record(MeterRegistry reg, String name, Runnable r, Iterable<Tag> tags) {
    if (reg == null) {
      r.run();
    } else {
      reg.timer(name, tags).record(r);
    }
  }

  public static void record(MeterRegistry reg, String name, Runnable r) {
    record(reg, name, r, Tags.empty());
  }

  public static <T> T record(MeterRegistry reg, String name, Supplier<T> supplier,
      Iterable<Tag> tags) {
    return reg == null
        ? supplier.get()
        : reg.timer(name, tags).record(supplier);
  }

  public static <T> T record(MeterRegistry reg, String name, Supplier<T> supplier) {
    return record(reg, name, supplier, Tags.empty());
  }

  public static void stopSample(Sample sample, MeterRegistry reg, String name, Iterable<Tag> tags) {
    if (reg == null) {
      return;
    }
    sample.stop(Timer.builder(name).tags(tags).register(reg));
  }
}