package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessSymbolAndObjects implements AtomAccessCollector {

  private final String quoteKey;

  private final String[] itemKeys;

  public AccessSymbolAndObjects(String quoteKey, String[] itemKeys) {
    this.quoteKey = quoteKey;
    this.itemKeys = itemKeys;
  }

  public AccessSymbolAndObjects(String[] itemKeys) {
    this("quote", itemKeys);
  }

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final Quote quote = (Quote) model.get(this.quoteKey);

    for (String itemKey : this.itemKeys) {
      final Object obj = model.get(itemKey);
      if (obj != null) {
        return toAccess(quote, getMajorSymbols(req), AccessStatus.OK);
      }
    }

    return toAccess(quote, getMajorSymbols(req), AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
