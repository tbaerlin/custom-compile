package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessStkScreenerUpDown implements AtomAccessCollector {

  private static final List<String> KEYS = Arrays.asList(
      "ups", "downs"
  );

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final String[] symbols = getMajorSymbols(req);
    for (String key : KEYS) {
      final Object obj = model.get(key);
      if ((obj instanceof List) && !((List) obj).isEmpty()) {
        return Access.of(symbols[0], AccessStatus.OK);
      }
    }

    return Access.of(symbols[0], AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, "region");
  }
}
