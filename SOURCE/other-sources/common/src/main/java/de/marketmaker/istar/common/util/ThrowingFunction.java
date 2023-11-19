/*
 * Created at 17:44 on 20.06.18
 */
package de.marketmaker.istar.common.util;

/**
 * @author zzhao
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

  R apply(T t) throws E;
}
