package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.merger.web.easytrade.ListResult;

/**
 * @author zzhao
 */
public class AccessMatrix extends AccessSymbolAndPredicate {

  public AccessMatrix() {
    super("underlyingQuote", "listinfo", obj -> {
      final ListResult listResult = (ListResult) obj;
      return listResult.getCount() > 0;
    });
  }
}
