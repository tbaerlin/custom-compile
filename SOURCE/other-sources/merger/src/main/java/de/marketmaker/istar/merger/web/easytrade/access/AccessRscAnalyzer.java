package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.Access.SYMBOL_DEFAULT;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.analyses.analyzer.ReportView;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Reports query and providerId
 */
public class AccessRscAnalyzer implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final AccessStatus status = hasResultData(model) ? AccessStatus.OK : AccessStatus.NO_DATA;
    return Access.of(getQuery(req), getProviderId(req), status);
  }

  private boolean hasResultData(Map<String, Object> model) {
    final ReportView view = (ReportView) model.get("view");
    if (view == null || view.getRows().isEmpty())
      return false;

    return view.getRows().stream()
        .anyMatch(values -> !values.isEmpty());
  }

  /*
   * Request parameter query can be empty/blank sometimes.
   * Blank string is also processed as SYMBOL_DEFAULT
   */
  private String getQuery(AtomRequest request) {
    final String[] param = getParameter(request, "query");
    return (param.length > 0) ?
        (StringUtils.isBlank(param[0]) ? SYMBOL_DEFAULT : param[0]) : SYMBOL_DEFAULT;
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
