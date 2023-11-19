package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.Access.SYMBOL_DEFAULT;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.web.easytrade.ListInfo;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

/**
 * Very similar to {@link AccessFinder}.
 * However, this collector reports the mandatory providerId parameter in the request.
 */
public class AccessRscFinderNew implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final int count = getResultCount(model);
    final AccessStatus status = count == 0 ? AccessStatus.NO_DATA : AccessStatus.OK;

    return Access.of(getQuery(req), getProviderId(req), status);
  }

  private int getResultCount(Map<String, Object> model) {
    final Object listResult = model.get("listinfo");
    if (listResult instanceof ListResult) {
      return ((ListResult) listResult).getCount();
    } else if (listResult instanceof ListInfo) {
      return ((ListInfo) listResult).getCount();
    }

    throw new IllegalStateException("check finder implementation");
  }

  private String getQuery(AtomRequest request) {
    final String[] param = getParameter(request, "query");
    return (param.length > 0) ? param[0] : SYMBOL_DEFAULT;
  }

  private String getProviderId(AtomRequest request) {
    final String[] param = getParameter(request, "providerId");
    return (param.length > 0) ? param[0] : SYMBOL_DEFAULT;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }

}
