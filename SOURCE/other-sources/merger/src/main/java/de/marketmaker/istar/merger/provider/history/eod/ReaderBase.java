/*
 * EodReaderBase.java
 *
 * Created on 11.01.13 16:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.history.BufferedBytesTransporter;
import de.marketmaker.istar.feed.history.ItemsSegment;
import java.io.Closeable;
import java.io.IOException;

/**
 * Only supports sequential buffered reading. The buffer size is either BUF_SIZE (default 32M) or
 * the data size, whichever smaller is.
 *
 * @author zzhao
 */
public abstract class ReaderBase implements Closeable {

  private final DataFile df;

  private final ItemsSegment<Long> itemsSegment;

  private final BufferedBytesTransporter tran;

  public ReaderBase(DataFile df) throws IOException {
    this.df = df;
    if (df.size() > 0) {
      final long indexStartPos = df.size() - 8 - 1;
      df.seek(indexStartPos);
      final long indexStart = df.readLong();
      final OffsetLengthCoder olCoder = new OffsetLengthCoder(df.readByte());
      df.seek(indexStart);
      final long offset0 = df.readLong();
      final long indexLen = indexStart - offset0;
      this.itemsSegment = new ItemsSegment<>(df, offset0, indexLen, olCoder, Long.class);
      this.tran = new BufferedBytesTransporter(this.df, olCoder.maxLength());
    } else {
      this.itemsSegment = new ItemsSegment<>();
      this.tran = null;
    }
  }

  protected ItemsSegment<Long> getIndexSegment() {
    return itemsSegment;
  }

  protected BufferedBytesTransporter getTransporter() {
    return this.tran;
  }

  @Override
  public void close() throws IOException {
    IoUtils.close(this.df);
  }
}
