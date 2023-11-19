/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts items from a data file with one level B* tree index structure at the end.
 * <p>
 * Proto buf data file can be expanded with one level B* tree index by
 * {@link OneLevelBsTree#addBsTreeLevel1(java.io.File)}
 * </p>
 *
 * @author zzhao
 */
@Slf4j
public class ItemExtractor<T extends Comparable<T>> implements Iterable<Item<T>> {

  private final DataFile dataFile;

  private final long indexOffsetStart;

  private final long indexSize;

  private final Class<T> keyClass;

  private final OffsetLengthCoder olCoder;

  public ItemExtractor(Class<T> clazz, DataFile df) throws IOException {
    this.dataFile = df;
    final long indexStartPos = df.size() - 8 - 1;
    if (indexStartPos > 0) {
      df.seek(indexStartPos);
      final long indexStart = df.readLong();
      this.olCoder = new OffsetLengthCoder(df.readByte());
      df.seek(indexStart);
      this.indexOffsetStart = df.readLong();
      this.indexSize = indexStart - this.indexOffsetStart;
      this.keyClass = clazz;
    } else {
      this.olCoder = null;
      this.indexOffsetStart = 0;
      this.indexSize = 0;
      this.keyClass = null;
    }
  }

  public static long indexOffsetStart(DataFile df) throws IOException {
    final long indexStartPos = df.size() - 8 - 1;
    if (indexStartPos > 0) {
      df.seek(indexStartPos);
      final long indexStart = df.readLong();
      df.seek(indexStart);
      return df.readLong();
    } else {
      return 0;
    }
  }

  public OffsetLengthCoder getOffsetLengthCoder() {
    return this.olCoder;
  }

  long getIndexOffsetStart() {
    return this.indexOffsetStart;
  }

  @Override
  public Iterator<Item<T>> iterator() {
    try {
      return this.indexSize > 0
          ? new ItemsSegment<>(this.dataFile, this.indexOffsetStart, this.indexSize, this.olCoder,
          this.keyClass)
          : Collections.emptyIterator();
    } catch (IOException e) {
      throw new IllegalStateException(String.format(
          "failed creating index segment from %s, pos: %d, size: %d, %s, %s",
          this.dataFile, this.indexOffsetStart, this.indexSize, this.olCoder,
          this.keyClass.getSimpleName()),
          e);
    }
  }
}
