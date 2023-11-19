package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
public class AccessParamAndCollections implements AtomAccessCollector {

  private final String paramKey;

  private final String[] itemKeys;

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    for (String itemKey : this.itemKeys) {
      final Object obj = model.get(itemKey);
      if ((obj instanceof Collection) && !((Collection) obj).isEmpty()) {
        return toAccess(null, getMajorSymbols(req), AccessStatus.OK);
      } else if ((obj instanceof Map) && !((Map) obj).isEmpty()) {
        return toAccess(null, getMajorSymbols(req), AccessStatus.OK);
      }
    }

    return toAccess(null, getMajorSymbols(req), AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, this.paramKey);
  }
}
