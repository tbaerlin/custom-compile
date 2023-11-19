/*
 * Created at 17:42 on 20.06.18
 */
package de.marketmaker.istar.common.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author zzhao
 */
public final class LambdaUtil {

  private LambdaUtil() {
    throw new AssertionError("not for instantiation or inheritance");
  }

  public static <T> Predicate<T> alwaysTrue() {
    return x -> true;
  }

  public static <T> Supplier<T> wrap(ThrowingSupplier<T, Exception> throwingSupplier) {
    return () -> {
      try {
        return throwingSupplier.get();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    };
  }

  public static <T> Consumer<T> wrap(ThrowingConsumer<T, Exception> throwingConsumer) {
    return x -> {
      try {
        throwingConsumer.accept(x);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    };
  }

  public static <T, R> Function<T, R> wrap(ThrowingFunction<T, R, Exception> throwingFunc) {
    return x -> {
      try {
        return throwingFunc.apply(x);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    };
  }
}
