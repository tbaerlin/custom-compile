package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;

/**
 * @author zzhao
 */
public class AccessPriceData extends AccessSymbolAndPredicate {

  public AccessPriceData() {
    super("price", obj -> {
      final PriceRecord price = (PriceRecord) obj;
      return price.getPriceQuality() != PriceQuality.NONE;
    });
  }
}
