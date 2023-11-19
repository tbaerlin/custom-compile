package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author zzhao
 */
public class AccessSymbolAndPredicate implements AtomAccessCollector {

  private final String quoteKey;

  private final String itemKey;

  private final Predicate<Object> predicate;

  public AccessSymbolAndPredicate(String quoteKey, String itemKey, Predicate<Object> predicate) {
    this.quoteKey = quoteKey;
    this.itemKey = itemKey;
    this.predicate = predicate;
  }

  public AccessSymbolAndPredicate(String itemKey, Predicate<Object> predicate) {
    this("quote", itemKey, predicate);
  }

  public AccessSymbolAndPredicate(String quoteKey, String itemKey) {
    this(quoteKey, itemKey, Objects::nonNull);
  }

  public AccessSymbolAndPredicate(String itemKey) {
    this(itemKey, Objects::nonNull);
  }

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final Object obj = model.get(this.itemKey);
    final Quote quote = (Quote) model.get(this.quoteKey);

    if (obj == null) {
      return toAccess(quote, getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    return toAccess(quote, getMajorSymbols(req),
        this.predicate.test(obj) ? AccessStatus.OK : AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
