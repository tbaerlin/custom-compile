package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.NullWarrantRatios;
import de.marketmaker.istar.domain.data.WarrantRatios;

/**
 * @author zzhao
 */
public class AccessWntRatios extends AccessSymbolAndPredicate {

  public AccessWntRatios() {
    super("ratios", obj -> {
      final WarrantRatios ratios = (WarrantRatios) obj;
      return !(ratios instanceof NullWarrantRatios);
    });
  }
}
