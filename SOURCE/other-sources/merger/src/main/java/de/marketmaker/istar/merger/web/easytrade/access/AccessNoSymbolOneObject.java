package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
public class AccessNoSymbolOneObject implements AtomAccessCollector {

  private final String itemKey;

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final Object obj = model.get(this.itemKey);

    if (obj == null) {
      return Access.of(AccessStatus.NO_DATA);
    }

    if (obj instanceof String) {
      return Access.of(
          StringUtils.isNoneBlank((String) obj) ? AccessStatus.OK : AccessStatus.NO_DATA);
    }

    // not null
    return Access.of(AccessStatus.OK);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
