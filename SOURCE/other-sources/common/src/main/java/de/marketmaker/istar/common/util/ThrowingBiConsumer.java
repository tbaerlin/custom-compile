/*
 * Created at 17:44 on 20.06.18
 */
package de.marketmaker.istar.common.util;

/**
 * @author zzhao
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, S, E extends Exception> {

  void accept(T t, S s) throws E;
}
