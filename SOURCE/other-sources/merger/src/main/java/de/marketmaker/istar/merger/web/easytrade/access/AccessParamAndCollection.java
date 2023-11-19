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
public class AccessParamAndCollection implements AtomAccessCollector {

  private final String paramKey;

  private final String itemKey;

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Object obj = model.get(this.itemKey);
    if (obj == null) {
      toAccess(null, getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    if (obj instanceof Collection) {
      return toAccess(null, getMajorSymbols(req),
          ((Collection) obj).isEmpty() ? AccessStatus.NO_DATA : AccessStatus.OK);
    } else if (obj instanceof Map) {
      return toAccess(null, getMajorSymbols(req),
          ((Map) obj).isEmpty() ? AccessStatus.NO_DATA : AccessStatus.OK);
    }

    throw new IllegalStateException(
        "no support for: " + req.getName() + ", " + obj.getClass().getName());
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, this.paramKey);
  }
}
