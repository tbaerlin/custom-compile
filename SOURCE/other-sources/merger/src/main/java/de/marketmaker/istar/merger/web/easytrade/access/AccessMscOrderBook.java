package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.NullOrderbookData;
import de.marketmaker.istar.domain.data.OrderbookData;

/**
 * @author zzhao
 */
public class AccessMscOrderBook extends AccessSymbolAndPredicate {

  public AccessMscOrderBook() {
    super("data", obj -> {
      final OrderbookData data = (OrderbookData) obj;
      return !(data instanceof NullOrderbookData);
    });
  }
}
