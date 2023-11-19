package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.ExtendedHistoricRatios;
import de.marketmaker.istar.domainimpl.data.NullExtendedHistoricRatios;
import java.util.List;

/**
 * @author hummell
 */
public class AccessExtendedHistoricRatios extends AccessSymbolAndPredicate {

  public AccessExtendedHistoricRatios() {
    super("ratios", obj -> {
      final List<ExtendedHistoricRatios> ratios = (List<ExtendedHistoricRatios>) obj;
      for (ExtendedHistoricRatios ratio : ratios) {
        if (!(ratio instanceof NullExtendedHistoricRatios)) {
          return true;
        }
      }
      return false;
    });
  }
}
