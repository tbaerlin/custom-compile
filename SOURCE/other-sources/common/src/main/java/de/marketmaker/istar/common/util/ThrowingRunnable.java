/*
 * Created at 15:45 on 19.07.18
 */
package de.marketmaker.istar.common.util;

/**
 * @author zzhao
 */
public interface ThrowingRunnable<E extends Exception> {

  void run() throws E;
}
