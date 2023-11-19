package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessStkOreCompanyData implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final Object obj = model.get("data");
    final String[] symbols = getMajorSymbols(req);
    return Access.of(symbols[0], obj != null ? AccessStatus.OK : AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
