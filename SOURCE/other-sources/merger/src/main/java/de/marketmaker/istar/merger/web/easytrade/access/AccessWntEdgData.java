package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domainimpl.data.NullEdgData;

/**
 * @author zzhao
 */
public class AccessWntEdgData extends AccessSymbolAndPredicate {

  public AccessWntEdgData() {
    super("data", obj -> {
      final EdgData data = (EdgData) obj;
      return !(data instanceof NullEdgData);
    });
  }
}
