/*
 * EodPricesReaderImpl.java
 *
 * Created on 11.01.13 16:28
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
class EodPricesReader extends ReaderBase implements EodReader<EodPrices> {

  public EodPricesReader(DataFile df) throws IOException {
    super(df);
  }

  @Override
  public EodIterator<EodPrices> iterator() {
    return new EodIteratorImpl<>(getIndexSegment(), getTransporter(), new EodPrices());
  }
}
