/*
 * SnapFieldIteratorFactory.java
 *
 * Created on 17.10.12 11:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import java.util.function.Consumer;

/**
 * Uses an array of {@link VwdFieldOrder} ids (which must be in ascending order and does not contain
 * duplicates) to iterate {@link BufferFieldData}.
 *
 * @author zzhao
 */
public class FieldDataIterator {

  public static FieldDataIterator create(int[] orderIds) {
    if (null == orderIds || orderIds.length == 0) {
      throw new IllegalArgumentException("non-empty array of VwdFieldOrder ids required");
    }
    for (int i = 1; i < orderIds.length; i++) {
      if (orderIds[i] <= orderIds[i - 1]) {
        throw new IllegalArgumentException(
            "VwdFieldOrder ids must be in ascending order and does not contain duplicates");
      }
    }

    return new FieldDataIterator(orderIds);
  }

  private final int[] orderIds;

  private FieldDataIterator(int[] orderIds) {
    this.orderIds = orderIds;
  }

  public void iterate(final BufferFieldData fd, Consumer<FieldData> consumer) {
    int n = 0;
    for (int oid = fd.readNext(); oid != 0; oid = fd.readNext()) {
      while (oid > this.orderIds[n]) {
        consumer.accept(null);
        n++;
        if (n == this.orderIds.length) {
          return;
        }
      }

      if (oid == this.orderIds[n]) {
        consumer.accept(fd);
        n++;
        if (n == this.orderIds.length) {
          return;
        }
      } else {
        fd.skipCurrent();
      }
    }

    for (int i = n; i < this.orderIds.length; i++) {
      consumer.accept(null);
    }
  }
}
