/*
 * ItemBuffer.java
 *
 * Created on 12.12.12 17:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.merger.provider.history.eod.ReaderBase;
import java.io.IOException;

/**
 * @author zzhao
 */
class EodFieldsReader extends ReaderBase implements EodReader<EodFields> {

  public EodFieldsReader(DataFile df) throws IOException {
    super(df);
  }

  @Override
  public EodIterator<EodFields> iterator() {
    return new EodIteratorImpl<>(getIndexSegment(), getTransporter(), new EodFields());
  }
}
