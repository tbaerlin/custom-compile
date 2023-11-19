package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domainimpl.data.NullBasicHistoricRatios;
import java.util.List;

/**
 * @author zzhao
 */
public class AccessHistoricRatios extends AccessSymbolAndPredicate {

  public AccessHistoricRatios() {
    super("ratios", obj -> {
      final List<BasicHistoricRatios> ratios = (List<BasicHistoricRatios>) obj;
      for (BasicHistoricRatios ratio : ratios) {
        if (!(ratio instanceof NullBasicHistoricRatios)) {
          return true;
        }
      }
      return false;
    });
  }
}
