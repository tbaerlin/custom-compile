/*
 * EodIteratorImpl.java
 *
 * Created on 23.01.13 10:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import de.marketmaker.istar.feed.history.BufferedBytesTransporter;
import de.marketmaker.istar.feed.history.ByteArrayTarget;
import de.marketmaker.istar.feed.history.ItemsSegment;
import de.marketmaker.istar.feed.history.Item;
import java.io.IOException;

/**
 * @author zzhao
 */
public class EodIteratorImpl<T extends EodItem> implements EodIterator<T> {

  private final ItemsSegment<Long> itemsSegment;

  private final BufferedBytesTransporter transporter;

  private final T item;

  private final ByteArrayTarget target;

  private long quote;

  public EodIteratorImpl(ItemsSegment<Long> itemsSegment, BufferedBytesTransporter tran, T item) {
    this.itemsSegment = itemsSegment;
    this.transporter = tran;
    this.item = item;
    this.target = new ByteArrayTarget();
  }

  @Override
  public long getQuote() {
    return this.quote;
  }

  @Override
  public boolean hasNext() {
    return this.itemsSegment.hasNext();
  }

  @Override
  public T next() {
    final Item<Long> indexItem = this.itemsSegment.next();
    this.quote = indexItem.getKey();
    try {
      this.transporter.transferTo(indexItem.getOffset(), indexItem.getLength(), this.target);
    } catch (IOException e) {
      throw new IllegalStateException("cannot iterate end-of-day history file", e);
    }
    this.item.withData(this.target.data());
    return this.item;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("not implemented");
  }
}
