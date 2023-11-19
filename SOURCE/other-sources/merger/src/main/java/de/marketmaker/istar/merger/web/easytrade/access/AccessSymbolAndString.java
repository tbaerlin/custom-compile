package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
public class AccessSymbolAndString implements AtomAccessCollector {

  private final String quoteKey;

  private final String itemKey;

  public AccessSymbolAndString(String quoteKey, String itemKey) {
    this.quoteKey = quoteKey;
    this.itemKey = itemKey;
  }

  public AccessSymbolAndString(String itemKey) {
    this("quote", itemKey);
  }

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final Object obj = model.get(this.itemKey);
    final Quote quote = (Quote) model.get(this.quoteKey);

    if (obj == null) {
      return toAccess(quote, getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    if (obj instanceof String) {
      return toAccess(quote, getMajorSymbols(req),
          StringUtils.isNoneBlank((String) obj) ? AccessStatus.OK : AccessStatus.NO_DATA);
    } else { // no string
      throw new IllegalStateException("wrong usage");
    }
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
