/*
 * EodReader.java
 *
 * Created on 15.01.13 11:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.Closeable;

/**
 * @author zzhao
 */
interface EodReader<T extends EodItem> extends Closeable, Iterable<T> {

  @Override
  EodIterator<T> iterator();
}
