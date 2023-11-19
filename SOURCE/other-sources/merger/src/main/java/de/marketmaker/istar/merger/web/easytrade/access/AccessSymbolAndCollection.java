package de.marketmaker.istar.merger.web.easytrade.access;

import java.util.Collection;

/**
 * @author zzhao
 */
public class AccessSymbolAndCollection extends AccessSymbolAndPredicate {

  public AccessSymbolAndCollection(String collectionKey) {
    super(collectionKey, obj -> !((Collection) obj).isEmpty());
  }
}
