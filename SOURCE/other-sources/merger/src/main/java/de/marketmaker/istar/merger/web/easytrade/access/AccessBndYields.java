package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getListId;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.block.BndYields;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessBndYields implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // no request symbol
    final Map<String, BndYields.YieldElement> yields =
        (Map<String, BndYields.YieldElement>) model.get("yields");
    if (yields == null || yields.isEmpty()) {
      return Access.of(getSymbol(req), AccessStatus.NO_DATA);
    }

    final ArrayList<Access> accesses = new ArrayList<>(yields.size());
    for (BndYields.YieldElement yieldElement : yields.values()) {
      accesses.add(Access.of(yieldElement.getQuote(), AccessStatus.OK));
    }

    return accesses;
  }

  private String getSymbol(AtomRequest req) {
    final String[] strings = getMajorSymbols(req);
    return strings.length == 0 ? "" : strings[0];
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getListId(req);
  }
}
