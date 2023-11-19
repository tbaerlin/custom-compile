package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.ok;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessMscChains implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Object count = model.get("count");

    if (count == null) {
      noData(getMajorSymbols(req));
    }

    if (count instanceof Integer) {
      return (Integer) count == 0 ? noData(getMajorSymbols(req)) : ok(getMajorSymbols(req));
    }

    throw new IllegalStateException("check MscChains model");
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    final String[] queries = getParameter(req, "query");
    return queries == null ? getParameter(req, "searchstring") : queries;
  }
}
