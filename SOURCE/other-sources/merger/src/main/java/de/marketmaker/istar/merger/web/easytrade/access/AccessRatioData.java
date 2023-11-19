package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.RatioDataRecord;

/**
 * @author zzhao
 */
public class AccessRatioData extends AccessSymbolAndPredicate {

  public AccessRatioData() {
    super("ratios", obj -> {
      final RatioDataRecord ratios = (RatioDataRecord) obj;
      return ratios != RatioDataRecord.NULL;
    });
  }
}
