package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
public class AccessNoSymbolOneCollection implements AtomAccessCollector {

  private final String itemKey;

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final Object obj = model.get(this.itemKey);

    if (obj == null) {
      return Access.of(AccessStatus.NO_DATA);
    }

    final Collection collection = (Collection) obj;
    return Access.of(!collection.isEmpty() ? AccessStatus.OK : AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
