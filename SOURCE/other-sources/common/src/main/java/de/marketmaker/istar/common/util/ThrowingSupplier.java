/*
 * Created at 17:44 on 20.06.18
 */
package de.marketmaker.istar.common.util;

/**
 * @author zzhao
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {

  T get() throws E;
}
